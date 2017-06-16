package cn.xinyisoft.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.SparseArray;

public class NetWorkChangedReceiver extends BroadcastReceiver {

    /**
     * XinyiSocket
     */
    public static final int NETWORK_SOCKET = 1;

    public static int netWorkType = 0;

    /**
     * 设置网络变化监听
     */
    public static void addNetWorkChangeListener(int key, Util.Callback<Boolean> callBack) {
        callbackArray.put(key, callBack);
    }

    /**
     * 移除网络变化监听
     */
    public static void removeNetWorkChangeListener(int key) {
        callbackArray.remove(key);
    }

    private static SparseArray<Util.Callback<Boolean>> callbackArray = new SparseArray<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
        // 网络连接
        if (netInfo != null && netInfo.isAvailable()) {
            netWorkType = netInfo.getType(); //ConnectivityManager.TYPE_WIFI;
        } else {//无网络连接
            netWorkType = 0;
        }
        for (int i = 0; i < callbackArray.size(); i++) {
            Util.Callback<Boolean> callback = callbackArray.valueAt(i);
            if (callback != null) {
                callback.callback(netWorkType != 0);
            }
        }
    }
}
