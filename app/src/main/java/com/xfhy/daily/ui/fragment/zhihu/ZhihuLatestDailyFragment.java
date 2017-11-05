package com.xfhy.daily.ui.fragment.zhihu;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.trello.rxlifecycle2.LifecycleTransformer;
import com.xfhy.androidbasiclibs.basekit.fragment.BaseMVPFragment;
import com.xfhy.androidbasiclibs.common.util.DensityUtil;
import com.xfhy.androidbasiclibs.common.util.DevicesUtils;
import com.xfhy.androidbasiclibs.common.util.GlideUtils;
import com.xfhy.androidbasiclibs.common.util.LogUtils;
import com.xfhy.androidbasiclibs.common.util.SnackbarUtil;
import com.xfhy.androidbasiclibs.uihelper.adapter.BaseQuickAdapter;
import com.xfhy.androidbasiclibs.uihelper.widget.EasyBanner;
import com.xfhy.androidbasiclibs.uihelper.widget.StatefulLayout;
import com.xfhy.daily.R;
import com.xfhy.daily.network.entity.zhihu.LatestDailyListBean;
import com.xfhy.daily.network.entity.zhihu.PastNewsBean;
import com.xfhy.daily.presenter.ZhihuDailyLatestContract;
import com.xfhy.daily.presenter.impl.ZhihuDailyLatestPresenter;
import com.xfhy.daily.ui.adapter.ZhihuLatestDailyAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * author feiyang
 * create at 2017/9/30 16:38
 * description：知乎最新日报fragment
 */
public class ZhihuLatestDailyFragment extends BaseMVPFragment<ZhihuDailyLatestPresenter>
        implements ZhihuDailyLatestContract.View, SwipeRefreshLayout.OnRefreshListener,
        BaseQuickAdapter.RequestLoadMoreListener, BaseQuickAdapter.OnItemClickListener,
        EasyBanner.OnItemClickListener {

    @BindView(R.id.sl_state_view)
    StatefulLayout mStateView;
    @BindView(R.id.srl_refresh_layout)
    SwipeRefreshLayout mRefreshLayout;
    @BindView(R.id.rv_latest_daily_list)
    RecyclerView mDailyRecyclerView;

    private ZhihuLatestDailyAdapter mDailyAdapter;
    /**
     * 过去的天数
     */
    private int pastDays = 1;
    /**
     * 顶部轮播图
     */
    private EasyBanner mBanner;
    /**
     * banner所占高度
     */
    private static final int BANNER_HEIGHT = 200;

    public static ZhihuLatestDailyFragment newInstance() {

        Bundle args = new Bundle();

        ZhihuLatestDailyFragment fragment = new ZhihuLatestDailyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initPresenter() {
        mPresenter = new ZhihuDailyLatestPresenter(mActivity);
    }

    @Override
    protected void initViewEvent() {
        mRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_zhihu_latest_daily;
    }

    @Override
    protected void initView() {
        //下拉刷新颜色
        mRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        mDailyAdapter = new ZhihuLatestDailyAdapter(mActivity,
                null);
        // 开启RecyclerView动画
        mDailyAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM);
        // 循环播放动画
        mDailyAdapter.isFirstOnly(false);
        // 可加载更多
        mDailyAdapter.setEnableLoadMore(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        mDailyRecyclerView.setLayoutManager(linearLayoutManager);
        mDailyRecyclerView.setAdapter(mDailyAdapter);
        //设置加载更多监听器
        mDailyAdapter.setOnLoadMoreListener(this, mDailyRecyclerView);
        // 当未满一屏幕时不刷新
        mDailyAdapter.disableLoadMoreIfNotFullPage();
        // 设置RecyclerView的item监听
        mDailyAdapter.setOnItemClickListener(this);
        initBanner();
    }

    /**
     * 初始化banner
     */
    private void initBanner() {
        // 动态生成banner
        mBanner = new EasyBanner(mActivity);
        // 设置banner的大小
        LinearLayout.LayoutParams bannerLayoutParams = new LinearLayout.LayoutParams(LinearLayout
                .LayoutParams.MATCH_PARENT, DensityUtil.dip2px(mActivity, BANNER_HEIGHT));
        mBanner.setLayoutParams(bannerLayoutParams);
        //设置banner图片加载器
        mBanner.setImageLoader(new EasyBanner.ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                GlideUtils.loadConsumImage(mActivity, url, imageView);
            }
        });
        // 设置bannerItem监听事件
        mBanner.setOnItemClickListener(this);
    }

    @Override
    protected void lazyLoad() {
        super.lazyLoad();
        mPresenter.reqDailyDataFromNet();
    }

    @Override
    public void showLatestData(LatestDailyListBean latestDailyListBean) {
        if (latestDailyListBean == null) {
            mStateView.showEmpty(R.string.stfEmptyMessage, R.string.stfButtonRetry);
            return;
        }

        //提取数据源中的image地址和title
        List<LatestDailyListBean.TopStoriesBean> topStories = latestDailyListBean.getTopStories();
        List<String> topImageUrls = new ArrayList<>();
        List<String> topContentData = new ArrayList<>();
        for (LatestDailyListBean.TopStoriesBean topStory : topStories) {
            topImageUrls.add(topStory.getImage());
            topContentData.add(topStory.getTitle());
        }

        mDailyAdapter.removeAllHeaderView();

        //临时的办法:将之前的banner清除掉,再重新new一个
        mBanner.stop();
        mBanner = null;
        initBanner();

        //设置banner图片url和图片标题
        mBanner.initBanner(topImageUrls, topContentData);
        // 添加banner
        mDailyAdapter.addHeaderView(mBanner);

        mDailyAdapter.setNewData(latestDailyListBean.getStories());

    }

    @Override
    public void onLoading() {
        if (mStateView != null) {
            mStateView.showLoading();
        }
    }

    @Override
    public void closeLoading() {
        mStateView.showContent();
    }

    @Override
    public void showContent() {
        closeRefresh();
        mStateView.showContent();
    }

    @Override
    public void showErrorMsg(String msg) {
        closeRefresh();
        SnackbarUtil.showBarLongTime(mDailyRecyclerView, msg, SnackbarUtil.ALERT);
    }

    @Override
    public void showEmptyView() {
        closeRefresh();
        mStateView.showEmpty(R.string.stfEmptyMessage, R.string.stfButtonRetry);
    }

    @Override
    public void showOffline() {
        closeRefresh();
        mStateView.showOffline(R.string.stfOfflineMessage, R.string.stfButtonSetting, new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                //未联网  跳转到设置界面
                DevicesUtils.goSetting(mActivity);
            }
        });
    }

    @Override
    public void loadMoreSuccess(String groupTitle, PastNewsBean pastNewsBean) {
        mDailyAdapter.loadMoreComplete();
        if (pastNewsBean == null) {
            return;
        }
        mDailyAdapter.addData(pastNewsBean.getStories());
    }

    @Override
    public void loadMoreFailed() {
        mDailyAdapter.loadMoreFail();
    }

    @Override
    public LifecycleTransformer<LatestDailyListBean> bindLifecycle() {
        return bindToLifecycle();
    }

    @Override
    public void onRefresh() {
        mPresenter.reqDailyDataFromNet();
    }

    @Override
    public void onLoadMoreRequested() {
        mPresenter.loadMoreData(pastDays++);
    }

    /**
     * 停止刷新
     */
    private void closeRefresh() {
        mRefreshLayout.setRefreshing(false);
    }

    // RecyclerView的item点击事件
    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        SnackbarUtil.showBarShortTime(mStateView, "position:" + position, SnackbarUtil.INFO);
    }

    // mBanner的点击事件
    @Override
    public void onItemClick(int position, String title) {
        SnackbarUtil.showBarShortTime(mStateView, "position:" + position, SnackbarUtil.INFO);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBanner != null) {
            mBanner.stop();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mBanner != null) {
            mBanner.start();
        }
    }
}
