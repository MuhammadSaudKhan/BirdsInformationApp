package com.material.recipe.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.material.recipe.R;

public class SharedPref {
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences prefs;

    public static final int MAX_OPEN_COUNTER = 10;

    public static final String FCM_PREF_KEY = "FCM_PREF_KEY";
    public static final String SERVER_FLAG_KEY = "SERVER_FLAG_KEY";

    // need refresh
    public static final String REFRESH_RECIPE = "REFRESH_RECIPE";
    public static final String REFRESH_CATEGORY = "REFRESH_CATEGORY";

    public static final String THEME_COLOR_KEY = "THEME_COLOR_KEY";

    public static final String LAST_RECIPE_PAGE = "LAST_RECIPE_PAGE_KEY";
    public static final String LAST_CATEGORY_PAGE = "LAST_CATEGORY_PAGE_KEY";

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("MAIN_PREF", Context.MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setFcmRegId(String gcmRegId) {
        sharedPreferences.edit().putString(FCM_PREF_KEY, gcmRegId).apply();
    }

    public String getFcmRegId() {
        return sharedPreferences.getString(FCM_PREF_KEY, null);
    }

    public boolean isFcmRegIdEmpty() {
        return TextUtils.isEmpty(getFcmRegId());
    }

    /**
     * For notifications flag
     */
    public boolean getNotification() {
        return prefs.getBoolean(context.getString(R.string.pref_key_notif), true);
    }

    public String getRingtone() {
        return prefs.getString(context.getString(R.string.pref_key_ringtone), "content://settings/system/notification_sound");
    }

    public boolean getVibration() {
        return prefs.getBoolean(context.getString(R.string.pref_key_vibrate), true);
    }

    /**
     * Refresh user data
     * When phone receive GCM notification this flag will be enable.
     * so when user open the app all data will be refresh
     */
    public boolean isRefreshRecipe() {
        return sharedPreferences.getBoolean(REFRESH_RECIPE, false);
    }

    public void setRefreshRecipe(boolean need_refresh) {
        sharedPreferences.edit().putBoolean(REFRESH_RECIPE, need_refresh).apply();
    }

    public boolean isRefreshCategory() {
        return sharedPreferences.getBoolean(REFRESH_CATEGORY, false);
    }

    public void setRefreshCategory(boolean need_refresh) {
        sharedPreferences.edit().putBoolean(REFRESH_CATEGORY, need_refresh).apply();
    }

    /**
     * For theme color
     */
    public void setThemeColor(String color) {
        sharedPreferences.edit().putString(THEME_COLOR_KEY, color).apply();
    }

    public String getThemeColor() {
        return sharedPreferences.getString(THEME_COLOR_KEY, "");
    }

    public int getThemeColorInt() {
        if (getThemeColor().equals("")) {
            return context.getResources().getColor(R.color.colorPrimary);
        }
        return Color.parseColor(getThemeColor());
    }


    /**
     * To save last state request
     */
    public void setLastRecipePage(int page) {
        sharedPreferences.edit().putInt(LAST_RECIPE_PAGE, page).apply();
    }

    public int getLastRecipePage() {
        return sharedPreferences.getInt(LAST_RECIPE_PAGE, 1);
    }

    public void setLastCategoryPage(int page) {
        Log.e("TEST", "page : " + page);
        sharedPreferences.edit().putInt(LAST_CATEGORY_PAGE, page).apply();
    }

    public int getLastCategoryPage() {
        return sharedPreferences.getInt(LAST_CATEGORY_PAGE, 1);
    }

    /**
     * To save dialog permission state
     */
    public void setNeverAskAgain(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getNeverAskAgain(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    // when app open N-times it will update gcm RegID at server
    public boolean isOpenAppCounterReach() {
        int counter = sharedPreferences.getInt("OPEN_COUNTER_KEY", MAX_OPEN_COUNTER) + 1;
        setOpenAppCounter(counter);
        Log.e("COUNTER", "" + counter);
        return (counter >= MAX_OPEN_COUNTER);
    }

    public void setOpenAppCounter(int val) {
        sharedPreferences.edit().putInt("OPEN_COUNTER_KEY", val).apply();
    }

}
