package com.material.recipe.data;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.material.recipe.R;
import com.material.recipe.connection.API;
import com.material.recipe.connection.RestAdapter;
import com.material.recipe.connection.callbacks.CallbackDevice;
import com.material.recipe.model.DeviceInfo;
import com.material.recipe.utils.Tools;

import retrofit2.Call;
import retrofit2.Response;

public class ThisApplication extends Application {

    private Call<CallbackDevice> callback = null;
    private static ThisApplication mInstance;
    private SharedPref sharedPref;
    private FirebaseAnalytics firebaseAnalytics;

    private int fcm_count = 0;
    private final int FCM_MAX_COUNT = 10;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Constant.LOG_TAG, "onCreate : ThisApplication");
        mInstance = this;
        sharedPref = new SharedPref(this);

        // initialize firebase
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);

        // Init firebase ads.
        MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));

        // obtain regId & registering device to server
        obtainFirebaseToken(firebaseApp);

        //init image loader
        Tools.initImageLoader(getApplicationContext());

        // activate analytics tracker
        getFirebaseAnalytics();
    }

    public static synchronized ThisApplication getInstance() {
        return mInstance;
    }

    private void obtainFirebaseToken(final FirebaseApp firebaseApp) {
        if (!sharedPref.isOpenAppCounterReach() || !Tools.cekConnection(this)) return;
        fcm_count++;

        Task<InstanceIdResult> resultTask = FirebaseInstanceId.getInstance().getInstanceId();
        resultTask.addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String regId = instanceIdResult.getToken();
                sharedPref.setFcmRegId(regId);
                if (!TextUtils.isEmpty(regId)) sendRegistrationToServer(regId);
            }
        });

        resultTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(Constant.LOG_TAG, "Failed obtain fcmID : " + e.getMessage());
                if (fcm_count > FCM_MAX_COUNT) return;
                obtainFirebaseToken(firebaseApp);
            }
        });
    }

    /**
     * --------------------------------------------------------------------------------------------
     * For Firebase Cloud Messaging
     */
    private void sendRegistrationToServer(String token) {
        if (Tools.cekConnection(this) && !TextUtils.isEmpty(token)) {
            API api = RestAdapter.createAPI();
            DeviceInfo deviceInfo = Tools.getDeviceInfo(this);
            deviceInfo.setRegid(token);

            callback = api.registerDevice(deviceInfo);
            callback.enqueue(new retrofit2.Callback<CallbackDevice>() {
                @Override
                public void onResponse(Call<CallbackDevice> call, Response<CallbackDevice> response) {
                    CallbackDevice resp = response.body();
                    if (resp.status.equals("success")) {
                        sharedPref.setOpenAppCounter(0);
                    }
                }

                @Override
                public void onFailure(Call<CallbackDevice> call, Throwable t) {
                }
            });
        }
    }

    /**
     * --------------------------------------------------------------------------------------------
     * For Google Analytics
     */
    public synchronized FirebaseAnalytics getFirebaseAnalytics() {
        if (firebaseAnalytics == null && AppConfig.ENABLE_ANALYTICS) {
            // Obtain the Firebase Analytics.
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }
        return firebaseAnalytics;
    }

    public void trackScreenView(String event) {
        if (firebaseAnalytics == null || !AppConfig.ENABLE_ANALYTICS) return;
        Bundle params = new Bundle();
        event = event.replaceAll("[^A-Za-z0-9_]", "");
        params.putString("event", event);
        firebaseAnalytics.logEvent(event, params);
    }

    public void trackEvent(String category, String action, String label) {
        if (firebaseAnalytics == null || !AppConfig.ENABLE_ANALYTICS) return;
        Bundle params = new Bundle();
        category = category.replaceAll("[^A-Za-z0-9_]", "");
        action = action.replaceAll("[^A-Za-z0-9_]", "");
        label = label.replaceAll("[^A-Za-z0-9_]", "");
        params.putString("category", category);
        params.putString("action", action);
        params.putString("label", label);
        firebaseAnalytics.logEvent("EVENT", params);
    }

    /** ---------------------------------------- End of analytics --------------------------------- */
}
