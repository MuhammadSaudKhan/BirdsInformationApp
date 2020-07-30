package com.material.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.material.recipe.adapter.AdapterImageList;
import com.material.recipe.data.AppConfig;
import com.material.recipe.data.Constant;
import com.material.recipe.data.DatabaseHandler;
import com.material.recipe.data.GDPR;
import com.material.recipe.data.SharedPref;
import com.material.recipe.data.ThisApplication;
import com.material.recipe.model.Images;
import com.material.recipe.model.Recipe;
import com.material.recipe.utils.Tools;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.LinearLayoutManager.*;

public class ActivityRecipeDetails extends AppCompatActivity {

    public static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";

    private Recipe recipe;
    private FloatingActionButton fab;
    private View parent_view;
    private ImageLoader imgloader = ImageLoader.getInstance();
    private DatabaseHandler db;
    private ArrayList<String> new_images_str = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        parent_view = findViewById(android.R.id.content);

        prepareAds();

        db = new DatabaseHandler(getApplicationContext());
        Tools.initImageLoader(getApplicationContext());

        recipe = (Recipe) getIntent().getSerializableExtra(EXTRA_OBJECT);
        setupToolbar(recipe.name);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fabToggle();

        ((TextView) findViewById(R.id.duration)).setText(recipe.duration + " " + getString(R.string.duration_unit));
        ((TextView) findViewById(R.id.category)).setText(recipe.category_name);
        ((TextView) findViewById(R.id.latina)).setText(recipe.latina);
        ((TextView) findViewById(R.id.muutto)).setText(recipe.muutto);
        WebView webview = (WebView) findViewById(R.id.instructions);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadDataWithBaseURL(null, recipe.instruction, "text/html; charset=UTF-8", "utf-8", null);
        ImageView image = (ImageView) findViewById(R.id.image);
        imgloader.displayImage(Constant.getURLimgRecipe(recipe.image), image);
        ImageView image2 = (ImageView) findViewById(R.id.image2);
        String url=Constant.getURLimgRecipe(recipe.image);
        imgloader.displayImage(Constant.getURLimgRecipe(recipe.image), image2);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (db.isFavoritesExist(recipe.id)) {
                    db.deleteFavorites(recipe);
                    Snackbar.make(parent_view, recipe.name + " " + getString(R.string.remove_favorites), Snackbar.LENGTH_SHORT).show();
                    // analytics tracking
                    ThisApplication.getInstance().trackEvent(Constant.Event.FAVORITES.name(), "REMOVE", recipe.name);
                } else {
                    db.addOneFavorite(recipe);
                    Snackbar.make(parent_view, recipe.name + " " + getString(R.string.add_favorites), Snackbar.LENGTH_SHORT).show();
                    // analytics tracking
                    ThisApplication.getInstance().trackEvent(Constant.Event.FAVORITES.name(), "ADD", recipe.name);
                }
                fabToggle();
            }
        });

        // analytics tracking
        ThisApplication.getInstance().trackScreenView("View recipe : " + recipe.name);
        String imagegallery=recipe.imagegallery;

        ArrayList<Images> list=new ArrayList<>();
        try {
          String[] arr=imagegallery.split(",");
            for (String imageName:
                 arr) {
               String name= imageName.replace("\"","").replace("[","").replace("]","");
                list.add(new Images(recipe.id,name));
               System.out.println(name);
            }
            setImageGallery(list);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
    private void setImageGallery(List<Images> images) {
        // add optional image into list
        List<Images> new_images = new ArrayList<>();
        new_images.add(new Images(recipe.id, recipe.image));
        new_images.addAll(images);
        new_images_str = new ArrayList<>();
        for (Images img : new_images) {
            new_images_str.add(Constant.getURLimgRecipe(img.name));
        }

        RecyclerView galleryRecycler = (RecyclerView) findViewById(R.id.galleryRecycler);
        galleryRecycler.setLayoutManager(new LinearLayoutManager(this, HORIZONTAL, false));
        AdapterImageList adapter = new AdapterImageList(new_images);
        galleryRecycler.setAdapter(adapter);
        adapter.setOnItemClickListener(new AdapterImageList.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String viewModel, int pos) {
                openImageGallery(pos);
            }
        });
    }
    private void openImageGallery(int position) {
        Intent i = new Intent(ActivityRecipeDetails.this, ActivityFullScreenImage.class);
        i.putExtra(ActivityFullScreenImage.EXTRA_POS, position);
        i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, new_images_str);
        startActivity(i);
    }
    private void setupToolbar(String name) {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(name);
        CollapsingToolbarLayout collapsing_toolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setContentScrimColor(new SharedPref(this).getThemeColorInt());
        // for system bar in lollipop
        Tools.systemBarLolipop(this);
    }

    private void fabToggle() {
        if (db.isFavoritesExist(recipe.id)) {
            fab.setImageResource(R.drawable.ic_nav_favorites);
        } else {
            fab.setImageResource(R.drawable.ic_nav_favorites_outline);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                Intent i = new Intent(getApplicationContext(), ActivitySetting.class);
                startActivity(i);
                break;
            case R.id.action_rate:
                Snackbar.make(parent_view, R.string.rate_this_app, Snackbar.LENGTH_SHORT).show();
                Tools.rateAction(this);
                break;
            case R.id.action_about:
                Tools.aboutAction(this);
                break;
            case android.R.id.home:
                finish();
                break;
            case R.id.action_share:
                Snackbar.make(parent_view, R.string.share_action, Snackbar.LENGTH_SHORT).show();
                Tools.methodShare(ActivityRecipeDetails.this, recipe);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_recipe_details, menu);
        return true;
    }

    private void prepareAds() {
        if (AppConfig.ADS_RECIPE_DETAILS_BANNER && Tools.cekConnection(getApplicationContext())) {
            AdView mAdView = (AdView) findViewById(R.id.ad_view);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(this)).build();
            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
        } else {
            ((RelativeLayout) findViewById(R.id.banner_layout)).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        if (!imgloader.isInited()) Tools.initImageLoader(getApplicationContext());
        super.onResume();
    }

}
