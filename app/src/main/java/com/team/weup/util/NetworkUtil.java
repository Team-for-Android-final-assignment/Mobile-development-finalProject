package com.team.weup.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class NetworkUtil {
    private static Retrofit retrofit;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            String baseUrl = "http://123.56.85.195:8080/";

            // 配置 Jackson
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                    .addConverterFactory(JacksonConverterFactory.create(mapper)).build();
        }
        return retrofit;
    }
}
