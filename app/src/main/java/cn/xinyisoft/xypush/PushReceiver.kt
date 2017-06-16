package cn.xinyisoft.xypush

import android.content.Context
import cn.xinyisoft.push.XinyiPushReceiver

/**
 * author:zhangtao on 2017/6/16 16:03
 */
class PushReceiver : XinyiPushReceiver() {
    override fun onPushMsg(context: Context?, msg: String?) {
    }

    override fun onPushStatus(isSuccess: Boolean, msg: String?) {
    }

    override fun onPushException(msg: String?) {
    }

    override fun onPushOffline(msg: String?) {
    }

    override fun onPushDisconAndRecon() {
    }
}