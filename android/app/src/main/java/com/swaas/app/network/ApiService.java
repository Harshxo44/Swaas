package com.swaas.app.network;

import com.swaas.app.model.WaterBody;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit interface defining all API endpoints for the SWAAS Node.js backend.
 */
public interface ApiService {

    /** POST /api/auth/register */
    @POST("auth/register")
    Call<Map<String, Object>> register(@Body Map<String, String> body);

    /** POST /api/auth/login */
    @POST("auth/login")
    Call<Map<String, Object>> login(@Body Map<String, String> body);

    /** GET /api/waterbodies — All water bodies */
    @GET("waterbodies")
    Call<List<WaterBody>> getAllWaterBodies(@Header("Authorization") String token);

    /** GET /api/waterbodies/nearby?lat=&lng=&radius= */
    @GET("waterbodies/nearby")
    Call<List<WaterBody>> getNearbyWaterBodies(
            @Header("Authorization") String token,
            @Query("lat") double latitude,
            @Query("lng") double longitude,
            @Query("radius") double radiusKm
    );

    /** GET /api/waterbodies/{id} */
    @GET("waterbodies/{id}")
    Call<WaterBody> getWaterBodyById(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    /** POST /api/waterbodies — Add new water body (Contributors only) */
    @POST("waterbodies")
    Call<WaterBody> addWaterBody(
            @Header("Authorization") String token,
            @Body WaterBody waterBody
    );

    /** PUT /api/waterbodies/{id} — Update existing water body (Contributors only) */
    @PUT("waterbodies/{id}")
    Call<WaterBody> updateWaterBody(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body WaterBody waterBody
    );

    /** GET /api/waterbodies/search?query= */
    @GET("waterbodies/search")
    Call<List<WaterBody>> searchWaterBodies(
            @Header("Authorization") String token,
            @Query("query") String query
    );
}
