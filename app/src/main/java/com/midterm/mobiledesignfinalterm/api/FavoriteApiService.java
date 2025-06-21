package com.midterm.mobiledesignfinalterm.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Interface for favorite cars related API calls
 */
public interface FavoriteApiService {

    /**
     * Get user's favorite cars
     * @param account_id User account ID
     */
    @GET("favorites.php")
    Call<Map<String, Object>> getUserFavorites(@Query("account_id") int account_id);

    /**
     * Toggle favorite status for a car
     */
    @POST("favorites.php")
    Call<Map<String, Object>> toggleFavorite(@Body Map<String, Object> body);
}
