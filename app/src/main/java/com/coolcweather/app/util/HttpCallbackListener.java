package com.coolcweather.app.util;

/**
 * Created by Administrator on 2016/6/21.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
