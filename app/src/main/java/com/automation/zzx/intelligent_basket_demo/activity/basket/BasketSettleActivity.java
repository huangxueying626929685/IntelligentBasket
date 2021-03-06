package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.http.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.EditAlertDialog;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by zzx on 2019/5/3.
 * Describe: 吊篮参数设置活动
 * limits: Overweight_Current\Angel_UpperLimit\Angel_LowerLimit\Interval_Snapshot
 */
public class BasketSettleActivity extends AppCompatActivity implements View.OnClickListener {
    //消息处理
    private static final int GET_INFORM = 1;
    private static final int REFRESH_CURRENT = 2;
    private static final int REFRESH_UPPER = 3;
    private static final int REFRESH_LOWER = 4;
    private static final int REFRESH_SNAPSHOT = 5;


    private static final int GETINFO_DEFAULT = 8;
    private static final int REFRESH_DEFAULT = 9;
    private static final int REFRESH_SETTING = 10;//正在设置中

    //布局控件
    private RelativeLayout rlCurrent;
    private RelativeLayout rlUpper;
    private RelativeLayout rlLower;
    private RelativeLayout rlSnapshot;
    private TextView tvCurrent;
    private TextView tvUpper;
    private TextView tvLower;
    private TextView tvSnapshot;

    //修改对话框
    private EditAlertDialog mEditAlertDialog;

    //提交次数
    private Integer submitNum;

    // 吊篮相关
    private String mDeviceId; // 吊篮ID

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_INFORM:  //获取默认参数
                    if(msg.obj == null || msg.obj.toString().equals("")){
                        ToastUtil.showToastTips(BasketSettleActivity.this, "无法连接到设备");
                        finish();
                    }
                    if(msg.obj == null){
                        //当data数据为空时
                        tvCurrent.setText("0");
                        tvUpper.setText("0");
                        tvLower.setText("0");
                        tvSnapshot.setText("0");
                    } else {
                        JSONObject jsonObject = JSON.parseObject(msg.obj.toString());
                        if(jsonObject.getString("deviceId").equals(mDeviceId)){
                            tvCurrent.setText(jsonObject.getString("overweight_current"));
                            tvUpper.setText(jsonObject.getString("angle_upperlimit"));
                            tvLower.setText(jsonObject.getString("angle_lowerlimit"));
                            tvSnapshot.setText(jsonObject.getString("interval_snapshot"));
                        }
                    }
                    break;
                case REFRESH_CURRENT: //控件更新——超载电流
                    mEditAlertDialog.dismiss();
                    tvCurrent.setText(msg.obj.toString());
                    break;
                case REFRESH_UPPER: //控件更新——上限位
                    mEditAlertDialog.dismiss();
                    tvUpper.setText(msg.obj.toString());
                    break;
                case REFRESH_LOWER: //控件更新——下限位
                    mEditAlertDialog.dismiss();
                    tvLower.setText(msg.obj.toString());
                    break;
                case REFRESH_SNAPSHOT: //控件更新——截图时间
                    mEditAlertDialog.dismiss();
                    tvSnapshot.setText(msg.obj.toString());
                    break;
                case REFRESH_DEFAULT:
                    Toast.makeText(BasketSettleActivity.this, "提交参数失败,请稍后重试！", Toast.LENGTH_SHORT).show();
                    break;
                case REFRESH_SETTING:
                    Toast.makeText(BasketSettleActivity.this, "正在设置中，请稍后！" , Toast.LENGTH_SHORT).show();
                    mEditAlertDialog.countDownTimer.start();
                    break;
                case GETINFO_DEFAULT:
                    Toast.makeText(BasketSettleActivity.this, "获取参数失败,请稍后重试！", Toast.LENGTH_SHORT).show();
                    mEditAlertDialog.dismiss();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_settle);

        submitNum = 0;
        initWidgetResource();  // 初始化控件

        Intent intent = getIntent();
        mDeviceId = intent.getStringExtra(BasketDetailActivity.BASKET_ID);  // 获取吊篮ID
        if(mDeviceId==null || mDeviceId.equals("")) mDeviceId = "1";

        initParam();//初始化参数

    }

    private void initWidgetResource(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.settle_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        //获取资源控件
        tvCurrent = findViewById(R.id.txt_current_content);
        tvUpper  = findViewById(R.id.txt_upper_content);
        tvLower  = findViewById(R.id.txt_lower_content);
        tvSnapshot  = findViewById(R.id.txt_snapshot_content);

        rlCurrent = findViewById(R.id.item_current_layout);//超载电流
        rlCurrent.setOnClickListener(this);
        rlUpper  = findViewById(R.id.item_upper_layout);//倾斜角上限位
        rlUpper.setOnClickListener(this);
        rlLower = findViewById(R.id.item_lower_layout);//倾斜角下限位
        rlLower.setOnClickListener(this);
        rlSnapshot  = findViewById(R.id.item_snapshot_layout);//截图时间间隔
        rlSnapshot.setOnClickListener(this);

        mEditAlertDialog = new EditAlertDialog(this);

    }
    private void initParam() {
        HttpUtil.getParameOkHttpRequest(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                Looper.prepare();
                Toast.makeText(BasketSettleActivity.this, "网络连接失败！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() != 200){
                    Looper.prepare();
                    Toast.makeText(BasketSettleActivity.this, "网络连接超时,请稍后重试！", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                // 返回服务器数据
                String responseData = response.body().string();
                try {
                    Message msg = new Message();
                    JSONObject jsonObject = JSON.parseObject(responseData);
                    String isGet = jsonObject.getString("get");
                    if(isGet.equals("success")){
                        msg.what = GET_INFORM;
                        msg.obj = jsonObject.getString("data");
                        mHandler.sendMessage(msg);
                    } else if(isGet.equals("failed")) {
                        msg.what = GETINFO_DEFAULT;
                        mHandler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },mDeviceId);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.item_current_layout:
                mEditAlertDialog = initDialog(tvCurrent.getText().toString(),REFRESH_CURRENT,"Overweight_Current",getString(R.string.overweight_current));
                mEditAlertDialog.show();
                break;
            case R.id.item_upper_layout:
                mEditAlertDialog =initDialog(tvUpper.getText().toString(), REFRESH_UPPER,"Angle_UpperLimit",getString(R.string.angel_upperLimit));
                mEditAlertDialog.show();
                break;
            case R.id.item_lower_layout:
                mEditAlertDialog = initDialog(tvLower.getText().toString(), REFRESH_LOWER,"Angle_LowerLimit",getString(R.string.angel_lowerLimit));
                mEditAlertDialog.show();
                break;
            case R.id.item_snapshot_layout:
                mEditAlertDialog = initDialog(tvSnapshot.getText().toString(),REFRESH_SNAPSHOT,"Interval_Snapshot",getString(R.string.interval_snapshot));
                mEditAlertDialog.show();
                break;
             default:
                    break;
        }
    }

    /*
     * 修改弹框
     */
    private EditAlertDialog initDialog(String mMsg,final int i,final String key,final String title){
        return new EditAlertDialog(this, R.style.dialog, mMsg,
                new EditAlertDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm,String result) {
                        if(confirm){
                            //判断提交次数
                            if(submitNum >= 3){
                                Message msg = new Message();
                                msg.what = REFRESH_DEFAULT;
                                mHandler.sendMessage(msg);
                                dialog.dismiss();
                            } else {
                                httpSubmitParam(key, result, i);
                            }
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle(title);
    }

    //提交修改申请
    private void httpSubmitParam(String key, final String value, final int i){
        HttpUtil.setParameOkHttpRequest(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                Looper.prepare();
                Toast.makeText(BasketSettleActivity.this, "网络连接失败！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() != 200){
                    Looper.prepare();
                    Toast.makeText(BasketSettleActivity.this, "网络连接超时,请稍后重试！", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                // 返回服务器数据
                String responseData = response.body().string();
                try {
                    Message msg = new Message();
                    if(responseData.equals("success")){
                        msg.what = i;
                        msg.obj = value;
                        submitNum = 0;                                //清零提交次数
                        mHandler.sendMessage(msg);
                    } else if(responseData.equals("setting")){
                        msg.what = REFRESH_SETTING;
                        submitNum = submitNum + 1;                                //增加提交次数
                        mHandler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },key,value,mDeviceId);
    }


    //页面销毁前,清理计时器
    @Override
    protected void onDestroy() {
        //销毁计时器
        if(mEditAlertDialog.countDownTimer != null){
            mEditAlertDialog.countDownTimer.cancel();
        }
        mEditAlertDialog.countDownTimer = null;
        submitNum = 0;
        super.onDestroy();
    }

    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
