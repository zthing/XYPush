package cn.xinyisoft.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;


public class XinyiPushHttpRequest {
    /**
     * 信鸽注册
     */
    protected static final String ADDXGCLIENT = "public.push.addxgclient";
    /**
     * 信鸽注销
     */
    protected static final String REMOVEXGCLIENT = "public.push.removexgclient";
    /**
     * socket注册
     */
    protected static final String REGISTER = "public.client.reg";

    private static final int XGTYPE = 1;// 1：android；2：iOS
    private static final String TYPE = "other";
    private static final String CLIENTTYPE = "androidclient";

    /**
     * socket验证信息
     *
     * @author ZhangTao
     * @time 2016年9月27日下午2:34:15
     */
    protected static String getValidateStr(int uid, String token, int appId, String secret) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("clienttype", CLIENTTYPE);
        json.put("token", token);
        json.put("type", TYPE);
        json.put("uid", uid);
        json.put("unicode", System.currentTimeMillis());
        json.put("sign", Util.getSocketSign(json, REGISTER, appId, secret));
        return json.toString();
    }

    /**
     * 信鸽注册
     *
     * @param token 信鸽注册得到的token
     */
    public static void addXGClient(Context context, int uid, String token, Util.Callback<JSONObject> callback) throws JSONException {
        SharedPreferences sp = context.getSharedPreferences(XinyiPush.SP_PUSH, Context.MODE_PRIVATE);
        JSONObject json = new JSONObject();
        json.put("uid", uid);
        json.put("token", token);
        json.put("type", TYPE);
        json.put("clienttype", "AndroidClient");
        json.put("xgtype", XGTYPE);
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            json.put("xgappid", appInfo.metaData.getInt("XG_V2_ACCESS_ID"));
            json.put("xgsecret", appInfo.metaData.getString("XG_V2_ACCESS_KEY"));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        json.put("sign", Util.getSocketSign(json, ADDXGCLIENT, sp.getInt("XINYI_APPID", 0), sp.getString("XINYI_SECRET", "")));
        postCall(sp.getString("socketHttpUrl", XinyiPush.SOCKET_HTTP_URL), json, callback);
        sp.edit().putString("xgtoken", token).apply();
    }

    /**
     * 信鸽注销
     */
    public static void removeXGClient(Context context, Util.Callback<JSONObject> callback) throws JSONException {
        SharedPreferences sp = context.getSharedPreferences(XinyiPush.SP_PUSH, Context.MODE_PRIVATE);
        JSONObject json = new JSONObject();
        json.put("token", sp.getString("xgtoken", ""));
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            json.put("xgappid", appInfo.metaData.getInt("XG_V2_ACCESS_ID"));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        json.put("sign", Util.getSocketSign(json, REMOVEXGCLIENT, sp.getInt("XINYI_APPID", 0), sp.getString("XINYI_SECRET", "")));
        postCall(sp.getString("socketHttpUrl", XinyiPush.SOCKET_HTTP_URL), json, callback);
        sp.edit().putString("xgtoken", "").apply();
    }

    /**
     * socket相关请求
     *
     * @author ZhangTao
     * @time 2016年8月31日下午3:45:00
     */
    private static void postCall(String url, JSONObject json, final Util.Callback<JSONObject> callBack) throws JSONException {
        Log.e("socket API请求的数据", json.toString());
        FormEncodingBuilder builder = new FormEncodingBuilder();
        Iterator<String> it = json.keys();
        while (it.hasNext()) {
            String key = it.next();
            builder.add(key, json.getString(key));
        }
        Request request = new Request.Builder().url(url).post(builder.build()).build();
        new OkHttpClient().newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onResponse(Response arg0) throws IOException {
                String result = arg0.body().string();
                Log.e("socket API返回的数据", result);
                if (callBack != null) {
                    try {
                        callBack.callback(new JSONObject(result));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        requestFail(callBack, result);
                    }
                }
            }

            @Override
            public void onFailure(Request arg0, IOException arg1) {
                if (callBack != null) {
                    requestFail(callBack, "网络异常");
                }
            }
        });
    }

    private static void requestFail(Util.Callback<JSONObject> callBack, String msg) {
        JSONObject json = new JSONObject();
        try {
            json.put("code", -1);
            json.put("msg", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callBack.callback(json);
    }

}
