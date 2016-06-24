package com.coolcweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.coolcweather.app.R;
import com.coolcweather.app.service.AutoUpdateService;
import com.coolcweather.app.util.HttpCallbackListener;
import com.coolcweather.app.util.HttpUtil;
import com.coolcweather.app.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Created by Administrator on 2016/6/23.
 */
public class WeatherActivity extends Activity implements View.OnClickListener{

    /**
     * 高德地图地理编码的基地址
     */
    public static final String BASEADDRESSOFAMAP = "http://restapi.amap.com/v3/geocode/geo?key=66659500681d3da9952f62dc81cca26e&address=";

    /**
     * 天气预告请求的基地址
     */
    public static final String BASEADDRESSOFFORECAST = "http://v.juhe.cn/weather/geo?format=2&key=1359e14b8bccc3628f64dcf7b582cfe9&";

    /**
     * 用于显示城市名
     */
    private TextView cityNameText;

    /**
     * 用于显示发布时间
     */
    private TextView publishText;

    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;

    /**
     * 用于显示当前温度
     */
    private TextView currentTemperature;

    /**
     * 用于显示最高最低温度
     */
    private TextView minMaxTemperature;

    /**
     * 用于显示天气描述
     */
    private TextView weatherDesp;

    /**
     * 利用异步消息机制对UI进行操作。
     */
    public static final int UPDATE_TEXT = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    //在这里可以进行UI操作
                    showWeather();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 切换城市按钮
     */
    private Button switchCity;

    /**
     * 更新天气按钮
     */
    private Button refreshWeather;

    /**
     * 地址的全名即省 + 市 + 县
     */
    private String fullAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        //初始化各控件
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        currentDateText = (TextView) findViewById(R.id.current_date);
        currentTemperature = (TextView) findViewById(R.id.current_temperature);
        minMaxTemperature = (TextView) findViewById(R.id.min_max_temperature);
        weatherDesp = (TextView) findViewById(R.id.weather_desp);

        fullAddress = getIntent().getStringExtra("fullAddress");
        if (fullAddress != null) {
            handleWeatherInfo(fullAddress);
        }
        showWeather();

        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);

        //利用intent自动更新启动服务，
//        Intent intent = new Intent(this, AutoUpdateService.class);
//        startService(intent);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中...");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                fullAddress = preferences.getString("full_address", null);
                if (fullAddress != null) {
                    handleWeatherInfo(fullAddress);
                }
                break;
        }
    }

    /**
     * 通过网络获取指定location的天气情况
     * 1.利用高德地图WebApi对指定地点进行地理编码
     * 2.通过获得的地理编码查询天气情况
     * 3.将获得的天气情况存储到SharedPreference中
     * 4.通过异步消息机制，通知UI进行更新
     */
    private void handleWeatherInfo(String location) {

        String addressAMap = null;
        try {
            addressAMap = BASEADDRESSOFAMAP + URLEncoder.encode(location ,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d("tiaoshi", addressAMap);
        HttpUtil.sendHttpRequest(addressAMap, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    Log.d("tiaoshi", response.toString());
                    JSONObject jsonObject = new JSONObject(response);
                    String location = jsonObject.getJSONArray("geocodes").getJSONObject(0).getString("location");
                    String[] lonAndLat = location.split(",");
                    String lon = lonAndLat[0];
                    String lat = lonAndLat[1];
                    String addressForecast = BASEADDRESSOFFORECAST + "lon=" + lon + "&lat=" + lat;
                    Log.d("tiaoshi", addressForecast);
                    HttpUtil.sendHttpRequest(addressForecast, new HttpCallbackListener() {
                        @Override
                        public void onFinish(String response) {
                            Log.d("tiaoshi", response);
                            Utility.handleWeatherResponse(WeatherActivity.this, response, fullAddress);
                            Message msg = new Message();
                            msg.what = UPDATE_TEXT;
                            handler.sendMessage(msg);
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

    /**
     * 从SharedPreference文件中读取存储的天气信息，并显示到界面上
     */
    private void showWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(preferences.getString("city_name", ""));
        minMaxTemperature.setText(preferences.getString("min_max_temperature", ""));
        currentTemperature.setText(preferences.getString("current_temperature", "") + "℃");
        publishText.setText("今天" + preferences.getString("publish_time", "") + "发布");
        weatherDesp.setText(preferences.getString("weather_desp", ""));
        currentDateText.setText(preferences.getString("current_date", ""));
    }
}