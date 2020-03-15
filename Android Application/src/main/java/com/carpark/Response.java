package com.carpark;

import com.google.gson.annotations.SerializedName;

public class Response {

    @SerializedName("status")
    String status;

    @SerializedName("fare")
    int fare;
}
