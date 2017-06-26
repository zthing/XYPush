package cn.xinyisoft.push;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Socket连接类
 * 耗时操作
 */
class XinyiSocketConnect {
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 2);
    private SocketChannel socketChannel;

    private boolean netStatus;
    private Context mContext;
    private boolean isConnect = true;

    XinyiSocketConnect(Context context) {
        this.mContext = context;
        netStatus = Util.isNetworkConnected(context);
        NetWorkChangedReceiver.addNetWorkChangeListener(NetWorkChangedReceiver.NETWORK_SOCKET, new Util.Callback<Boolean>() {

            @Override
            public void callback(Boolean hasNetwork) {
                if (hasNetwork) {
                    if (!netStatus) {
                        netStatus = true;
                        if (isConnect)
                            connect();
                    }
                } else {
                    netStatus = false;
                }
            }
        });
    }

    /**
     * socket心跳异常次数
     */
    private int socketRequestCount = 0;
    private Timer timer = new Timer();
    private XTimerTask task;

    private class XTimerTask extends TimerTask {

        @Override
        public void run() {
            if (Util.isNetworkConnected(mContext)) {
                if (socketRequestCount++ < 3) {
                    sendMsg("xt");
                } else {
                    closeConnect();
                }
            }
        }
    }

    /**
     * 连接
     */
    void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isConnect && netStatus) {
                    try {
                        socketRequestCount = 0;
                        SharedPreferences sp = mContext.getSharedPreferences(XinyiPush.SP_PUSH, Context.MODE_PRIVATE);
                        int uid = sp.getInt("uid", 0);
                        if (uid == 0) {
                            disConnect();
                            break;
                        }
                        String token = sp.getString("token", "");
                        int appId = sp.getInt("XINYI_APPID", 0);
                        String secret = sp.getString("XINYI_SECRET", "");
                        String socketIp = sp.getString("socketIp", XinyiPush.SOCKET_IP);
                        int socketPort = sp.getInt("socketPort", XinyiPush.SOCKET_PORT);
                        SocketAddress socketAddress = new InetSocketAddress(socketIp, socketPort);
//                            socketChannel = SocketChannel.open(new InetSocketAddress(socketIp, socketPort));
                        socketChannel = SocketChannel.open();
                        socketChannel.socket().connect(socketAddress, 10000);//连接超时时间
                        socketChannel.socket().setSoTimeout(15000);//读超时时间
                        socketChannel.configureBlocking(false);
                        sendMsg(XinyiPushHttpRequest.getValidateStr(uid, token, appId, secret));
                        task = new XTimerTask();
                        timer.schedule(task, 5000, 5000);
                        receiveMessage();
                    } catch (Exception e) {
                        Log.e("SocketTimeoutException", e.getMessage());
                        e.printStackTrace();
                        sendReceiver(Util.OnPushCallback.PUSH_EXCEPTION, e.getMessage());
                    }
                    if (isConnect && netStatus) {// 连接断开，自动重连
                        sendReceiver(Util.OnPushCallback.PUSH_DIS_RECON, "");
                    }
                    closeConnect();
                    if (task != null) {
                        task.cancel();
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 关闭连接
     */
    private void closeConnect() {
        if (socketChannel != null) {
            try {
                socketChannel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭连接(不再重连)
     */
    void disConnect() {
        isConnect = false;
        if (task != null) {
            task.cancel();
        }
        NetWorkChangedReceiver.removeNetWorkChangeListener(NetWorkChangedReceiver.NETWORK_SOCKET);
        closeConnect();
    }

    private void receiveMessage() throws Exception {
        // 打开并注册选择器到信道
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
        while (selector.select() > 0) {// select()方法只能使用一次，用了之后就会自动删除,每个连接到服务器的选择器都是独立的
            // 遍历每个有可用IO操作Channel对应的SelectionKey
            for (SelectionKey sk : selector.selectedKeys()) {
                // 如果该SelectionKey对应的Channel中有可读的数据
                if (sk.isReadable()) {
                    readBuffer();
                    // 为下一次读取作准备
                    sk.interestOps(SelectionKey.OP_READ);
                }
                // 删除正在处理的SelectionKey
                selector.selectedKeys().remove(sk);
            }
        }
    }

    /**
     * 得到发送的数据
     */
    private ByteBuffer getSendStr(String str) throws UnsupportedEncodingException {
        byte[] bt = str.getBytes("UTF-8");
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + bt.length);
        // byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(bt.length);
        byteBuffer.put(bt);
        byteBuffer.flip();
        return byteBuffer;
    }

    /**
     * 读取数据
     */
    private void readBuffer() throws IOException {
        socketChannel.read(byteBuffer);
        byteBuffer.flip();
        checkPushMsg();
    }

    /**
     * 解析数据
     */
    private void checkPushMsg() throws UnsupportedEncodingException {
        int position = byteBuffer.position();
        int limit = byteBuffer.limit();
        if (limit != position && limit - position > 4) {// 长度有包头长
            int len;
            if ((len = byteBuffer.getInt()) <= limit - byteBuffer.position()) {// 存在一条完整的数据
                if (len != 0) {// 数据长度不为0时
                    byte[] bt = new byte[len];
                    byteBuffer.get(bt);
                    pushMsg(new String(bt, "UTF-8"));
                }
                checkPushMsg();
            } else if (len > byteBuffer.capacity() - 4) {// 对象长度不够一条数据
                ByteBuffer buffer = ByteBuffer.allocate(byteBuffer.capacity());
                byteBuffer.position(0);
                buffer.put(byteBuffer);
                buffer.flip();
                byteBuffer = ByteBuffer.allocate(byteBuffer.capacity() + 1024);
                byteBuffer.put(buffer);
            } else {// 对象长度够且不为一条完整的数据时
                byte[] bt = new byte[limit - byteBuffer.position()];
                byteBuffer.get(bt);
                byteBuffer.clear();
                byteBuffer.putInt(len);
                byteBuffer.put(bt);
            }
        } else if (limit - position > 0) {// 有数据但没有包头长
            byte[] bt = new byte[limit - position];
            byteBuffer.get(bt);
            byteBuffer.clear();
            byteBuffer.put(bt);
        } else {// 没数据了
            byteBuffer.clear();
        }
    }

    /**
     * 发送消息
     */
    private void sendMsg(final String str) {
        Log.e("socket发送", str);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketChannel.write(getSendStr(str));
                } catch (IOException e) {
                    e.printStackTrace();
                    closeConnect();
                }
            }
        }).start();
    }

    /**
     * 处理接收到的推送消息
     */
    private void pushMsg(final String str) {
        for (int i = 0; i < (str.length() % 2000 == 0 ? str.length() / 2000 : str.length() / 2000 + 1); i++) {
            if (i == str.length() / 2000 && str.length() % 2000 != 0) {
                Log.e("socket接收", str.substring(i * 2000, i * 2000 + (str.length() % 2000)));
            } else {
                Log.e("socket接收", str.substring(i * 2000, i * 2000 + 2000));
            }
        }
        if (str.equals("xt")) {
            socketRequestCount = 0;
            return;
        }
        try {
            JSONObject json = new JSONObject(str);
            if (json.has("code")) {
                int code = json.getInt("code");
                String msg = json.getString("msg");
                switch (code) {
                    case 1:// 注册成功
                        sendReceiver(Util.OnPushCallback.PUSH_STATUS, true, msg);
                        return;
                    case 401:// 相同的终端编号正在被同时注册

                        return;
                    case 402:// 相同的终端编号在其他地方登陆

                        return;
                    case 404:// 相同的用户正在多个设备同时注册

                        return;
                    case 405:// 用户在其他设备登陆
                        if (XinyiSocketService.Companion.getSocketService() != null) {
                            mContext.stopService(XinyiSocketService.Companion.getSocketService());
                        } else {
                            disConnect();
                        }
                        sendReceiver(Util.OnPushCallback.PUSH_OFFLINE, msg);
                        return;
                    case 409:// 注册数据解析失败
                        closeConnect();
                        return;
                    case 300:// 进入黑名单
                    case 400:// 注册时没有终端唯一编号
                    case 403:// data.type参数错误，只能是pc或者other
                    case 410:// 其他错误
                        sendReceiver(Util.OnPushCallback.PUSH_STATUS, false, msg);
                        closeConnect();
                        return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendReceiver(Util.OnPushCallback.PUSH_MSG, str);
    }

    private void sendReceiver(int pushType, String pushMsg) {
        Intent intent = new Intent("cn.xinyisoft.push");
        intent.putExtra("pushType", pushType);
        intent.putExtra("pushMsg", pushMsg);
        mContext.sendBroadcast(intent);
    }

    private void sendReceiver(int pushType, boolean isSuccess, String pushMsg) {
        Intent intent = new Intent("cn.xinyisoft.push");
        intent.putExtra("pushType", pushType);
        intent.putExtra("pushSuccess", isSuccess);
        intent.putExtra("pushMsg", pushMsg);
        mContext.sendBroadcast(intent);
    }

}