package cn.xinyisoft.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 接收推送信息
 * author:zhangtao on 2017/6/16 14:41
 */
abstract class XinyiPushReceiver : BroadcastReceiver(), Util.OnPushCallback {
    final override fun onReceive(context: Context, intent: Intent) {
        val msg = intent.getStringExtra("pushMsg")
        when (intent.getIntExtra("pushType", 0)) {
            Util.OnPushCallback.PUSH_STATUS -> onPushStatus(intent.getBooleanExtra("pushSuccess", false), msg)
            Util.OnPushCallback.PUSH_MSG -> onPushMsg(context, msg)
            Util.OnPushCallback.PUSH_DIS_RECON -> onPushDisconAndRecon()
            Util.OnPushCallback.PUSH_OFFLINE -> onPushOffline(msg)
            Util.OnPushCallback.PUSH_EXCEPTION -> onPushException(msg)
        }
    }
}
