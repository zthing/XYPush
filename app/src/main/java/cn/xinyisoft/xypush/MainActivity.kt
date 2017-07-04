package cn.xinyisoft.xypush

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.xinyisoft.push.XinyiPush
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.Utils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Utils.init(this)

        val token = DeviceUtils.getAndroidID() ?: System.currentTimeMillis().toString()

        XinyiPush.Builder(this)
                .socketIp("192.168.10.188")
                .socketPort(8124)
                .isLog(true)
                .connect(38, token)

        XinyiPush.connect(this, 38, token)
    }

    override fun onDestroy() {
        super.onDestroy()
        XinyiPush.disconnect(this)
    }
}
