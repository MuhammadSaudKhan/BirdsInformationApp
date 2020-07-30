package com.material.recipe.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdView;
import com.material.recipe.ActivityMain;
import com.material.recipe.ActivityRecipeDetails;
import com.material.recipe.R;
import com.material.recipe.adapter.RecipeGridAdapter;
import com.material.recipe.data.AppConfig;
import com.material.recipe.data.DatabaseHandler;
import com.material.recipe.model.Recipe;
import com.material.recipe.utils.SpacingItemDecoration;
import com.material.recipe.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {
    private View view;
    private RecyclerView recyclerView;
    private RecipeGridAdapter mAdapter;
    private ViewGroup lyt_not_found;
    private DatabaseHandler db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favorites, null);
        db = new DatabaseHandler(getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        lyt_not_found = (ViewGroup) view.findViewById(R.id.lyt_not_found);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(Tools.getGridSpanCount(getActivity()), StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(new SpacingItemDecoration(Tools.getGridSpanCount(getActivity()), Tools.dpToPx(getActivity(), 6), true));
        recyclerView.setHasFixedSize(true);

        mAdapter = new RecipeGridAdapter(getActivity(), recyclerView, new ArrayList<Recipe>());
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new RecipeGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Recipe obj, int position) {
                Intent i = new Intent(getActivity(), ActivityRecipeDetails.class);
                i.putExtra(ActivityRecipeDetails.EXTRA_OBJECT, obj);
                startActivity(i);
            }
        });

        displayData();
        ActivityMain.getFab().hide();
        return view;
    }

    private void displayData() {
        List<Recipe> list = db.getAllFavorites();
        mAdapter.setItems(list);
        if (list.size() == 0) {
            lyt_not_found.setVisibility(View.VISIBLE);
        } else {
            lyt_not_found.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        displayData();
    }

}
