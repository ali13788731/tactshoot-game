package com.tactshoot.game.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.tactshoot.game.Store;
import com.tactshoot.game.StoreConfig;

import ir.cafebazaar.poolakey.Connection;
import ir.cafebazaar.poolakey.Payment;
import ir.cafebazaar.poolakey.config.PaymentConfiguration;
import ir.cafebazaar.poolakey.config.SecurityCheck;
import kotlin.Unit;

/**
 * Cafe Bazaar billing through Poolakey.
 *
 * Ownership checks use a short-lived connection; the purchase itself runs in
 * {@link BillingActivity} because Poolakey needs an ActivityResultRegistry,
 * which libGDX's AndroidApplication (a plain Activity) does not provide.
 * Poolakey calls back on the UI thread; Entitlements hops to the GL thread.
 */
public final class AndroidStore implements Store {

    static final String BAZAAR_PACKAGE = "com.farsitel.bazaar";

    /** Result channel for the purchase in flight (UI thread only). */
    static PurchaseCallback pending;

    private final Activity activity;

    public AndroidStore(Activity activity) {
        this.activity = activity;
    }

    static PaymentConfiguration config() {
        return new PaymentConfiguration(new SecurityCheck.Enable(StoreConfig.BAZAAR_RSA_KEY));
    }

    private boolean bazaarInstalled() {
        try {
            activity.getPackageManager().getPackageInfo(BAZAAR_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        return StoreConfig.rsaKeyConfigured() && bazaarInstalled();
    }

    @Override
    public String unavailableReason() {
        if (!StoreConfig.rsaKeyConfigured()) return "Store is not configured in this build.";
        if (!bazaarInstalled()) return "Install Cafe Bazaar to purchase Pro.";
        return "Purchases are not available right now.";
    }

    @Override
    public void queryOwned(final OwnedCallback cb) {
        if (!isAvailable()) {
            cb.onError(unavailableReason());
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Context app = activity.getApplicationContext();
                    final Payment payment = new Payment(app, config());
                    final Connection[] conn = new Connection[1];
                    conn[0] = payment.connect(c -> {
                        c.connectionSucceed(() -> {
                            payment.getPurchasedProducts(q -> {
                                q.querySucceed(list -> {
                                    boolean owns = false;
                                    for (Object o : list) {
                                        if (isProEntity(o)) owns = true;
                                    }
                                    disconnect(conn[0]);
                                    cb.onOwned(owns);
                                    return Unit.INSTANCE;
                                });
                                q.queryFailed(t -> {
                                    disconnect(conn[0]);
                                    cb.onError("Could not read purchases from Cafe Bazaar.");
                                    return Unit.INSTANCE;
                                });
                                return Unit.INSTANCE;
                            });
                            return Unit.INSTANCE;
                        });
                        c.connectionFailed(t -> {
                            cb.onError("Could not connect to Cafe Bazaar. Is it installed and signed in?");
                            return Unit.INSTANCE;
                        });
                        c.disconnected(() -> Unit.INSTANCE);
                        return Unit.INSTANCE;
                    });
                } catch (Throwable t) {
                    cb.onError("Cafe Bazaar check failed.");
                }
            }
        });
    }

    @Override
    public void purchasePro(final PurchaseCallback cb) {
        if (!isAvailable()) {
            cb.onError(unavailableReason());
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pending != null) {
                    cb.onError("A purchase is already in progress.");
                    return;
                }
                pending = cb;
                try {
                    activity.startActivity(new Intent(activity, BillingActivity.class));
                } catch (Throwable t) {
                    pending = null;
                    cb.onError("Could not open the Cafe Bazaar purchase screen.");
                }
            }
        });
    }

    static void disconnect(Connection c) {
        try {
            if (c != null) c.disconnect();
        } catch (Throwable ignored) {
        }
    }

    /** Poolakey's purchase entity type differs between versions, so read productId reflectively. */
    private static boolean isProEntity(Object entity) {
        try {
            Object id = entity.getClass().getMethod("getProductId").invoke(entity);
            return StoreConfig.PRO_SKU.equals(id);
        } catch (Throwable t) {
            return String.valueOf(entity).contains(StoreConfig.PRO_SKU);
        }
    }
}
