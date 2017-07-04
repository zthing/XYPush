package cn.xinyisoft.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.json.JSONException;
import org.json.JSONObject;


public class XinyiPushHttpRequest {
    /**
     * 信鸽注册
     */
    private static final String ADDXGCLIENT = "public.push.addxgclient";
    /**
     * 信鸽注销
     */
    private static final String REMOVEXGCLIENT = "public.push.removexgclient";
    /**
     * socket注册
     */
    private static final String REGISTER = "public.client.reg";

    private static final int XGTYPE = 1;// 1：android；2：iOS
    private static final String TYPE = "other";
    private static final String CLIENTTYPE = "androidclient";

    /**
     * socket验证信息
     *
     * @author ZhangTao
     * @time 2016年9月27日下午2:34:15
     */
    static String getValidateStr(int uid, String token, int appId, String secret) throws JSONException {
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
     * 得到信鸽注册请求json字符串
     *
     * @param token 信鸽注册得到的token
     */
    public static String addXGClient(Context context, int uid, String token) throws JSONException {
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
        return json.toString();
    }

    /**
     * 得到信鸽注销请求json字符串
     *
     * @param token 信鸽注册得到的token
     */
    public static String removeXGClient(Context context, String token) throws JSONException {
        SharedPreferences sp = context.getSharedPreferences(XinyiPush.SP_PUSH, Context.MODE_PRIVATE);
        JSONObject json = new JSONObject();
        json.put("token", token);
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            json.put("xgappid", appInfo.metaData.getInt("XG_V2_ACCESS_ID"));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        json.put("sign", Util.getSocketSign(json, REMOVEXGCLIENT, sp.getInt("XINYI_APPID", 0), sp.getString("XINYI_SECRET", "")));
        return json.toString();
    }
}
