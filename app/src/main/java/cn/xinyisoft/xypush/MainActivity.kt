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
                .soceketIp("192.168.10.216")
                .socketPort(8124)
                .socketHttpUrl("http://api.xinyitest.cn/api")
                .connect(38, token)

        XinyiPush.connect(this, 38, token)
    }

    override fun onDestroy() {
        super.onDestroy()
        XinyiPush.disconnect(this)
    }
}
