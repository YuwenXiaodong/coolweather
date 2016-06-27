package com.coolcweather.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import com.coolcweather.app.db.CoolWeatherDB;
import com.coolcweather.app.model.City;
import com.coolcweather.app.model.County;
import com.coolcweather.app.model.Province;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/6/21.
 */
public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB
            , String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONObject(response).getJSONObject("str").getJSONArray("regions");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(jsonObject.getString("name"));
                    province.setProvinceCode(jsonObject.getString("id"));
                    //将解析出来的数据存储到Province表中
                    coolWeatherDB.saveProvince(province);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB
            , String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONObject(response).getJSONObject("str").getJSONArray("regions");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    City city = new City();
                    city.setCityName(jsonObject.getString("name"));
                    city.setCityCode(jsonObject.getString("id"));
                    city.setProvinceId(provinceId);
                    //将解析出来的数据存储到City表中
                    coolWeatherDB.saveCity(city);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }


    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB
            , String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONObject(response).getJSONObject("str").getJSONArray("regions");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(jsonObject.getString("name"));
                    county.setCountyCode(jsonObject.getString("id"));
                    county.setCityId(cityId);
                    Log.d("Utility", county.getCountyName());
                    //将解析出来的数据存储到County表中
                    coolWeatherDB.saveCounty(county);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * 解析服务器返回的天气JSON数据，并将解析出的数据存储到本地。
     */
    public static void handleWeatherResponse(Context context, String response, String fullAddress) {
        try {
            JSONObject jsonObject = new JSONObject(response).getJSONObject("result");
            String cityName = jsonObject.getJSONObject("today").getString("city");
            String minMaxTemperature = jsonObject.getJSONObject("today").getString("temperature");
            String currentTemperature = jsonObject.getJSONObject("sk").getString("temp");
            String publishTime = jsonObject.getJSONObject("sk").getString("time");
            String weatherDesp = jsonObject.getJSONObject("today").getString("weather");
            String currentDate = jsonObject.getJSONObject("today").getString("date_y");

            saveWeatherInfo(context, cityName, minMaxTemperature, currentTemperature,
                    publishTime, weatherDesp, currentDate, fullAddress);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将服务器返回的所有天气信息存储到SharedPreferences文件中
     */
    public static void saveWeatherInfo(Context context, String cityName, String minMaxTemperature,
                                       String currentTemperature, String publishTime,
                                       String weatherDesp, String currentDate, String fullAddress) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("min_max_temperature", minMaxTemperature);
        editor.putString("current_temperature", currentTemperature);
        editor.putString("publish_time", publishTime);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("current_date", currentDate);
        editor.putString("full_address", fullAddress);
        editor.commit();
    }
}
