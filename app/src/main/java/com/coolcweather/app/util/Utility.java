package com.coolcweather.app.util;

import android.text.TextUtils;

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
            , String response, int provinceId){
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
           , String response, int cityId){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONObject(response).getJSONObject("str").getJSONArray("regions");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(jsonObject.getString("name"));
                    county.setCountyCode(jsonObject.getString("id"));
                    county.setCityId(cityId);
                    //将解析出来的数据存储到City表中
                    coolWeatherDB.saveCounty(county);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

}
