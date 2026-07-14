/**
 * Bulk-uploads scraped RRBMU papers to Firebase.
 *
 *   PDFs      → Storage: papers/{courseId}/{systemId}/{partId}/{fileName}
 *   Metadata  → Firestore: courses/{courseId}/systems/{systemId}/parts/{partId}/papers/{paperId}
 *
 * Usage:
 *   node upload.js --dry-run          # parse + preview only, writes preview.json
 *   node upload.js                    # real upload (resumable, skips already-done)
 *
 * Auth (real upload only):
 *   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/serviceAccountKey.json
 *   (or place serviceAccountKey.json next to this script)
 *
 * Source folder defaults to the scraped FinalPdf dir; override with --src=/path.
 */

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const DRY_RUN = process.argv.includes('--dry-run');
const srcArg = process.argv.find((a) => a.startsWith('--src='));
const SRC = srcArg
  ? srcArg.slice('--src='.length)
  : '/Users/shashankranjan/claudeProjects/rrbmuDataScraping/FinalPdf';

const PROJECT_ID = 'rbmu-studies-prod';
const BUCKET = 'rbmu-studies-prod.firebasestorage.app';
const CHECKPOINT = path.join(__dirname, '.uploaded.json');

// ---------------------------------------------------------------------------
// Folder → Firestore ID mapping
// ---------------------------------------------------------------------------
const COURSE_IDS = {
  'b-ed': 'b_ed',
  ba: 'ba',
  babed: 'babed',
  bcom: 'bcom',
  bsc: 'bsc',
  bscbed: 'bscbed',
  ma: 'ma',
  msc: 'msc',
  med: 'med',
};

const SYSTEM_IDS = {
  'yearly-system': 'yearly_system',
  'semester-system': 'semester_system',
  'entrance-exam': 'entrance_exam',
};

function partId(folderPart) {
  // part-1 → part_1, semester-3 → semester_3
  return folderPart.replace(/-/g, '_');
}

function partName(folderPart) {
  const m = folderPart.match(/^(part|semester)-(\d+)$/);
  if (!m) return folderPart;
  const kind = m[1] === 'part' ? 'Part' : 'Semester';
  return `${kind} ${m[2]}`;
}

function partOrder(folderPart) {
  const m = folderPart.match(/(\d+)$/);
  return m ? parseInt(m[1], 10) : 99;
}

// ---------------------------------------------------------------------------
// Filename → metadata heuristics
// ---------------------------------------------------------------------------
const SUBJECT_KEYWORDS = [
  ['general english', 'General English'],
  ['general hindi', 'General Hindi'],
  ['political science', 'Political Science'],
  ['public administration', 'Public Administration'],
  ['home science', 'Home Science'],
  ['elementary computer', 'Computer Science'],
  ['computer science', 'Computer Science'],
  ['computer application', 'Computer Applications'],
  ['physical education', 'Physical Education'],
  ['hindi literature', 'Hindi Literature'],
  ['english literature', 'English Literature'],
  ['drawing and painting', 'Drawing & Painting'],
  ['environmental', 'Environmental Studies'],
  ['financial literacy', 'Financial Literacy'],
  ['value system', 'Indian Value System'],
  ['personality development', 'Personality Development'],
  ['communication skills', 'Communication Skills'],
  ['mathematics', 'Mathematics'],
  ['accountancy', 'Accountancy'],
  ['accounting', 'Accountancy'],
  ['business', 'Business Studies'],
  ['economics', 'Economics'],
  ['sociology', 'Sociology'],
  ['psychology', 'Psychology'],
  ['philosophy', 'Philosophy'],
  ['geography', 'Geography'],
  ['history', 'History'],
  ['sanskrit', 'Sanskrit'],
  ['urdu', 'Urdu'],
  ['punjabi', 'Punjabi'],
  ['physics', 'Physics'],
  ['chemistry', 'Chemistry'],
  ['botany', 'Botany'],
  ['zoology', 'Zoology'],
  ['biology', 'Biology'],
  ['statistics', 'Statistics'],
  ['commerce', 'Commerce'],
  ['education', 'Education'],
  ['english', 'English'],
  ['hindi', 'Hindi'],
  ['music', 'Music'],
  ['law', 'Law'],
];

const CODE_PREFIX_SUBJECT = {
  aen: 'General English',
  ahn: 'General Hindi',
  eng: 'English',
  hin: 'Hindi',
  san: 'Sanskrit',
  urd: 'Urdu',
  pol: 'Political Science',
  pad: 'Public Administration',
  geo: 'Geography',
  his: 'History',
  his_: 'History',
  eco: 'Economics',
  soc: 'Sociology',
  psy: 'Psychology',
  phi: 'Philosophy',
  mat: 'Mathematics',
  phy: 'Physics',
  che: 'Chemistry',
  bot: 'Botany',
  zoo: 'Zoology',
  hsc: 'Home Science',
  drg: 'Drawing & Painting',
  mus: 'Music',
  edu: 'Education',
  com: 'Commerce',
  acc: 'Accountancy',
  bus: 'Business Studies',
  sta: 'Statistics',
  cs: 'Computer Science',
  cmp: 'Computer Science',
  evs: 'Environmental Studies',
  ped: 'Physical Education',
  vac: 'Value Added Course',
  sec: 'Skill Enhancement',
  aec: 'Ability Enhancement',
  abs: 'Business Studies',
  bad: 'Business Administration',
};

const COURSE_TOKEN_RE =
  /^(b-?ed|ba|b-?a|bsc|b-?sc|bcom|b-?com|babed|bscbed|ma|m-?a|msc|m-?sc|med|m-?ed|bs|bed)$/;

function titleCase(words) {
  const small = new Set(['and', 'of', 'in', 'the', 'to', 'for', 'a', 'an', 'avum', 'evam']);
  return words
    .map((w, i) => {
      if (i > 0 && small.has(w)) return w;
      if (/^[ivx]+$/.test(w)) return w.toUpperCase(); // roman numerals
      return w.charAt(0).toUpperCase() + w.slice(1);
    })
    .join(' ');
}

function parseFileName(relPath) {
  const base = path.basename(relPath, '.pdf').toLowerCase();

  // Year: prefer the max 20xx anywhere in the name.
  const yearMatches = [...base.matchAll(/20\d{2}/g)].map((m) => parseInt(m[0], 10));
  const year = yearMatches.length ? Math.max(...yearMatches) : 0;

  let rest = base;

  // Subject-code style: aen-51t-1001, vac-52t-009, sec-t-015
  let paperCode = '';
  let codePrefix = '';
  const codeM = rest.match(/([a-z]{2,4})-(\d{0,2}[a-z]{1,2})-(\d{3,4})/);
  if (codeM) {
    paperCode = `${codeM[1]}-${codeM[2]}-${codeM[3]}`.toUpperCase();
    codePrefix = codeM[1];
    rest = rest.replace(codeM[0], ' ');
  }

  // bed-04 style course-paper code
  if (!paperCode) {
    const bedM = rest.match(/\b(bed-\d{1,3})\b/);
    if (bedM) {
      paperCode = bedM[1].toUpperCase();
      rest = rest.replace(bedM[0], ' ');
    }
  }

  // Set marker
  let set = '';
  const setM = rest.match(/set-([a-z])\b/);
  if (setM) {
    set = setM[1].toUpperCase();
    rest = rest.replace(setM[0], ' ');
  }

  // Full dates dd-mm-yyyy
  rest = rest.replace(/\b\d{2}-\d{2}-20\d{2}\b/g, ' ');
  // Remaining years
  rest = rest.replace(/\b20\d{2}\b/g, ' ');

  // Plain numeric paper code (e.g. 4652, 46811) — take it if no code yet.
  const numM = rest.match(/\b(\d{3,6})\b/);
  if (!paperCode && numM) {
    paperCode = numM[1];
  }
  rest = rest.replace(/\b\d{3,6}\b/g, ' ');

  // Strip structural tokens.
  const rawWords = rest
    .split(/[-_.]+/)
    .map((w) => w.trim())
    .filter(Boolean)
    .filter((w) => !COURSE_TOKEN_RE.test(w))
    .filter((w) => !/^(part|sem|semester|paper|question|previous|year|pyq|new|old)$/.test(w))
    .filter((w) => !/^\d+$/.test(w));

  // A lone trailing a/b/c/d (e.g. "…707-b-2022") is a set marker, not a word.
  const words = [];
  for (const w of rawWords) {
    if (/^[a-d]$/.test(w)) {
      if (!set) set = w.toUpperCase();
    } else if (w.length > 1 || /^[ivx]$/.test(w)) {
      words.push(w);
    }
  }

  // Subject from descriptive words.
  const descriptive = words.join(' ');
  let subject = '';
  for (const [needle, canonical] of SUBJECT_KEYWORDS) {
    if (descriptive.includes(needle)) {
      subject = canonical;
      break;
    }
  }
  if (!subject && codePrefix && CODE_PREFIX_SUBJECT[codePrefix]) {
    subject = CODE_PREFIX_SUBJECT[codePrefix];
  }
  if (!subject) subject = 'General';

  // Title
  let title;
  if (words.length >= 2) {
    title = titleCase(words);
  } else if (paperCode) {
    title = `${subject} (${paperCode})`;
  } else {
    title = subject;
  }
  if (set) title += ` — Set ${set}`;

  return { title, subject, paperCode, year };
}

function docIdFor(relPath) {
  const base = path
    .basename(relPath, '.pdf')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .slice(0, 120);
  // Tiny hash suffix guards against two files normalizing to the same id.
  const h = crypto.createHash('md5').update(relPath).digest('hex').slice(0, 6);
  return `${base}_${h}`;
}

// ---------------------------------------------------------------------------
// Build the work list from manifest.json
// ---------------------------------------------------------------------------
function buildWorkList() {
  const manifest = JSON.parse(fs.readFileSync(path.join(SRC, 'manifest.json'), 'utf8'));
  const items = [];
  const partsToEnsure = new Map(); // "course/system/part" → {courseId, systemId, partId, name, order}
  const problems = [];

  for (const [courseFolder, systems] of Object.entries(manifest)) {
    const courseId = COURSE_IDS[courseFolder];
    if (!courseId) {
      problems.push(`Unknown course folder: ${courseFolder}`);
      continue;
    }
    for (const [systemFolder, parts] of Object.entries(systems)) {
      const systemId = SYSTEM_IDS[systemFolder];
      if (!systemId) {
        problems.push(`Unknown system folder: ${courseFolder}/${systemFolder}`);
        continue;
      }
      for (const [partFolder, files] of Object.entries(parts)) {
        const pid = partId(partFolder);
        partsToEnsure.set(`${courseId}/${systemId}/${pid}`, {
          courseId,
          systemId,
          partId: pid,
          name: partName(partFolder),
          order: partOrder(partFolder),
        });
        for (const entry of files) {
          const relPath = entry.file;
          const absPath = path.join(SRC, relPath);
          if (!fs.existsSync(absPath)) {
            problems.push(`Missing file on disk: ${relPath}`);
            continue;
          }
          const meta = parseFileName(relPath);
          items.push({
            relPath,
            absPath,
            courseId,
            systemId,
            partId: pid,
            paperId: docIdFor(relPath),
            storagePath: `papers/${courseId}/${systemId}/${pid}/${path.basename(relPath)}`,
            sizeBytes: fs.statSync(absPath).size,
            ...meta,
          });
        }
      }
    }
  }
  return { items, partsToEnsure, problems };
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------
async function main() {
  const { items, partsToEnsure, problems } = buildWorkList();

  console.log(`Parsed ${items.length} papers across ${partsToEnsure.size} parts.`);
  if (problems.length) {
    console.log(`\n⚠ ${problems.length} problems:`);
    problems.slice(0, 20).forEach((p) => console.log('  - ' + p));
  }

  if (DRY_RUN) {
    const preview = items.map(({ absPath, ...rest }) => rest);
    fs.writeFileSync(path.join(__dirname, 'preview.json'), JSON.stringify(preview, null, 1));

    console.log('\n=== Sample derived metadata (12 random) ===');
    const shuffled = [...items].sort(() => 0.5 - Math.random()).slice(0, 12);
    for (const it of shuffled) {
      console.log(`\n  file:    ${it.relPath}`);
      console.log(`  → dest:   ${it.courseId}/${it.systemId}/${it.partId}/${it.paperId}`);
      console.log(`  → title:  ${it.title}`);
      console.log(`  → subject:${it.subject}  code:${it.paperCode || '—'}  year:${it.year}`);
    }

    const bySubject = {};
    items.forEach((i) => (bySubject[i.subject] = (bySubject[i.subject] || 0) + 1));
    console.log('\n=== Subject distribution ===');
    Object.entries(bySubject)
      .sort((a, b) => b[1] - a[1])
      .forEach(([s, n]) => console.log(`  ${String(n).padStart(4)}  ${s}`));
    const noYear = items.filter((i) => !i.year).length;
    console.log(`\nPapers without a detected year: ${noYear}`);
    console.log('\nDry run complete — full preview in preview.json');
    return;
  }

  // ---- Real upload ----
  const admin = require('firebase-admin');
  const keyPath =
    process.env.GOOGLE_APPLICATION_CREDENTIALS ||
    (fs.existsSync(path.join(__dirname, 'serviceAccountKey.json'))
      ? path.join(__dirname, 'serviceAccountKey.json')
      : null);
  if (!keyPath) {
    console.error(
      '\nNo credentials. Download a service-account key (Firebase Console → Project settings →' +
        ' Service accounts → Generate new private key) and save it as scripts/paper-uploader/serviceAccountKey.json',
    );
    process.exit(1);
  }
  admin.initializeApp({
    credential: admin.credential.cert(require(keyPath)),
    projectId: PROJECT_ID,
    storageBucket: BUCKET,
  });
  const db = admin.firestore();
  const bucket = admin.storage().bucket();

  const done = fs.existsSync(CHECKPOINT) ? JSON.parse(fs.readFileSync(CHECKPOINT, 'utf8')) : {};
  const saveCheckpoint = () => fs.writeFileSync(CHECKPOINT, JSON.stringify(done));

  // 1) Ensure part docs exist (merge — never clobbers existing fields).
  console.log('Ensuring system/part documents…');
  for (const p of partsToEnsure.values()) {
    const sysRef = db
      .collection('courses')
      .doc(p.courseId)
      .collection('systems')
      .doc(p.systemId);
    const sysName =
      p.systemId === 'yearly_system'
        ? 'Yearly System'
        : p.systemId === 'semester_system'
          ? 'Semester System'
          : 'Entrance Exam';
    const sysType =
      p.systemId === 'yearly_system'
        ? 'YEARLY'
        : p.systemId === 'semester_system'
          ? 'SEMESTER'
          : 'ENTRANCE';
    await sysRef.set({ name: sysName, type: sysType }, { merge: true });
    await sysRef
      .collection('parts')
      .doc(p.partId)
      .set({ name: p.name, order: p.order }, { merge: true });
  }

  // 2) Upload papers with limited concurrency.
  const queue = items.filter((it) => !done[it.paperId]);
  console.log(`Uploading ${queue.length} papers (${items.length - queue.length} already done)…`);
  let uploaded = 0;
  let failed = 0;
  const CONCURRENCY = 5;

  async function uploadOne(it) {
    const token = crypto.randomUUID();
    await bucket.upload(it.absPath, {
      destination: it.storagePath,
      metadata: {
        contentType: 'application/pdf',
        metadata: { firebaseStorageDownloadTokens: token },
      },
    });
    const pdfUrl =
      `https://firebasestorage.googleapis.com/v0/b/${BUCKET}/o/` +
      `${encodeURIComponent(it.storagePath)}?alt=media&token=${token}`;

    const now = Date.now();
    await db
      .collection('courses')
      .doc(it.courseId)
      .collection('systems')
      .doc(it.systemId)
      .collection('parts')
      .doc(it.partId)
      .collection('papers')
      .doc(it.paperId)
      .set(
        {
          title: it.title,
          subject: it.subject,
          paperCode: it.paperCode,
          year: it.year,
          description: null,
          pdfUrl,
          coverImageUrl: null,
          downloadCount: 0,
          isPublished: true,
          createdAt: now,
          updatedAt: now,
          createdBy: 'bulk-import',
        },
        { merge: true },
      );
  }

  let cursor = 0;
  async function worker() {
    while (cursor < queue.length) {
      const it = queue[cursor++];
      try {
        await uploadOne(it);
        done[it.paperId] = true;
        uploaded++;
        if (uploaded % 10 === 0) {
          saveCheckpoint();
          console.log(`  ${uploaded}/${queue.length} uploaded…`);
        }
      } catch (e) {
        failed++;
        console.error(`  ✗ ${it.relPath}: ${e.message}`);
      }
    }
  }
  await Promise.all(Array.from({ length: CONCURRENCY }, worker));
  saveCheckpoint();

  console.log(`\nDone. Uploaded ${uploaded}, failed ${failed}, skipped ${items.length - queue.length}.`);
  if (failed) console.log('Re-run the script to retry failures (already-done files are skipped).');
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
