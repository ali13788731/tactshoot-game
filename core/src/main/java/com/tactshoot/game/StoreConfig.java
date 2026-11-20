package com.tactshoot.game;

/**
 * Monetization settings. This is the one file to edit before publishing.
 *
 * Model: the game and ALL online play are free. "TactShoot Pro" is a one-time
 * purchase (Cafe Bazaar in-app product) that unlocks extra content in offline
 * Practice mode. Nothing here gives an advantage in online matches.
 */
public final class StoreConfig {

    /**
     * Master switch. true  = free download + Pro purchase (default).
     *                false = everything unlocked; use this if you publish the app
     *                        as a plain PAID app (price set in the Bazaar panel).
     */
    public static final boolean MONETIZATION_ENABLED = true;

    /** Product id exactly as created in the Bazaar developer panel (one-time / non-consumable). */
    public static final String PRO_SKU = "tactshoot_pro";

    /**
     * Your app's RSA public key. Bazaar panel -> your app -> "In-app billing"
     * -> copy the key. Poolakey uses it to verify that purchases are genuine.
     */
    public static final String BAZAAR_RSA_KEY = "PASTE_YOUR_BAZAAR_RSA_PUBLIC_KEY_HERE";

    /** Public store page, appended to share texts so every shared room code is an install link. */
    public static final String STORE_URL = "https://cafebazaar.ir/app/com.tactshoot.game";

    /** True once the placeholder above has been replaced with a real key. */
    public static boolean rsaKeyConfigured() {
        return BAZAAR_RSA_KEY != null
                && BAZAAR_RSA_KEY.length() > 100
                && !BAZAAR_RSA_KEY.startsWith("PASTE_");
    }

    private StoreConfig() {
    }
}
