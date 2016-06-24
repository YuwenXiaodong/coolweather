package com.coolcweather.app.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.coolcweather.app.receiver.AutoUpdateReceiver;
import com.coolcweather.app.util.HttpCallbackListener;
import com.coolcweather.app.util.HttpUtil;
import com.coolcweather.app.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Administrator on 2016/6/24.
 */
public class AutoUpdateService extends Service {

    /**
     * 高德地图地理编码的基地址
     */
    public static final String BASEADDRESSOFAMAP = "http://restapi.amap.com/v3/geocode/geo?key=66659500681d3da9952f62dc81cca26e&address=";

    /**
     * 天气预告请求的基地址
     */
    public static final String BASEADDRESSOFFORECAST = "http://v.juhe.cn/weather/geo?format=2&key=1359e14b8bccc3628f64dcf7b582cfe9&";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;    //这是8小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String fullAddress = preferences.getString("full_address", null);
        if (fullAddress != null) {
            String addressAMap = null;
            try {
                addressAMap = BASEADDRESSOFAMAP + URLEncoder.encode(fullAddress ,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.d("tiaoshi", "Service--------------" + addressAMap);
            HttpUtil.sendHttpRequest(addressAMap, new HttpCallbackListener() {
                @Override
                public void onFinish(String response) {
                    try {
                        Log.d("tiaoshi", "Service--------------" + response.toString());
                        JSONObject jsonObject = new JSONObject(response);
                        String location = jsonObject.getJSONArray("geocodes").getJSONObject(0).getString("location");
                        String[] lonAndLat = location.split(",");
                        String lon = lonAndLat[0];
                        String lat = lonAndLat[1];
                        String addressForecast = BASEADDRESSOFFORECAST + "lon=" + lon + "&lat=" + lat;
                        Log.d("tiaoshi", "Service--------------" + addressForecast);
                        HttpUtil.sendHttpRequest(addressForecast, new HttpCallbackListener() {
                            @Override
                            public void onFinish(String response) {
                                Log.d("tiaoshi", "Service--------------" + response);
                                Utility.handleWeatherResponse(AutoUpdateService.this, response, fullAddress);
                            }

                            @Override
                            public void onError(Exception e) {
                                e.printStackTrace();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });


        }


    }

}
