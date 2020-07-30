package com.material.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.material.recipe.adapter.RecipeGridAdapter;
import com.material.recipe.data.Constant;
import com.material.recipe.data.DatabaseHandler;
import com.material.recipe.data.SharedPref;
import com.material.recipe.data.ThisApplication;
import com.material.recipe.model.Category;
import com.material.recipe.model.Recipe;
import com.material.recipe.utils.SpacingItemDecoration;
import com.material.recipe.utils.Tools;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ActivityCategoryDetails extends AppCompatActivity {

    public static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";

    private RecyclerView recyclerView;
    private RecipeGridAdapter mAdapter;
    private Category category;
    private View parent_view;
    private ViewGroup lyt_not_found;
    private DatabaseHandler db;
    private ImageLoader imgloader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_details);

        parent_view = findViewById(android.R.id.content);
        db = new DatabaseHandler(getApplicationContext());
        category = (Category) getIntent().getSerializableExtra(EXTRA_OBJECT);

        initComponent();
        setupToolbar();

        mAdapter = new RecipeGridAdapter(this, recyclerView, new ArrayList<Recipe>());
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new RecipeGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Recipe obj, int position) {
                Intent i = new Intent(getApplicationContext(), ActivityRecipeDetails.class);
                i.putExtra(ActivityRecipeDetails.EXTRA_OBJECT, obj);
                startActivity(i);
            }
        });

        startLoadMoreAdapter();

        // analytics tracking
        ThisApplication.getInstance().trackScreenView("View category : " + category.name);
    }

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");
        final CollapsingToolbarLayout collapsing_toolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setContentScrimColor(new SharedPref(this).getThemeColorInt());
        // for system bar in lollipop
        Tools.systemBarLolipop(this);
    }

    private void initComponent() {
        Tools.initImageLoader(getApplicationContext());
        lyt_not_found = (ViewGroup) findViewById(R.id.lyt_not_found);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(Tools.getGridSpanCount(this), StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(new SpacingItemDecoration(Tools.getGridSpanCount(this), Tools.dpToPx(this, 6), true));
        recyclerView.setHasFixedSize(true);

        ((TextView) findViewById(R.id.name)).setText(category.name);
        ((TextView) findViewById(R.id.description)).setText(category.description);
        ImageView image = (ImageView) findViewById(R.id.image);
        imgloader.displayImage(Constant.getURLimgCategory(category.banner), image);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_category_details, menu);
        return true;
    }


    @Override
    protected void onResume() {
        if (!imgloader.isInited()) Tools.initImageLoader(getApplicationContext());
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }

    private void showNoItemView() {
        if (mAdapter.getItemCount() == 0) {
            lyt_not_found.setVisibility(View.VISIBLE);
        } else {
            lyt_not_found.setVisibility(View.GONE);
        }
    }

    private void startLoadMoreAdapter() {
        mAdapter.resetListData();
        mAdapter.insertData(db.getRecipesByCategoryId(category, Constant.LIMIT_LOADMORE, 0));
        showNoItemView();
        final int item_count = db.getRecipesByCategoryIdCount(category);
        // detect when scroll reach bottom
        mAdapter.setOnLoadMoreListener(new RecipeGridAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(final int current_page) {
                if (item_count > mAdapter.getItemCount() && current_page != 0) {
                    displayDataByPage(current_page);
                } else {
                    mAdapter.setLoaded();
                }
            }
        });
    }

    private void displayDataByPage(final int next_page) {
        mAdapter.setLoading();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Recipe> posts = db.getRecipesByCategoryId(category, Constant.LIMIT_LOADMORE, (next_page * Constant.LIMIT_LOADMORE));
                mAdapter.insertData(posts);
                showNoItemView();
            }
        }, 500);
    }
}
