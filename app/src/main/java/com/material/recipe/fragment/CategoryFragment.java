package com.material.recipe.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.material.recipe.ActivityCategoryDetails;
import com.material.recipe.ActivityMain;
import com.material.recipe.ActivitySearch;
import com.material.recipe.R;
import com.material.recipe.adapter.CategoryListAdapter;
import com.material.recipe.connection.RestAdapter;
import com.material.recipe.connection.callbacks.CallbackListCategory;
import com.material.recipe.data.AppConfig;
import com.material.recipe.data.Constant;
import com.material.recipe.data.DatabaseHandler;
import com.material.recipe.data.SharedPref;
import com.material.recipe.data.ThisApplication;
import com.material.recipe.model.Category;
import com.material.recipe.utils.SpacingItemDecoration;
import com.material.recipe.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFragment extends Fragment {

    private int count_total = 0;

    public RecyclerView recyclerView;
    public CategoryListAdapter mAdapter;

    private View view;
    private Snackbar snackbar_retry;
    private ViewGroup lyt_not_found;
    private ViewGroup lyt_progress;
    private TextView text_progress;

    private DatabaseHandler db;
    private SharedPref sharedPref;
    private Call<CallbackListCategory> callback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_category, null);
        // activate fragment menu
        setHasOptionsMenu(true);
        db = new DatabaseHandler(getActivity());
        sharedPref = new SharedPref(getActivity());

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        lyt_not_found = (ViewGroup) view.findViewById(R.id.lyt_not_found);
        lyt_progress = (ViewGroup) view.findViewById(R.id.lyt_progress);
        text_progress = (TextView) view.findViewById(R.id.text_progress);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new SpacingItemDecoration(1, Tools.dpToPx(getActivity(), 6), true));

        recyclerView.setHasFixedSize(true);
        ActivityMain.getFab().show();

        //set data and list adapter
        mAdapter = new CategoryListAdapter(getActivity(), recyclerView, new ArrayList<Category>());
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new CategoryListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Category obj, int position) {
                Intent i = new Intent(getActivity(), ActivityCategoryDetails.class);
                i.putExtra(ActivityCategoryDetails.EXTRA_OBJECT, obj);
                startActivity(i);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView v, int state) {
                super.onScrollStateChanged(v, state);
                if (state == v.SCROLL_STATE_DRAGGING || state == v.SCROLL_STATE_SETTLING) {
                    ActivityMain.animateFab(true);
                } else {
                    ActivityMain.animateFab(false);
                }
            }
        });

        ActivityMain.getFab().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ActivitySearch.class);
                i.putExtra(ActivitySearch.EXTRA_TYPE, ActivitySearch.TYPE_CATEGORY);
                startActivity(i);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (sharedPref.isRefreshCategory() || db.getCategoriesCount() == 0) {
            actionRefresh(sharedPref.getLastCategoryPage());
        } else {
            startLoadMoreAdapter();
        }
    }

    @Override
    public void onResume() {
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        if (snackbar_retry != null) snackbar_retry.dismiss();
        if (callback != null && callback.isExecuted()) {
            callback.cancel();
        }
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_category, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            sharedPref.setLastCategoryPage(1);
            sharedPref.setRefreshCategory(true);
            text_progress.setText("Hae uudet");
            if (snackbar_retry != null) snackbar_retry.dismiss();
            actionRefresh(sharedPref.getLastCategoryPage());
            // analytics tracking
            ThisApplication.getInstance().trackEvent(Constant.Event.REFRESH.name(), "CATEGORY", "-");
        }
        return super.onOptionsItemSelected(item);
    }

    private void startLoadMoreAdapter() {
        mAdapter.resetListData();
        mAdapter.insertData(db.getCategoriesByPage(Constant.LIMIT_LOADMORE, 0));
        showNoItemView();
        final int item_count = (int) db.getCategoriesCount();
        // detect when scroll reach bottom
        mAdapter.setOnLoadMoreListener(new CategoryListAdapter.OnLoadMoreListener() {
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
                List<Category> posts = db.getCategoriesByPage(Constant.LIMIT_LOADMORE, (next_page * Constant.LIMIT_LOADMORE));
                mAdapter.insertData(posts);
                showNoItemView();
            }
        }, 500);
    }

    private void actionRefresh(int page_no) {
        boolean conn = Tools.cekConnection(getActivity());
        if (conn) {
            if (!onProcess) {
                onRefresh(page_no);
            } else {
                Snackbar.make(view, R.string.task_running, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            onFailureRetry(page_no, getString(R.string.no_internet));
        }
    }

    private boolean onProcess = false;

    private void onRefresh(final int page_no) {
        onProcess = true;
        showProgress(onProcess);
        callback = RestAdapter.createAPI().getCategoriesByPage(page_no, Constant.LIMIT_CATEGORY_REQUEST);
        callback.enqueue(new Callback<CallbackListCategory>() {
            @Override
            public void onResponse(Call<CallbackListCategory> call, Response<CallbackListCategory> response) {
                CallbackListCategory resp = response.body();
                if (resp != null) {
                    count_total = resp.count_total;
                    if (page_no == 1) db.truncateTableCategory();
                    db.addListCategory(resp.categories);
                    sharedPref.setLastCategoryPage(page_no + 1);
                    delayNextRequest(page_no);
                    String str_progress = String.format(getString(R.string.load_of), (page_no * Constant.LIMIT_CATEGORY_REQUEST), count_total);
                    text_progress.setText(str_progress);
                } else {
                    onFailureRetry(page_no, getString(R.string.refresh_failed));
                }
            }

            @Override
            public void onFailure(Call<CallbackListCategory> call, Throwable t) {
                if (call != null && !call.isCanceled()) {
                    Log.e("onFailure", t.getMessage());
                    boolean conn = Tools.cekConnection(getActivity());
                    if (conn) {
                        onFailureRetry(page_no, getString(R.string.refresh_failed));
                    } else {
                        onFailureRetry(page_no, getString(R.string.no_internet));
                    }
                }
            }
        });
    }

    private void showProgress(boolean show) {
        if (show) {
            lyt_progress.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            lyt_not_found.setVisibility(View.GONE);
        } else {
            lyt_progress.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showNoItemView() {
        if (mAdapter.getItemCount() == 0) {
            lyt_not_found.setVisibility(View.VISIBLE);
        } else {
            lyt_not_found.setVisibility(View.GONE);
        }
    }

    private void onFailureRetry(final int page_no, String msg) {
        onProcess = false;
        showProgress(onProcess);
        showNoItemView();
        startLoadMoreAdapter();
        snackbar_retry = Snackbar.make(view, msg, Snackbar.LENGTH_INDEFINITE);
        snackbar_retry.setAction(R.string.RETRY, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionRefresh(page_no);
            }
        });
        snackbar_retry.show();
    }

    private void delayNextRequest(final int page_no) {
        if (count_total == 0) {
            onFailureRetry(page_no, getString(R.string.refresh_failed));
            return;
        }
        if ((page_no * Constant.LIMIT_CATEGORY_REQUEST) > count_total) {
            onProcess = false;
            showProgress(onProcess);
            startLoadMoreAdapter();
            sharedPref.setRefreshCategory(false);
            text_progress.setText("");
            Snackbar.make(view, R.string.data_success_loaded, Snackbar.LENGTH_LONG).show();
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onRefresh(page_no + 1);
            }
        }, 100);
    }
}
