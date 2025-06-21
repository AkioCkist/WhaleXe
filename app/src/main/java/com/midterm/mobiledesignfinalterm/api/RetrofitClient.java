package com.midterm.mobiledesignfinalterm.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class quản lý việc khởi tạo và cung cấp instance của Retrofit để gọi API
 */
public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2/myapi/";
    private static Retrofit retrofit = null;

    /**
     * Cung cấp một instance của Retrofit để gọi API
     * @return Instance của Retrofit
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Tạo instance của HttpLogging Interceptor để log request và response
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Tạo instance của OkHttpClient với interceptor
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

            // Cấu hình Gson
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            // Tạo instance của Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    /**
     * Cung cấp service để gọi API liên quan đến xe
     * @return Instance của CarApiService
     */
    public static CarApiService getCarApiService() {
        return getClient().create(CarApiService.class);
    }

    /**
     * Cung cấp service để gọi API liên quan đến favorite
     * @return Instance của FavoriteApiService
     */
    public static FavoriteApiService getFavoriteApiService() {
        return getClient().create(FavoriteApiService.class);
    }
}
