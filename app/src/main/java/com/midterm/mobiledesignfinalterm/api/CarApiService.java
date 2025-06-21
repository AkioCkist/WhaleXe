package com.midterm.mobiledesignfinalterm.api;

import com.midterm.mobiledesignfinalterm.models.ApiResponse;
import com.midterm.mobiledesignfinalterm.models.Car;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Interface định nghĩa các API endpoints liên quan đến xe
 */
public interface CarApiService {

    /**
     * Lấy danh sách tất cả các xe
     */
    @GET("vehicles.php")
    Call<Map<String, Object>> getAllCars(
            @Query("limit") Integer limit,
            @Query("offset") Integer offset,
            @Query("status") String status
    );

    /**
     * Lấy thông tin chi tiết của xe theo ID
     * @param carId ID của xe cần lấy thông tin
     */
    @GET("vehicles.php")
    Call<Car> getCarDetails(@Query("id") int carId);

    /**
     * Tìm kiếm xe
     */
    @GET("vehicles.php")
    Call<Map<String, Object>> searchCars(
            @Query("search") String search,
            @Query("type") String vehicleType,
            @Query("location") String location,
            @Query("status") String status,
            @Query("brand") String brand,
            @Query("seats") Integer seats,
            @Query("fuel_type") String fuelType
    );

    /**
     * Lấy xe yêu thích
     */
    @GET("vehicles.php")
    Call<Map<String, Object>> getFavoriteCars(
            @Query("favorites") boolean favorites,
            @Query("status") String status
    );

    /**
     * Cập nhật trạng thái yêu thích của xe
     */
    @POST("vehicles.php")
    Call<Map<String, Object>> toggleFavorite(@Body Map<String, Object> body);

    /**
     * Cập nhật trạng thái của xe
     */
    @FormUrlEncoded
    @POST("vehicles.php")
    Call<Map<String, Object>> updateVehicleStatus(
            @Field("action") String action,
            @Field("vehicle_id") int vehicleId,
            @Field("status") String status
    );
}