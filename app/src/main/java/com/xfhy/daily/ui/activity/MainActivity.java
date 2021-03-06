package com.xfhy.daily.ui.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.xfhy.androidbasiclibs.basekit.activity.BaseActivity;
import com.xfhy.androidbasiclibs.basekit.fragment.BaseFragment;
import com.xfhy.androidbasiclibs.util.ToastUtil;
import com.xfhy.daily.R;
import com.xfhy.daily.ui.fragment.zhihu.ZHMainFragment;

import butterknife.BindView;

/**
 * author xfhy
 * create at 2017/9/12 21:11
 * description：主界面
 */
public class MainActivity extends BaseActivity implements NavigationView
        .OnNavigationItemSelectedListener {

    /**
     * Toolbar  标题栏
     */
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    /**
     * DrawerLayout  抽屉布局
     */
    @BindView(R.id.drawer_main)
    DrawerLayout mDrawerLayout;
    /**
     * NavigationView 侧滑菜单的布局
     */
    @BindView(R.id.nv_main)
    NavigationView mNavigationView;

    @Override
    protected void initView() {
        setToolBar(mToolbar, getResources().getString(R.string.drawer_menu_zhihu));

        //导航按钮有旋转特效
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //默认将知乎显示出来
        showZhihu();
    }

    /**
     * 测试---------------------------------------------------------
     * 默认将知乎显示出来
     */
    private void showZhihu() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fl_main_content, ZHMainFragment.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViewEvent() {
        //设置菜单默认选中项
        mNavigationView.setCheckedItem(R.id.nav_menu_zhihu);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void initData() {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //这个是HomeAsUp按钮的id永远都是android.R.id.home
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);   //将滑动菜单显示出来
                break;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_menu_zhihu:
                ToastUtil.showMessage(mContext, "知乎");
                break;
            case R.id.nav_menu_wechat:
                ToastUtil.showMessage(mContext, "微信精选");
                break;
            case R.id.nav_menu_joke:
                break;
            case R.id.nav_menu_gank:
                break;
            case R.id.nav_menu_girl:
                break;
            case R.id.nav_menu_like:
                break;
            case R.id.nav_menu_setting:
                break;
            case R.id.nav_menu_about:
                break;
            default:
                break;
        }
        //这里先测试一下
        //关闭滑动菜单
        mDrawerLayout.closeDrawers();
        return true;
    }

    public void pushFragment(BaseFragment fragment) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * 设置标题
     * @param title 标题
     */
    public void setToolBar(String title) {
        setToolBar(mToolbar, title);
    }

}
