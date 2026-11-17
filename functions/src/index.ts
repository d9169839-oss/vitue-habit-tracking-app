import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore, Timestamp } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { hasRecentAuthentication } from "./accountDeletionPolicy";

initializeApp();

/**
 * Deletes only the caller's own account. The UID is taken from verified Firebase Auth
 * context; no UID or collection path is accepted from the client.
 *
 * A short-lived tombstone is created first. Firestore rules use it to reject stale client
 * writes while deletion runs, preventing a background sync from recreating deleted records.
 * The Admin SDK then recursively deletes the user document and all nested data. Authentication
 * is deleted last so a failed purge can be retried by the still-authenticated account.
 */
export const deleteMyAccount = onCall(
  {
    region: "asia-south1",
    enforceAppCheck: true,
    maxInstances: 5
  },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Please sign in before deleting your account.");
    }

    const authTime = Number(request.auth.token.auth_time);
    const nowMillis = Date.now();
    const nowSeconds = Math.floor(nowMillis / 1000);
    if (!hasRecentAuthentication(authTime, nowSeconds)) {
      throw new HttpsError(
        "failed-precondition",
        "For your security, sign out and sign in again, then retry account deletion."
      );
    }

    const uid = request.auth.uid;
    const firestore = getFirestore();
    const userDocument = firestore.collection("users").doc(uid);
    const tombstone = firestore.collection("accountDeletionTombstones").doc(uid);

    try {
      // ID tokens expire after about one hour. A 24-hour TTL window safely outlives stale tokens.
      // This marker contains no habit/profile content; it exists only to prevent data resurrection.
      await tombstone.set({
        expireAt: Timestamp.fromMillis(nowMillis + 24 * 60 * 60 * 1000)
      }, { merge: true });

      // recursiveDelete includes all nested collections, not just the parent document.
      await firestore.recursiveDelete(userDocument);
    } catch (error) {
      logger.error("Account data deletion failed; deletion marker retained for safe retry.");
      throw new HttpsError(
        "internal",
        "Your deletion request could not finish. Your account is still active; please retry."
      );
    }

    try {
      await getAuth().deleteUser(uid);
    } catch (error) {
      const code = (error as { code?: string }).code;
      if (code !== "auth/user-not-found") {
        // Firestore is already purged and writes remain blocked by the tombstone. The same
        // account can retry this callable; recursiveDelete is safe against an empty user tree.
        logger.error("Firebase Authentication account deletion failed after data purge.");
        throw new HttpsError(
          "internal",
          "Your cloud data was deleted, but account removal could not finish. Please retry."
        );
      }
    }

    logger.info("Account deletion completed.");
    return { success: true };
  }
);
