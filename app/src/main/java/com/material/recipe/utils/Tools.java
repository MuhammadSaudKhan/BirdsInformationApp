package com.material.recipe.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.material.recipe.ActivityMain;
import com.material.recipe.ActivitySplash;
import com.material.recipe.R;
import com.material.recipe.data.AppConfig;
import com.material.recipe.data.SharedPref;
import com.material.recipe.model.DeviceInfo;
import com.material.recipe.model.Recipe;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class Tools {
    public static float getAPIVerison() {

        Float f = null;
        try {
            StringBuilder strBuild = new StringBuilder();
            strBuild.append(android.os.Build.VERSION.RELEASE.substring(0, 2));
            f = new Float(strBuild.toString());
        } catch (NumberFormatException e) {
            Log.e("", "Error get api version " + e.getMessage());
        }

        return f.floatValue();
    }

    public static boolean needRequestPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public static boolean isLolipopOrHigher() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public static void systemBarLolipop(Activity act) {
        if (isLolipopOrHigher()) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Tools.colorDarker(new SharedPref(act).getThemeColorInt()));
        }
    }

    public static boolean cekConnection(Context context) {
        try {
            ConnectionDetector conn = new ConnectionDetector(context);
            if (conn.isConnectingToInternet()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

    }

    public static void initImageLoader(Context context) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(AppConfig.IMAGE_CACHE)
                .cacheOnDisk(AppConfig.IMAGE_CACHE)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(options)
                .threadPoolSize(3)
                .memoryCache(new WeakMemoryCache())
                .build();

        ImageLoader.getInstance().init(config);
    }

    public static DisplayImageOptions getGridOption() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(AppConfig.IMAGE_CACHE)
                .cacheOnDisk(AppConfig.IMAGE_CACHE)
                .build();

        return options;
    }

    public static void directBrowser(Activity activity, String url) {
        if (!URLUtil.isValidUrl(url)) {
            Toast.makeText(activity, "Ops, Cannot open url", Toast.LENGTH_LONG).show();
            return;
        }
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public static void rateAction(Activity activity) {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static String getVersionName(Context ctx) {
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            return ctx.getString(R.string.version) + " " + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }

    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE + "";
    }

    public static int getGridSpanCount(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.recycler_item_size);
        return Math.round(screenWidth / cellWidth);
    }

    public static void methodShare(Activity act, Recipe r) {

        StringBuilder sb = new StringBuilder();
        // string to share
        sb.append("Mielenkiintoinenlintu : \'" + r.name + "\'\n");
        sb.append("Sovelluksessa " + act.getString(R.string.app_name) + "\n\n");
        sb.append("http://play.google.com/store/apps/details?id=" + act.getPackageName());

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, act.getString(R.string.app_name));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
        act.startActivity(Intent.createChooser(sharingIntent, "Share Using"));
    }

    public static void aboutAction(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_about);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        ((TextView) dialog.findViewById(R.id.text_version)).setText(Tools.getVersionName(activity));
        ((Button) dialog.findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public static void setActionBarColor(Context ctx, ActionBar actionbar) {
        ColorDrawable colordrw = new ColorDrawable(new SharedPref(ctx).getThemeColorInt());
        actionbar.setBackgroundDrawable(colordrw);
    }

    public static int colorDarker(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static int colorBrighter(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] /= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static void restartApplication(Activity activity) {
        activity.finish();
        ActivityMain.getInstance().finish();
        Intent i = new Intent(activity, ActivitySplash.class);
        activity.startActivity(i);
    }

    // object to JSON converter
    public static DeviceInfo getDeviceInfo(Context context) {
        String phoneID = Build.SERIAL;
        try {
            phoneID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
        }
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDevice(Tools.getDeviceName());
        deviceInfo.setEmail(phoneID);
        deviceInfo.setVersion(Tools.getAndroidVersion());
        deviceInfo.setRegid(new SharedPref(context).getFcmRegId());
        deviceInfo.setDate_create(System.currentTimeMillis());

        return deviceInfo;
    }

    public interface CallbackRegId {
        void onSuccess(DeviceInfo result);

        void onError();
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
