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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import androidx.annotation.RequiresApi;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class Utils {
    private static String loginName = "NDRn7imeCHtB9dFVkTFNSE2C3rfXHwN7okfYXXdri8/WZCvOQa1C7CW/LIpjNLuoiHoOlqrfq1In07vdVXlHFqSrLR7x4/pQ/awoqayAxa/09Xr4QIczWOzVGj3VniIojk0OUYfFMfsYbnsyYeADJNqBbP7oO8v3Oz4ueIfno5c=";
    private static String LoginUrl = "https://w3m.huawei.com/mcloud/mag/LoginAutoReg";
    private static String password = "gQERp1cL9TNEU5ktzHlmJP1v/6wtp27KpbPuQPmnaSPKvq9rTN0JIWGiTYuW211gx+OtGigjl+kJiM9Nv/oFuQbdFBkJqFBRKs1EzI2nIjvcCP1VMLPiSEDNzjEI9sJ9dnEUkBiz+G1lKs/2B28upAeDnjkl4as/DnJiQWogE8E=";
    private static String publicKeyFlag = "0";
    private static String PunchUrl = "https://w3m.huawei.com/mcloud/mag/ProxyForText/w3mbm/rest/mattend/punchCard?validateUser=false";
    private static Map<String, String> LoginHeaders = new HashMap<>();
    private static Map<String, String> PunchHeaders = new HashMap<>();
    private static final MediaType JsonMediaType = MediaType.parse("application/json; charset=utf-8");
    private static final String PunchBodyJson = "{	\"locale\": \"cn\",	\"deviceId\": \"E4305584-0EE3-4124-927F-6B76CD0EADBF\",	\"meapip\": \"104.71.166.148\",	\"x\": \"120.204513\",\"employeeNumber\": \"00451414\",\"deviceType\": \"0\",\"y\": \"30.182622\",	\"ip\": \"192.168.0.101\"}";

    static {
        LoginHeaders.put("AppId", "com.huawei.works");
        LoginHeaders.put("guid", "E4305584-0EE3-4124-927F-6B76CD0EADBF-P00451414");
        LoginHeaders.put("traceId", "WK-DD2EBB40-030E-4471-9BDC-23932BA7F883");
        LoginHeaders.put("User-Agent", "WeLink/3.8.5 (iPhone; iOS 12.1.4; Scale/3.00)");
        LoginHeaders.put("lang", "zh");
        LoginHeaders.put("nflag", "1");
        LoginHeaders.put("deviceName", "iPhone11,6");
        LoginHeaders.put("appVersion", "164");
        LoginHeaders.put("client", "HWorks.iPhone");
        LoginHeaders.put("uuid", "E4305584-0EE3-4124-927F-6B76CD0EADBF");
        LoginHeaders.put("osTarget", "1");
        LoginHeaders.put("appName", "WeLink");
        LoginHeaders.put("Connection", "keep-alive");
        LoginHeaders.put("isp", "");
        LoginHeaders.put("buildCode", "3.8.5");
        LoginHeaders.put("Accept-Language", "zh-Hans-CN;q=1, en-CN;q=0.9");
        LoginHeaders.put("networkType", "WiFi");
        LoginHeaders.put("Accept", "*/*");
        LoginHeaders.put("Content-Type", "application/x-www-form-urlencoded");
        LoginHeaders.put("needSF", "2");
//        LoginHeaders.put("Accept-Encoding", "br, gzip, deflate");

        PunchHeaders.put("Accept", "*/*");
        PunchHeaders.put("appName", "WeLink");
        PunchHeaders.put("Accept-Language", "zh-Hans-CN;q=1, en-CN;q=0.9");
//        PunchHeaders.put("Accept-Encoding", "br, gzip, deflate");
        PunchHeaders.put("uuid", "E4305584-0EE3-4124-927F-6B76CD0EADBF");
        PunchHeaders.put("traceId", "WK-FB9AC48A-A648-4FAF-8AC9-3A8282A89EDA");
        PunchHeaders.put("osTarget", "1");
        PunchHeaders.put("User-Agent", "WeLink/3.8.5 (iPhone; iOS 12.1.4; Scale/3.00)");
        PunchHeaders.put("lang", "zh");
        PunchHeaders.put("Connection", "keep-alive");
        PunchHeaders.put("client", "HWorks.iPhone");
        PunchHeaders.put("deviceName", "iPhone11,6");
        PunchHeaders.put("Content-Type", "application/json");


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
