package com.test.viewtest.activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.test.viewtest.R;
import com.test.viewtest.adapters.ImageAdapter;
import com.test.viewtest.utils.ImmerseUtil;
import com.test.viewtest.utils.LogUtil;
import com.test.viewtest.views.CoffinLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyr on 18-3-8 下午10:26.
 */

public class MainActivity extends AppCompatActivity {

    private CoffinLayout mCoffinLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.setDebugLevel(LogUtil.ERROR);
        setContentView(R.layout.act_main_view);
        initStatusBar();

        mCoffinLayout = findViewById(R.id.coffin_layout);

        View view = mCoffinLayout.getTopBar();
        if (view != null) {
            int statusBarHeight = ImmerseUtil.getStatusBarHeight(this);
            view.setPadding(0, statusBarHeight, 0, 0);
            view.getLayoutParams().height += statusBarHeight;
            mCoffinLayout.requestLayout();
        }
        view = mCoffinLayout.getResidualView();
        if (view != null) {
            view.setOnClickListener(v -> mCoffinLayout.closeCoffin());
        }
        view = mCoffinLayout.getBottomBar();
        if (view != null) {
            view.setOnClickListener(v -> Toast.makeText(this, "bottomBar clicked", Toast.LENGTH_SHORT).show());
        }

        RecyclerView topView = findViewById(R.id.top_recycler_view);
        topView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        topView.setAdapter(new ImageAdapter(this, getData(), R.layout.adapter_item_view));

        RecyclerView bottomView = findViewById(R.id.bottom_recycler_view);
        bottomView.setLayoutManager(new LinearLayoutManager(this));
        bottomView.setAdapter(new ImageAdapter(this, getData(), R.layout.adapter_item_view));

        RecyclerView horizontalView = findViewById(R.id.horizontal_recycler_view);
        horizontalView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        horizontalView.setAdapter(new ImageAdapter(this, getData(), R.layout.adapter_item_view2));
    }

    @Override
    public void onBackPressed() {
        if (mCoffinLayout.getCurrentStatus() == CoffinLayout.STATE_NAKED) {
            mCoffinLayout.closeCoffin();
        } else {
            super.onBackPressed();
        }
    }

    public List<Integer> getData() {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < 19; i++) {
            result.add(0x7f060055 + i);
        }
        return result;
    }

    private void initStatusBar() {
        //设置状态栏透明
        Window window = getWindow();
        // Translucent status bar
        window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // Translucent navigation bar
            /*window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);*/
        //android 5.0以上的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }
}
