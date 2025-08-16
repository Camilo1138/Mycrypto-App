package com.example.myapplication.utils;

import org.json.JSONObject;

public class CustomDeviceState {
    private String userId;
    private boolean subscribed;

    public CustomDeviceState(JSONObject json) {
        this.userId = json.optString("userId");
        this.subscribed = json.optBoolean("subscribed");
    }

    public String getUserId() {
        return userId;
    }

    public boolean isSubscribed() {
        return subscribed;
    }


}