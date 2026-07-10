const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

const TOPIC = "all_users";

/**
 * Sends a real FCM push whenever an admin creates or publishes a notification.
 *
 * Firestore doc: notifications/{id}
 * - title, body, isActive
 * - attachmentUrl / linkUrl / pageUrl / imageUrl (optional)
 */
exports.onNotificationWrite = onDocumentWritten(
  {
    document: "notifications/{id}",
    region: "asia-south1",
  },
  async (event) => {
    const before = event.data.before?.exists ? event.data.before.data() : null;
    const after = event.data.after?.exists ? event.data.after.data() : null;
    if (!after) return null;

    // Only push when the notification is active/published.
    if (after.isActive !== true) return null;

    // Avoid re-pushing on unrelated edits of an already-active doc.
    // Still push when first created as active, or when flipped inactive -> active.
    const wasActive = before?.isActive === true;
    if (wasActive && before && after) {
      const samePayload =
        before.title === after.title &&
        before.body === after.body &&
        (before.attachmentUrl || before.linkUrl || "") ===
          (after.attachmentUrl || after.linkUrl || "");
      if (samePayload) {
        logger.info("Skipping FCM — already-active notification with same payload");
        return null;
      }
    }

    const id = event.params.id;
    const title = after.title || "RRBMU Studies";
    const body = after.body || "";
    const attachmentUrl =
      after.attachmentUrl || after.linkUrl || after.pageUrl || after.imageUrl || "";

    const message = {
      topic: TOPIC,
      // Include both notification + data so tray shows even when app is backgrounded
      // on OEM builds that don't reliably deliver data-only messages.
      notification: {
        title: String(title),
        body: String(body),
      },
      data: {
        type: "notification",
        notificationId: String(id),
        title: String(title),
        body: String(body),
        attachmentUrl: String(attachmentUrl),
        linkUrl: after.linkUrl ? String(after.linkUrl) : String(attachmentUrl),
        pageUrl: after.pageUrl ? String(after.pageUrl) : "",
        category: after.category ? String(after.category) : "",
      },
      android: {
        priority: "high",
        notification: {
          channelId: "notifications",
          clickAction: "OPEN_NOTIFICATION",
        },
      },
    };

    try {
      const messageId = await admin.messaging().send(message);
      logger.info("FCM sent", { messageId, topic: TOPIC, notificationId: id });
      return messageId;
    } catch (err) {
      logger.error("FCM send failed", err);
      throw err;
    }
  },
);
