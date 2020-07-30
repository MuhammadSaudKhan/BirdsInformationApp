package com.material.recipe;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.material.recipe.adapter.CategoryListAdapter;
import com.material.recipe.adapter.RecipeGridAdapter;
import com.material.recipe.data.DatabaseHandler;
import com.material.recipe.model.Category;
import com.material.recipe.model.Recipe;
import com.material.recipe.utils.SpacingItemDecoration;
import com.material.recipe.utils.Tools;

import java.util.ArrayList;

public class ActivitySearch extends AppCompatActivity {

    public static final String EXTRA_TYPE = "SEARCH_EXTRA_TYPE";
    public static final String TYPE_CATEGORY = "TYPE_CATEGORY";
    public static final String TYPE_RECIPE = "TYPE_RECIPE";

    private Toolbar toolbar;
    private ActionBar actionBar;
    private EditText et_search;
    private RecyclerView recyclerView;
    private RecipeGridAdapter rAdapter;
    private CategoryListAdapter cAdapter;
    private ImageButton bt_clear;
    private View parent_view;

    private DatabaseHandler db;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        parent_view = findViewById(android.R.id.content);
        db = new DatabaseHandler(this);
        type = getIntent().getStringExtra(EXTRA_TYPE);

        initComponent();
        setupToolbar();
    }

    private void initComponent() {
        et_search = (EditText) findViewById(R.id.et_search);
        et_search.addTextChangedListener(textWatcher);
        et_search.setHint(type.equals(TYPE_RECIPE) ? R.string.hint_input_recipe : R.string.hint_input_category);

        bt_clear = (ImageButton) findViewById(R.id.bt_clear);
        bt_clear.setVisibility(View.GONE);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        if (type.equals(TYPE_RECIPE)) {

            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(Tools.getGridSpanCount(this), StaggeredGridLayoutManager.VERTICAL));
            recyclerView.addItemDecoration(new SpacingItemDecoration(Tools.getGridSpanCount(this), Tools.dpToPx(this, 6), true));
            recyclerView.setHasFixedSize(true);
            //set data and list adapter
            rAdapter = new RecipeGridAdapter(this, recyclerView, new ArrayList<Recipe>());
            recyclerView.setAdapter(rAdapter);
            rAdapter.setOnItemClickListener(new RecipeGridAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, Recipe obj, int position) {
                    Intent i = new Intent(ActivitySearch.this, ActivityRecipeDetails.class);
                    i.putExtra(ActivityRecipeDetails.EXTRA_OBJECT, obj);
                    startActivity(i);
                }
            });
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addItemDecoration(new SpacingItemDecoration(1, Tools.dpToPx(this, 6), true));
            recyclerView.setHasFixedSize(true);
            cAdapter = new CategoryListAdapter(this, recyclerView, new ArrayList<Category>());
            recyclerView.setAdapter(cAdapter);
            cAdapter.setOnItemClickListener(new CategoryListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, Category obj, int position) {
                    Intent i = new Intent(ActivitySearch.this, ActivityCategoryDetails.class);
                    i.putExtra(ActivityCategoryDetails.EXTRA_OBJECT, obj);
                    startActivity(i);
                }
            });
        }

        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
                if (type.equals(TYPE_RECIPE)) {
                    rAdapter.resetListData();
                } else {
                    cAdapter.resetListData();
                }
                showNotFoundView();
            }
        });

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    searchAction();
                    return true;
                }
                return false;
            }
        });

        showNotFoundView();
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // for system bar in lollipop
        if (Tools.isLolipopOrHigher()) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.BLACK);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                bt_clear.setVisibility(View.GONE);
            } else {
                bt_clear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void searchAction() {
        showNotFoundView();
        final String query = et_search.getText().toString().trim();
        if (!query.equals("")) {
            if (type.equals(TYPE_RECIPE)) {
                rAdapter.resetListData();
                rAdapter.insertData(db.searchRecipes(query));
            } else {
                cAdapter.resetListData();
                cAdapter.insertData(db.searchCategories(query));
            }
            showNotFoundView();
        } else {
            Toast.makeText(this, R.string.please_fill_input, Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotFoundView() {
        View lyt_no_item = (View) findViewById(R.id.lyt_no_item);
        boolean is_zero = (type.equals(TYPE_RECIPE) ? rAdapter.getItemCount() <= 0 : cAdapter.getItemCount() <= 0);
        if (is_zero) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }
}
