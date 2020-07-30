package com.material.recipe.data;

public class AppConfig {

    // if you not use ads you can set this to false
    public static final boolean ENABLE_GDPR = true;

    // flag for display ads
    public static final boolean ADS_MAIN_INTERSTITIAL = false;
    public static final boolean ADS_MAIN_BANNER = true;
    public static final boolean ADS_RECIPE_DETAILS_BANNER = true;

    // flag for save image offline
    public static final boolean IMAGE_CACHE = true;

    // flag for tracking analytics
    public static final boolean ENABLE_ANALYTICS = true;

    // clear image cache when receive push notifications
    public static final boolean REFRESH_IMG_NOTIF = true;

    // refresh data when receive push notifications
    public static final boolean REFRESH_DATA_NOTIF = true;

}
