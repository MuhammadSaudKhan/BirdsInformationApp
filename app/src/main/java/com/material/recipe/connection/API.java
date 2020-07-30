package com.material.recipe.connection;

import com.material.recipe.connection.callbacks.CallbackDevice;
import com.material.recipe.connection.callbacks.CallbackListCategory;
import com.material.recipe.connection.callbacks.CallbackListRecipe;
import com.material.recipe.model.DeviceInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface API {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "User-Agent: Recipe";

    /* Recipe API transaction ------------------------------- */

    @Headers({CACHE, AGENT})
    @GET("app/services/listRecipes")
    Call<CallbackListRecipe> getRecipesByPage(
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("app/services/listCategories")
    Call<CallbackListCategory> getCategoriesByPage(
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @POST("app/services/insertGcm")
    Call<CallbackDevice> registerDevice(
            @Body DeviceInfo deviceInfo
    );

}
