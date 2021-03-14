package co.appengine.games.sudokuland.utils;

import android.app.Activity;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static co.appengine.games.sudokuland.utils.Constants.SKU_ID;
import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

/**
 * Created by cristhiangomezmayor on 31/10/17.
 */

public class BillingManager {
    private static final String TAG = "BillingManager";

    private final BillingClient mBillingClient;
    private SkuDetailsResponseListener listener;
    private PurchasesUpdatedListener updatedListener;
    private final Activity mActivity;

    // Defining SKU constants from Google Play Developer Console
    private static final HashMap<String, List<String>> SKUS;

    static {
        SKUS = new HashMap<>();
        SKUS.put(INAPP, Arrays.asList(SKU_ID));
    }

    public BillingManager(Activity activity, SkuDetailsResponseListener listener, PurchasesUpdatedListener updatedListener) {
        mActivity = activity;
        this.updatedListener = updatedListener;
        mBillingClient = BillingClient.newBuilder(mActivity).setListener(this.updatedListener).build();
        this.listener = listener;
        startServiceConnectionIfNeeded(null);
    }

    /**
     * Trying to restart service connection if it's needed or just execute a request.
     * <p>Note: It's just a primitive example - it's up to you to implement a real retry-policy.</p>
     * @param executeOnSuccess This runnable will be executed once the connection to the Billing
     *                         service is restored.
     */
    private void startServiceConnectionIfNeeded(final Runnable executeOnSuccess) {
        if (mBillingClient.isReady()) {
            if (executeOnSuccess != null) {
                executeOnSuccess.run();
            }
        } else {
            mBillingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponse) {
                    if (billingResponse == BillingClient.BillingResponse.OK) {
                        Log.i(TAG, "onBillingSetupFinished() response: " + billingResponse);
                        Log.d("Sku List", getSkus(BillingClient.SkuType.INAPP) + " ");
                        querySkuDetailsAsync(BillingClient.SkuType.INAPP, getSkus(BillingClient.SkuType.INAPP));
                    } else {
                        Log.w(TAG, "onBillingSetupFinished() error code: " + billingResponse);
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    Log.w(TAG, "onBillingServiceDisconnected()");
                }
            });
        }
    }

    public void querySkuDetailsAsync(@BillingClient.SkuType final String itemType,
                                     final List<String> skuList) {
        // Specify a runnable to start when connection to Billing client is established
        SkuDetailsParams skuDetailsParams = SkuDetailsParams.newBuilder()
                .setSkusList(skuList).setType(itemType).build();
        mBillingClient.querySkuDetailsAsync(skuDetailsParams,
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode,
                                                     List<SkuDetails> skuDetailsList) {
                        listener.onSkuDetailsResponse(responseCode, skuDetailsList);
                        Log.d("queruSkuDetails", "Response code: " + responseCode);
                    }
                });

        // If Billing client was disconnected, we retry 1 time and if success, execute the query
        //startServiceConnectionIfNeeded(executeOnConnectedService);
    }

    public List<String> getSkus(@BillingClient.SkuType String type) {
        return SKUS.get(type);
    }

    public void startPurchaseFlow(final String skuId, final String billingType) {
        // Specify a runnable to start when connection to Billing client is established
        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setType(billingType)
                        .setSku(skuId)
                        .build();
                mBillingClient.launchBillingFlow(mActivity, billingFlowParams);
            }
        };

        // If Billing client was disconnected, we retry 1 time and if success, execute the query
        startServiceConnectionIfNeeded(executeOnConnectedService);
    }

    public BillingClient getmBillingClient(){
        return this.mBillingClient;
    }

    public void destroy() {
        mBillingClient.endConnection();
    }
}
