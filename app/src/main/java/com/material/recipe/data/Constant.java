package com.material.recipe.data;

public class Constant {

    // Edit WEB_URL with your url. Make sure you have backslash('/') in the end url
    public static String WEB_URL = "http://vps622308.ovh.net/lintuopas/";

    //DON'T EDIT ANY CODE BELOW --------------------------------------------------------------------
    public static String getURLimgRecipe(String file_name) {
        String URL = WEB_URL + IMG_PATH_RECIPE + "/" + file_name;
        return URL;
    }
    // image file url
    public static String getURLimgPlace(String file_name) {
        return WEB_URL + IMG_PATH_RECIPE + file_name;
    }
    public static String getURLimgCategory(String file_name) {
        String URL = WEB_URL + IMG_PATH_CATEGORY + "/" + file_name;
        return URL;
    }

    public static String IMG_PATH_RECIPE = "uploads/recipe";
    public static String IMG_PATH_CATEGORY = "uploads/category";

    // for search logs Tag
    public static final String LOG_TAG = "RECIPE_LOG";

    // Google analytics event category
    public enum Event {
        FAVORITES,
        THEME,
        NOTIFICATION,
        REFRESH
    }

    // this limit value used for give pagination (request and display) to decrease payload
    public static final int LIMIT_RECIPE_REQUEST = 100;
    public static final int LIMIT_CATEGORY_REQUEST = 100;
    public static final int LIMIT_LOADMORE = 50;

}
