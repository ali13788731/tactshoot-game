package com.tactshoot.game.android;

import android.os.Bundle;

import androidx.activity.ComponentActivity;

import com.tactshoot.game.Store;
import com.tactshoot.game.StoreConfig;

import ir.cafebazaar.poolakey.Connection;
import ir.cafebazaar.poolakey.Payment;
import ir.cafebazaar.poolakey.request.PurchaseRequest;
import kotlin.Unit;

/** Invisible activity that runs one Cafe Bazaar purchase for "TactShoot Pro". */
public class BillingActivity extends ComponentActivity {

    private Payment payment;
    private Connection connection;
    private Store.PurchaseCallback callback;
    private boolean done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callback = AndroidStore.pending;
        if (callback == null) {          // e.g. process restored after being killed
            finish();
            return;
        }
        try {
            payment = new Payment(this, AndroidStore.config());
            connection = payment.connect(c -> {
                c.connectionSucceed(() -> {
                    startPurchase();
                    return Unit.INSTANCE;
                });
                c.connectionFailed(t -> {
                    fail("Could not connect to Cafe Bazaar. Is it installed and signed in?");
                    return Unit.INSTANCE;
                });
                c.disconnected(() -> Unit.INSTANCE);
                return Unit.INSTANCE;
            });
        } catch (Throwable t) {
            fail("Could not start the purchase.");
        }
    }

    private void startPurchase() {
        try {
            // The payload is echoed back by Bazaar; it is not trusted for security.
            PurchaseRequest request = new PurchaseRequest(StoreConfig.PRO_SKU, "tactshoot-pro");
            payment.purchaseProduct(getActivityResultRegistry(), request, p -> {
                p.purchaseFlowBegan(() -> Unit.INSTANCE);
                p.failedToBeginFlow(t -> {
                    fail("Could not open the Cafe Bazaar purchase screen.");
                    return Unit.INSTANCE;
                });
                p.purchaseSucceed(info -> {
                    succeed();
                    return Unit.INSTANCE;
                });
                p.purchaseCanceled(() -> {
                    cancel();
                    return Unit.INSTANCE;
                });
                p.purchaseFailed(t -> {
                    fail("The purchase did not complete.");
                    return Unit.INSTANCE;
                });
                return Unit.INSTANCE;
            });
        } catch (Throwable t) {
            fail("Could not start the purchase.");
        }
    }

    private void succeed() {
        if (done) return;
        done = true;
        AndroidStore.pending = null;
        callback.onPurchased();
        finish();
    }

    private void cancel() {
        if (done) return;
        done = true;
        AndroidStore.pending = null;
        callback.onCancelled();
        finish();
    }

    private void fail(String message) {
        if (done) return;
        done = true;
        AndroidStore.pending = null;
        if (callback != null) callback.onError(message);
        finish();
    }

    @Override
    protected void onDestroy() {
        AndroidStore.disconnect(connection);
        // Closed some other way (back button, system): report it as cancelled.
        if (!done && callback != null && AndroidStore.pending == callback) {
            done = true;
            AndroidStore.pending = null;
            callback.onCancelled();
        }
        super.onDestroy();
    }
}
