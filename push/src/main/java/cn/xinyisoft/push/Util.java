package cn.xinyisoft.push;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * author:zhangtao on 2017/6/15 12:17
 */
public class Util {

    static String getSocketSign(JSONObject json, String method, int appId, String secret) throws JSONException {
        SignUtil.getSendStr_socket(appId, json, method);
        return SignUtil.getSign(json.toString(), secret);
    }

    /**
     * 检测网络是否可用
     */
    static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    public interface Callback<T> {
        void callback(T t);
    }

    /**
     * 推送回调
     */
    public interface OnPushCallback {

        int PUSH_MSG = 1;
        int PUSH_STATUS = 2;
        int PUSH_EXCEPTION = 3;
        int PUSH_OFFLINE = 4;
        int PUSH_DIS_RECON = 5;

        /**
         * 接收到推送消息
         */
        void onPushMsg(Context context, String msg);

        /**
         * 推送连接状态变化
         */
        void onPushStatus(boolean isSuccess, String msg);

        /**
         * 推送发生异常(暂无实现)
         */
        void onPushException(String msg);

        /**
         * 下线通知
         */
        void onPushOffline(String msg);

        /**
         * 连接断开,自动重连
         */
        void onPushDisconAndRecon();
    }
}
