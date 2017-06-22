# 芯易推送 #

***信鸽逻辑自行处理***

**使用**

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	dependencies {
	        compile 'com.github.ZT-github:XYPush:1.0'
	}
**配置**

        <!--你接收推送信息广播-->
        <receiver android:name="···">
            <intent-filter>
                <action android:name="cn.xinisoft.push"/>
            </intent-filter>
        </receiver>

        <!--芯易APPID-->
        <meta-data
            android:name="XINYI_APPID"
            android:value="···"/>
        <!--芯易SECRET-->
        <meta-data
            android:name="XINYI_SECRET"
            android:value="···"/>
**代码**

    //接收推送的广播(kotlin代码)
	class PushReceiver : XinyiPushReceiver()
	
	//广播继承XinyiPushReceiver类
	//实现下列方法

	/** 推送回调 */
    public interface OnPushCallback {

        /** 接收到推送消息 */
        void onPushMsg(Context context, String msg);

        /** 推送连接状态变化 */
        void onPushStatus(boolean isSuccess, String msg);

        /** 推送发生异常(暂无实现) */
        void onPushException(String msg);

        /** 下线通知 */
        void onPushOffline(String msg);

        /** 连接断开,自动重连 */
        void onPushDisconAndRecon();
    }
	
	//操作类 XinyiPush

	/**
     * 连接
     * @param uid     用户id
     * @param token   设备编号
     */
    public static void connect(Context context, int uid, String token)
	
	/** 断开连接 */
    public static void disconnect(Context context)

	//也可以使用下列方法初始化参数连接
	XinyiPush.Builder(this)//上下文对象Context
                .soceketIp("IP，默认：192.168.10.216")
                .socketPort(8124)//端口号，默认：8124
                .socketHttpUrl("连接信鸽时的请求地址，默认：http://api.xinyitest.cn/api")
                .connect(38, token)

	//信鸽连接相关 XinyiPushHttpRequest

	/**
     * 信鸽注册
     * @param token 信鸽注册得到的token
     */
    public static void addXGClient(Context context, int uid, String token, Util.Callback<JSONObject> callback)

    /** 信鸽注销 */
    public static void removeXGClient(Context context, Util.Callback<JSONObject> callback)
