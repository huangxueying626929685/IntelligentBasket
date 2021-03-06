package com.automation.zzx.intelligent_basket_demo.activity.proAdmin;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin.AreaAdminFirstFragment;
import com.automation.zzx.intelligent_basket_demo.fragment.proAdmin.ProAdminFragment;
import com.automation.zzx.intelligent_basket_demo.fragment.proAdmin.ProAdminMessageFragment;
import com.automation.zzx.intelligent_basket_demo.fragment.proAdmin.ProAdminMgProjectFragment;
import com.automation.zzx.intelligent_basket_demo.utils.xiaomi.mipush.MiPushUtil;
import com.hjm.bottomtabbar.BottomTabBar;

import java.util.ArrayList;

/**
 * Created by zzx on 2019/5/20.
 * Describe: 项目负责人主界面
 */

public class ProAdminPrimaryOldActivity extends AppCompatActivity {

    // 控件
    private BottomTabBar mBottomTabBar;

    // 屏幕素质
    private int mScreenWidth;
    private int mScreenHeight;

    // 用户登录信息相关
    private UserInfo mUserInfo;
    private String mProjectId;
    private String mToken;
    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_admin_primary);

        getUserInfo();
        initWidget();
        MiPushUtil.initMiPush(ProAdminPrimaryOldActivity.this, mUserInfo.getUserId(), null);
    }

    private void initWidget(){

        mBottomTabBar = (BottomTabBar) findViewById(R.id.bottom_tab_bar);

        getScreenSize();
        mBottomTabBar.init(getSupportFragmentManager(), mScreenWidth, mScreenHeight)
                .setImgSize(90, 90)
                .setFontSize(30)
                //.setTabPadding(5,0,5)
                .setChangeColor(Color.parseColor("#009688"),Color.parseColor("#cccccc"))
                .addTabItem("项目", R.mipmap.ic_navi_project, AreaAdminFirstFragment.class)
                .addTabItem("消息", R.mipmap.ic_navi_message, ProAdminMessageFragment.class)
                .addTabItem("我", R.mipmap.ic_navi_me, ProAdminFragment.class)
                .isShowDivider(false)
                //.setDividerColor(Color.parseColor("#FF0000"))
                //.setTabBarBackgroundColor(Color.parseColor("#00FF0000"))
                .setOnTabChangeListener(new BottomTabBar.OnTabChangeListener() {
                    @Override
                    public void onTabChange(int position, String name, View view) {
                        if(position == 1)
                            mBottomTabBar.setSpot(2, false);
                    }
                })
                .setSpot(1, true);
    }

    /*
     * 解析用户信息
     */
    // 获取用户数据
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mToken = mPref.getString("loginToken","");
        mProjectId = mPref.getString("projectId","");
    }
    // 将用户信息传递给子Fragment
    public UserInfo pushUserInfo(){
        return mUserInfo;
    }
    // 将用户token传递给子Fragment
    public String pushToken(){
        return mToken;
    }
    // 将用户token传递给子Fragment
    public String pushProjectId(){
        return mProjectId;
    }

    /*
     * 为fragment提供触摸接口
     */
    //1.触摸事件接口
    public interface MyOnTouchListener {
        public boolean onTouch(MotionEvent ev);
    }
    //2. 保存MyOnTouchListener接口的列表
    private ArrayList<MyOnTouchListener> onTouchListeners = new ArrayList<MyOnTouchListener>();
    //3.分发触摸事件给所有注册了MyOnTouchListener的接口
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (MyOnTouchListener listener : onTouchListeners) {
            listener.onTouch(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
    //4.提供给Fragment通过getActivity()方法来注册自己的触摸事件的方法
    public void registerMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
        onTouchListeners.add(myOnTouchListener);
    }
    //5.提供给Fragment通过getActivity()方法来注销自己的触摸事件的方法
    public void unregisterMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
        onTouchListeners.remove(myOnTouchListener);
    }

    /*
     * 获取屏幕素质
     */
    // 获取屏幕的宽高度
    private void getScreenSize(){
        DisplayMetrics dm2 = getResources().getDisplayMetrics();
        mScreenHeight = dm2.heightPixels;
        mScreenWidth = dm2.widthPixels;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //CustomApplication.setMainActivity(null);
    }
    // 退出但不销毁
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
