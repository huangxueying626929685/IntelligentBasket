package com.example.zzx.zbar_demo.activity.worker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.activity.loginRegist.LoginActivity;
import com.example.zzx.zbar_demo.entity.UserInfo;
import com.example.zzx.zbar_demo.utils.HttpUtil;
import com.example.zzx.zbar_demo.widget.VerifyWorkDialog;
import com.example.zzx.zbar_demo.widget.image.SmartImageView;
import com.example.zzx.zbar_demo.widget.zxing.activity.CaptureActivity;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.zzx.zbar_demo.entity.AppConfig.FILE_SERVER_PATH;

/**
 * Created by pengchenghu on 2019/3/15.
 * Author Email: 15651851181@163.com
 * Describe: 施工人员主页面
 * limits:
 */

public class WorkerPrimaryActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "WorkerPrimaryActivity";

    // 页面跳转
    private final static int CAPTURE_ACTIVITY_RESULT = 1;

    // Handler消息
    private final static int CHANGE_WORK_STATE_MSG = 101;
    private final static int OPEN_VERIFY_DIALOG_MSG = 102;
    private final static int UPDATE_USER_DISPLAY_MSG = 103;

    // header
    private SmartImageView mWorkerHead; // 头像
    private TextView mWorkerName;  // 名字
    private TextView mWorkerProjectState;  // 项目

    // function choose
    private LinearLayout mWorkLayout; // 开工/下工
    private ImageView mWorkIv; // 开工状态
    private TextView mWorkTv;
    private LinearLayout mMessageLayout; // 消息
    private LinearLayout mWarningLayout; //警告

    // 其它功能
    private RelativeLayout mContactLayout; // 联系与反馈
    private RelativeLayout mSettingLayout; // 设置

    // 页面信息
    private int mWorkState = 0; // 0:等待上工 1:等待下工
    private String mUserHeadUrl = FILE_SERVER_PATH + "/head/hdImg_default.jpg";

    // dialog
    private VerifyWorkDialog mVerifyWorkDialog;

    // 用户信息
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;

    // handler 处理消息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHANGE_WORK_STATE_MSG:  // 更改上工状态
                    if(mWorkState == 0){
                        mWorkTv.setText(R.string.worker_start_basket);
                        mWorkIv.setImageResource(R.mipmap.ic_worker_open);
                    }else if(mWorkState ==1){
                        mWorkTv.setText(R.string.worker_stop_basket);
                        mWorkIv.setImageResource(R.mipmap.ic_worker_close);
                    }
                    break;

                case OPEN_VERIFY_DIALOG_MSG:  // 打开上/下工确认
                    String basketId = msg.obj.toString();
                    mVerifyWorkDialog = new VerifyWorkDialog(WorkerPrimaryActivity.this,
                            R.style.dialog, mWorkState, mUserHeadUrl, basketId, new VerifyWorkDialog.OnDialogOperateListener() {
                        @Override
                        public void getVerifyResult(String result) {
                            if(result.contains("Success")) {
                                Log.i(TAG, "Now, you can opem/close the basket");
                                changeWorkState(mWorkState);
                            }
                        }
                    });
                    mVerifyWorkDialog.show();

                case UPDATE_USER_DISPLAY_MSG:  // 更新状态
                    mWorkerHead.setImageUrl(mUserHeadUrl);
                    mWorkerName.setText(mUserInfo.getUserName());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_primary);

        getUserInfo();
        if(!isHasPermission()) requestPermission();  // 权限申请
        initWidget();
    }

    // 初始化控件
    private void initWidget(){
        // header
        mWorkerHead = (SmartImageView) findViewById(R.id.login_head); // 头像
        mWorkerHead.setCircle(true);
        mWorkerHead.setImageUrl(mUserHeadUrl);
        mWorkerName = (TextView) findViewById(R.id.login_username);  // 用户名
        mWorkerProjectState = (TextView) findViewById(R.id.worker_project_state); // 登录状态

        // function choose
        mWorkLayout = (LinearLayout) findViewById(R.id.work_layout);  // 开工/下工
        mWorkLayout.setOnClickListener(this);
        mWorkIv = (ImageView) findViewById(R.id.work_imageview);
        mWorkTv = (TextView) findViewById(R.id.work_textview);
        mMessageLayout = (LinearLayout) findViewById(R.id.message_layout);
        mMessageLayout.setOnClickListener(this);
        mWarningLayout = (LinearLayout) findViewById(R.id.warning_layout);
        mWarningLayout.setOnClickListener(this);

        // other function
        mContactLayout = (RelativeLayout) findViewById(R.id.comment_and_feedback_layout);
        mContactLayout.setOnClickListener(this);
        mSettingLayout = (RelativeLayout) findViewById(R.id.setting_layout);
        mSettingLayout.setOnClickListener(this);
    }

    /*
     * 点击事件消息响应
     */
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.work_layout:  // 开工/下工
                Log.i(TAG, "You have clicked open/close work button");
                intent = new Intent(WorkerPrimaryActivity.this, CaptureActivity.class);
                startActivityForResult(intent, CAPTURE_ACTIVITY_RESULT);
                break;
            case R.id.order_layout:
                Log.i(TAG, "You have clicked order button");
                break;
            case R.id.message_layout:  // 消息
                Log.i(TAG, "You have clicked message button");
                break;
            case R.id.warning_layout:  // 报警
                Log.i(TAG, "You have clicked warning button");
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:15651851181"));
                startActivity(intent);
                break;
            case R.id.comment_and_feedback_layout:  // 联系与反馈
                Log.i(TAG, "You have clicked comment and feedback button");
                break;
            case R.id.setting_layout:  // 设置
                Log.i(TAG, "You have clicked setting button");
                logoutHttp();
                break;
        }
    }

    /*
     * 活动返回
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case CAPTURE_ACTIVITY_RESULT:
                if(resultCode == RESULT_OK){
                    String basket_id = data.getStringExtra("basket_id");
                    Log.i(TAG, "Device ID: "+basket_id);
                    Message msg = new Message();
                    msg.what = OPEN_VERIFY_DIALOG_MSG;
                    msg.obj = basket_id;
                    mHandler.sendMessage(msg);
                }
                break;
        }
    }

    /*
     * 上/下工切换
     */
    private void changeWorkState(int workState){
        if(workState == 0){
            mWorkState = 1;
            mWorkIv.setImageResource(R.mipmap.ic_worker_close);
            mWorkTv.setText(R.string.worker_stop_basket);
        }else if(workState == 1){
            mWorkState = 0;
            mWorkIv.setImageResource(R.mipmap.ic_worker_open);
            mWorkTv.setText(R.string.worker_start_basket);
        }
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
        mToken = mPref.getString("loginToken","");

        // 从后台获取其他数据
        getUserInfoFromInternet();
    }
    // 获取后台数据
    private void getUserInfoFromInternet(){
        HttpUtil.getWorkerAllInfoOkHttpRequest(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, e.toString());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                parseUserInfoFromInternet(data);
            }
        }, mToken, mUserInfo.getUserId());
    }
    // 解析后台返回数据
    private void parseUserInfoFromInternet(String data){
        Log.d(TAG, "parse data:" + data);
        JSONObject jsonObject = JSON.parseObject(data);
        String userInfo = jsonObject.getString("userInfo");
        mUserInfo = JSON.parseObject(userInfo, UserInfo.class);
        mHandler.sendEmptyMessage(UPDATE_USER_DISPLAY_MSG);  // 更新状态
    }
    //退出登录
    private void logoutHttp() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.clear();
        editor.commit();
        startActivity(new Intent(WorkerPrimaryActivity.this, LoginActivity.class));
    }

    /*
        用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(WorkerPrimaryActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.CAMERA) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.CALL_PHONE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            //initCamera(scanPreview.getHolder());
                            onResume();
                        }else {
                                Toast.makeText(WorkerPrimaryActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(WorkerPrimaryActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(WorkerPrimaryActivity.this);
                        }else {
                            Toast.makeText(WorkerPrimaryActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(WorkerPrimaryActivity.this, Permission.CAMERA)
                && XXPermissions.isHasPermission(WorkerPrimaryActivity.this, Permission.CALL_PHONE))
            return true;
        return false;
    }

    // 跳转到设置界面
    private void gotoPermissionSettings(View view) {
        XXPermissions.gotoPermissionSettings(this);
    }

}