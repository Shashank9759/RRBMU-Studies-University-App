/**
 * Firestore seed script for all 31 course skeletons.
 *
 * Usage (requires Firebase Admin SDK):
 *   cd scripts && npm install firebase-admin
 *   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/serviceAccount.json
 *   export FIREBASE_PROJECT_ID=your-new-project-id
 *   node seed-courses.js
 *
 * Seeds: courses → systems → parts for every course in CourseSeed.
 * Papers are NOT seeded — admins upload those via the app.
 */

const admin = require('firebase-admin');

const projectId = process.env.FIREBASE_PROJECT_ID;
if (!projectId) {
  console.error('Set FIREBASE_PROJECT_ID to your Firebase project ID (e.g. export FIREBASE_PROJECT_ID=my-project-id)');
  process.exit(1);
}

admin.initializeApp({ projectId });

const db = admin.firestore();

const COURSES = [
  { id: 'bsc', name: 'Bachelor of Science', shortName: 'B.Sc', level: 'UG', order: 1 },
  { id: 'ba', name: 'Bachelor of Arts', shortName: 'B.A', level: 'UG', order: 2 },
  { id: 'bcom', name: 'Bachelor of Commerce', shortName: 'B.Com', level: 'UG', order: 3 },
  { id: 'bba', name: 'Bachelor of Business Administration', shortName: 'BBA', level: 'UG', order: 4 },
  { id: 'bca', name: 'Bachelor of Computer Applications', shortName: 'BCA', level: 'UG', order: 5 },
  { id: 'b_ed', name: 'Bachelor of Education', shortName: 'B.Ed', level: 'UG', order: 6 },
  { id: 'ballb', name: 'Bachelor of Arts LLB', shortName: 'BA LLB', level: 'UG', order: 7 },
  { id: 'bds', name: 'Bachelor of Dental Surgery', shortName: 'BDS', level: 'UG', order: 8 },
  { id: 'be', name: 'Bachelor of Engineering', shortName: 'B.E', level: 'UG', order: 9 },
  { id: 'bhm', name: 'Bachelor of Hotel Management', shortName: 'BHM', level: 'UG', order: 10 },
  { id: 'bhsc', name: 'Bachelor of Health Science', shortName: 'BHSc', level: 'UG', order: 11 },
  { id: 'bped', name: 'Bachelor of Physical Education', shortName: 'B.P.Ed', level: 'UG', order: 12 },
  { id: 'bscbed', name: 'B.Sc B.Ed', shortName: 'B.Sc B.Ed', level: 'UG', order: 13 },
  { id: 'bsw', name: 'Bachelor of Social Work', shortName: 'BSW', level: 'UG', order: 14 },
  { id: 'llb', name: 'Bachelor of Laws', shortName: 'LLB', level: 'UG', order: 15 },
  { id: 'ma', name: 'Master of Arts', shortName: 'M.A', level: 'PG', order: 16 },
  { id: 'mba', name: 'Master of Business Administration', shortName: 'MBA', level: 'PG', order: 17 },
  { id: 'mbbs', name: 'Bachelor of Medicine & Surgery', shortName: 'MBBS', level: 'UG', order: 18 },
  { id: 'mca', name: 'Master of Computer Applications', shortName: 'MCA', level: 'PG', order: 19 },
  { id: 'mcom', name: 'Master of Commerce', shortName: 'M.Com', level: 'PG', order: 20 },
  { id: 'md', name: 'Doctor of Medicine', shortName: 'MD', level: 'PG', order: 21 },
  { id: 'mds', name: 'Master of Dental Surgery', shortName: 'MDS', level: 'PG', order: 22 },
  { id: 'med', name: 'Master of Education', shortName: 'M.Ed', level: 'PG', order: 23 },
  { id: 'mped', name: 'Master of Physical Education', shortName: 'M.P.Ed', level: 'PG', order: 24 },
  { id: 'mphil', name: 'Master of Philosophy', shortName: 'M.Phil', level: 'PG', order: 25 },
  { id: 'ms', name: 'Master of Surgery', shortName: 'MS', level: 'PG', order: 26 },
  { id: 'msc', name: 'Master of Science', shortName: 'M.Sc', level: 'PG', order: 27 },
  { id: 'msw', name: 'Master of Social Work', shortName: 'MSW', level: 'PG', order: 28 },
  { id: 'babed', name: 'BA B.Ed', shortName: 'BA B.Ed', level: 'UG', order: 29 },
  { id: 'diploma', name: 'Diploma Courses', shortName: 'Diploma', level: 'DIPLOMA', order: 30 },
  { id: 'pgdca', name: 'Post Graduate Diploma in Computer Applications', shortName: 'PGDCA', level: 'DIPLOMA', order: 31 },
];

const SYSTEMS = [
  { id: 'yearly_system', name: 'Yearly System', type: 'YEARLY' },
  { id: 'semester_system', name: 'Semester System', type: 'SEMESTER' },
  { id: 'entrance_exam', name: 'Entrance Exam', type: 'ENTRANCE' },
];

const YEARLY_PARTS = [
  { id: 'part_1', name: 'Part 1', order: 1 },
  { id: 'part_2', name: 'Part 2', order: 2 },
  { id: 'part_3', name: 'Part 3', order: 3 },
];

const SEMESTER_PARTS = Array.from({ length: 6 }, (_, i) => ({
  id: `semester_${i + 1}`,
  name: `Semester ${i + 1}`,
  order: i + 1,
}));

const ENTRANCE_PARTS = [
  { id: 'entrance_papers', name: 'Entrance Papers', order: 1 },
];

async function seed() {
  const batch = db.batch();
  let ops = 0;

  for (const course of COURSES) {
    const courseRef = db.collection('courses').doc(course.id);
    batch.set(courseRef, {
      name: course.name,
      shortName: course.shortName,
      iconUrl: null,
      order: course.order,
      isActive: true,
      level: course.level,
      durationYears: 3,
    });
    ops++;

    for (const system of SYSTEMS) {
      const systemRef = courseRef.collection('systems').doc(system.id);
      batch.set(systemRef, { name: system.name, type: system.type });
      ops++;

      const parts =
        system.id === 'yearly_system' ? YEARLY_PARTS :
        system.id === 'semester_system' ? SEMESTER_PARTS :
        ENTRANCE_PARTS;

      for (const part of parts) {
        const partRef = systemRef.collection('parts').doc(part.id);
        batch.set(partRef, { name: part.name, order: part.order, paperCount: 0 });
        ops++;
      }
    }
  }

  await batch.commit();
  console.log(`Seeded ${COURSES.length} courses with ${ops} total writes.`);
}

seed().catch(console.error);
