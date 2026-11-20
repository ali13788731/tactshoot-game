package com.tactshoot.game;

/**
 * Platform billing service (Cafe Bazaar on Android, nothing on desktop).
 * Callbacks may arrive on any thread; {@link Entitlements} moves them onto the
 * GL thread before touching game state.
 */
public interface Store {

    interface OwnedCallback {
        void onOwned(boolean ownsPro);

        void onError(String message);
    }

    interface PurchaseCallback {
        void onPurchased();

        void onCancelled();

        void onError(String message);
    }

    /** True when billing can be attempted on this device (store installed, key configured). */
    boolean isAvailable();

    /** Why {@link #isAvailable()} is false, as a short player-facing sentence. */
    String unavailableReason();

    /** Asks the store whether the signed-in user already owns Pro. */
    void queryOwned(OwnedCallback cb);

    /** Starts the store's purchase screen for Pro. */
    void purchasePro(PurchaseCallback cb);

    /** Store used when the platform has no billing (desktop builds). */
    Store NONE = new Store() {
        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public String unavailableReason() {
            return "Purchases are not available on this platform.";
        }

        @Override
        public void queryOwned(OwnedCallback cb) {
            cb.onError(unavailableReason());
        }

        @Override
        public void purchasePro(PurchaseCallback cb) {
            cb.onError(unavailableReason());
        }
    };
}
