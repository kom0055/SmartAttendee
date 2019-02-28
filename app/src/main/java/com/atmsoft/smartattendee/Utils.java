package com.atmsoft.smartattendee;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.RequiresApi;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class Utils {


    static {


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    static boolean isProcessRunning(Context context, String processName) {
        if (null == processName) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<RunningAppProcessInfo> lists = activityManager.getRunningAppProcesses();
        return lists.stream().anyMatch(p -> {
            if (null == p || null == p.processName) {
                return false;
            }
            return processName.equals(p.processName);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static boolean ConnectWiFi(WifiManager wifiManager, String ssId) {
        if (null == wifiManager || null == ssId) {
            return false;
        }
        try {
            wifiManager.setWifiEnabled(true);
            List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
            if (null == wifiConfigurations || 0 == wifiConfigurations.size()) {
                return false;
            }
            WifiConfiguration configuration;

            configuration = wifiConfigurations.stream().filter(p -> {
                if (null == p || null == p.SSID) {
                    return false;
                }
                return ssId.equals(p.SSID);
            }).findFirst().orElse(null);


            if (null == configuration) {

                configuration = wifiConfigurations.get(0);

            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            while (null == wifiInfo || null == wifiInfo.getSSID() || "".equals(wifiInfo.getSSID())) {
                wifiManager.enableNetwork(configuration.networkId, true);
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            Log.e("ConnectWifi error", e.getMessage());
            return false;
        }
        return true;
    }


    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.N)
    static boolean SendPunch(WifiManager wifiManager, String ssId) {
        try {
            HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
            if (!ConnectWiFi(wifiManager, ssId)) {
                return false;
            }
            OkHttpClient client = new OkHttpClient.Builder().cookieJar(new CookieJar() {
                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    cookieStore.put(url.host(), cookies);
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    List<Cookie> cookies = cookieStore.get(url.host());
                    return cookies != null ? cookies : new ArrayList<>();
                }
            }).build();
            try {
                Headers.Builder builder = new Headers.Builder();
                LoginHeaders.forEach(builder::add);
                RequestBody loginBody = new FormBody.Builder().add("loginName", loginName)
                        .add("password", password).add("publicKeyFlag", publicKeyFlag).build();
                Request loginRequest = new Request.Builder().url(LoginUrl).post(loginBody).headers(builder.build()).build();
                Response loginResponse = client.newCall(loginRequest).execute();
                if (loginResponse.code() != 200) {
                    return false;
                }
                if (loginResponse.body() == null) {
                    return false;
                }
                final String bodyStr = loginResponse.body().string();
                Log.d("LoginResponse Response", bodyStr);
                JSONObject jsonObject = JSONObject.parseObject(bodyStr);
                String sucessStr = jsonObject.getString("login");
                if (!"successed".equals(sucessStr)) {
                    return false;
                }
                Log.d("LoginResponse Headers", JSON.toJSONString(loginResponse.headers()));
            } catch (Exception e) {
                Log.e("Login Error", e.getMessage());
            }

            try {
                Headers.Builder builder = new Headers.Builder();
                PunchHeaders.forEach(builder::add);
                // builder.add("Cookie", cookies.toString());
                RequestBody punchBody = RequestBody.create(JsonMediaType, PunchBodyJson);
                Request punchRequest = new Request.Builder().url(PunchUrl).post(punchBody).headers(builder.build()).build();
                Response punchResponse = client.newCall(punchRequest).execute();
                if (punchResponse.code() != 200) {
                    return false;
                }
                if (punchResponse.body() == null) {
                    return false;
                }
                final String bodyStr = punchResponse.body().string();


                Log.d("LoginResponse Response", bodyStr);
                JSONObject jsonObject = JSONObject.parseObject(bodyStr);
                String sucessStr = jsonObject.getString("status");
                if (!"1".equals(sucessStr)) {
                    return false;
                }


            } catch (Exception e) {
                Log.e("Login Error", e.getMessage());
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e("SendPunch error", "" + e.getMessage());
            return false;
        }

    }


    public static String ASCII2UTF8(String asciiStr) {
        try {
            byte[] converttoBytes = asciiStr.getBytes("UTF-8");
            String s2 = new String(converttoBytes, "UTF-8");
            return s2;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}
