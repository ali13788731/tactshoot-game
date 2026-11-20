package com.tactshoot.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Whether the player owns Pro, kept in sync with the platform store. */
public final class Entitlements {

    /** Listener for the purchase / restore screen. Always called on the GL thread. */
    public interface Listener {
        void onChanged(boolean pro, String message);
    }

    private static final String PREFS = "tactshoot_store";
    private static final String K_SEED = "seed";
    private static final String K_TOKEN = "pro";

    private final Store store;
    private Preferences prefs;
    private long seed;
    private boolean pro;
    private boolean busy;

    public Entitlements(Store store) {
        this.store = store == null ? Store.NONE : store;
        try {
            prefs = Gdx.app.getPreferences(PREFS);
            seed = prefs.getLong(K_SEED, 0L);
            if (seed == 0L) {
                seed = (System.nanoTime() ^ ((long) (Math.random() * Long.MAX_VALUE))) | 1L;
                prefs.putLong(K_SEED, seed);
                prefs.flush();
            }
            pro = ProGate.tokenValid(seed, StoreConfig.PRO_SKU, prefs.getString(K_TOKEN, ""));
        } catch (Throwable t) {
            prefs = null;
        }
    }

    /** True if Pro content is usable (also true when monetization is switched off). */
    public boolean isPro() {
        return !StoreConfig.MONETIZATION_ENABLED || pro;
    }

    public boolean mapUnlocked(int mapIdx) {
        return ProGate.mapUnlocked(isPro(), mapIdx);
    }

    public boolean weaponUnlocked(int weapon) {
        return ProGate.weaponUnlocked(isPro(), weapon);
    }

    public boolean storeAvailable() {
        return store.isAvailable();
    }

    public String storeProblem() {
        return store.unavailableReason();
    }

    public boolean isBusy() {
        return busy;
    }

    /**
     * Quietly asks the store what the user owns. A confirmed purchase is
     * remembered; an empty or failed answer never takes Pro away, so a paying
     * player is not locked out by a flaky connection.
     */
    public void refresh(final Listener listener) {
        if (!StoreConfig.MONETIZATION_ENABLED || !store.isAvailable() || busy) return;
        busy = true;
        store.queryOwned(new Store.OwnedCallback() {
            @Override
            public void onOwned(final boolean owns) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        busy = false;
                        if (owns) setPro();
                        if (listener != null) {
                            listener.onChanged(isPro(), owns ? "Pro restored. Thank you!"
                                    : "No Pro purchase found for this Bazaar account.");
                        }
                    }
                });
            }

            @Override
            public void onError(final String message) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        busy = false;
                        if (listener != null) listener.onChanged(isPro(), message);
                    }
                });
            }
        });
    }

    /** Opens the Bazaar purchase screen. */
    public void buy(final Listener listener) {
        if (busy) return;
        if (!store.isAvailable()) {
            if (listener != null) listener.onChanged(isPro(), store.unavailableReason());
            return;
        }
        busy = true;
        store.purchasePro(new Store.PurchaseCallback() {
            @Override
            public void onPurchased() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        busy = false;
                        setPro();
                        if (listener != null) listener.onChanged(true, "Purchase complete. Pro unlocked!");
                    }
                });
            }

            @Override
            public void onCancelled() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        busy = false;
                        if (listener != null) listener.onChanged(isPro(), "Purchase cancelled.");
                    }
                });
            }

            @Override
            public void onError(final String message) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        busy = false;
                        if (listener != null) listener.onChanged(isPro(), message);
                    }
                });
            }
        });
    }

    private void setPro() {
        pro = true;
        if (prefs == null) return;
        try {
            prefs.putString(K_TOKEN, ProGate.token(seed, StoreConfig.PRO_SKU));
            prefs.flush();
        } catch (Throwable ignored) {
        }
    }

    private static void post(Runnable r) {
        if (Gdx.app != null) Gdx.app.postRunnable(r);
        else r.run();
    }
}
