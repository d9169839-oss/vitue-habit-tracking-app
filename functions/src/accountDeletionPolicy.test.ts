import { describe, expect, it } from "vitest";
import {
  hasRecentAuthentication,
  RECENT_AUTHENTICATION_WINDOW_SECONDS
} from "./accountDeletionPolicy";

describe("hasRecentAuthentication", () => {
  const now = 1_800_000_000;

  it("accepts a sign-in within the five-minute window", () => {
    expect(hasRecentAuthentication(now - 60, now)).toBe(true);
  });

  it("accepts the exact five-minute boundary", () => {
    expect(hasRecentAuthentication(now - RECENT_AUTHENTICATION_WINDOW_SECONDS, now)).toBe(true);
  });

  it("rejects an old sign-in", () => {
    expect(hasRecentAuthentication(now - 301, now)).toBe(false);
  });

  it("rejects missing, zero, and future timestamps", () => {
    expect(hasRecentAuthentication(Number.NaN, now)).toBe(false);
    expect(hasRecentAuthentication(0, now)).toBe(false);
    expect(hasRecentAuthentication(now + 1, now)).toBe(false);
  });
});
