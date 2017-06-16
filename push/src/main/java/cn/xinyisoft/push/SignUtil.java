package cn.xinyisoft.push;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * author:zhangtao on 2017/6/15 14:35
 */

class SignUtil {

    static String getSign(String data, String secret) {
        String result = "";
        try {
            JSONObject JsonObj = new JSONObject(data);
            JsonObj.put("secret", secret);
            List<String> keys = new ArrayList<>();
            Iterator<?> it = JsonObj.keys();
            while (it.hasNext()) {
                try {
                    String key = (String) it.next();
                    if (key.equals("sign")) {
                        continue;
                    }
                    keys.add(key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (keys.size() > 0) {
                Collections.sort(keys);
                for (int j = 0; j < keys.size(); j++) {
                    String key = keys.get(j);
                    String value = JsonObj.get(key).toString();
                    if (j != 0) {
                        result += "&";
                    }
                    result += key + "=" + value;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getMD5Value(result);
    }

    /**
     * socket通用字段
     */
    static void getSendStr_socket(int appId, JSONObject json, String method) throws JSONException {
        json.put("appid", appId);
        json.put("method", method);
        json.put("timestamp", System.currentTimeMillis());
        json.put("version", 1);
    }

    private static String getMD5Value(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
}
