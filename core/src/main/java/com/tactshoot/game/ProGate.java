package com.tactshoot.game;

/**
 * What the free version can use, and a tamper-evident token for the cached
 * "Pro owned" flag. Pure Java (no libGDX) so it can be unit tested anywhere.
 */
public final class ProGate {

    /** Free players get Dustyard (map 0) and the Assault Rifle. */
    public static boolean mapUnlocked(boolean pro, int mapIdx) {
        return pro || mapIdx == 0;
    }

    public static boolean weaponUnlocked(boolean pro, int weapon) {
        return pro || weapon == Weapons.AR;
    }

    /**
     * Token stored next to the cached flag. It ties the flag to a per-install
     * random seed, so copying a preferences file from another phone, or
     * flipping a boolean by hand, does not grant Pro. (A rooted user can still
     * defeat any client-side check; Bazaar re-verification on each launch is
     * what makes a genuine purchase restorable.)
     */
    public static String token(long seed, String sku) {
        String s = "tsp1|" + seed + "|" + sku;
        long h = 0xcbf29ce484222325L;               // FNV-1a, 64 bit
        for (int i = 0; i < s.length(); i++) {
            h ^= s.charAt(i);
            h *= 0x100000001b3L;
        }
        h ^= (h >>> 29);
        h *= 0xbf58476d1ce4e5b9L;
        h ^= (h >>> 32);
        return Long.toHexString(h);
    }

    public static boolean tokenValid(long seed, String sku, String stored) {
        return stored != null && stored.equals(token(seed, sku));
    }

    private ProGate() {
    }
}
