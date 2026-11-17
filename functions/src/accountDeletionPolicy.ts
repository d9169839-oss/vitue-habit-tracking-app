export const RECENT_AUTHENTICATION_WINDOW_SECONDS = 5 * 60;

/**
 * Firebase ID tokens expose auth_time in seconds. A freshly refreshed ID token does not
 * refresh auth_time, so this verifies a real recent sign-in rather than token refresh.
 */
export function hasRecentAuthentication(
  authTimeSeconds: number,
  nowSeconds: number,
  maxAgeSeconds = RECENT_AUTHENTICATION_WINDOW_SECONDS
): boolean {
  return Number.isFinite(authTimeSeconds)
    && authTimeSeconds > 0
    && nowSeconds >= authTimeSeconds
    && nowSeconds - authTimeSeconds <= maxAgeSeconds;
}
