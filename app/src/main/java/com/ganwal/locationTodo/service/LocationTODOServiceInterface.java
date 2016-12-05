package com.ganwal.locationTodo.service;

import com.ganwal.locationTodo.db.LocationTodo;
import com.ganwal.locationTodo.db.User;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface LocationTODOServiceInterface {

    @POST("users/")
    Call<User> createUser(@Header("Authorization") String authHeader, @Body User user);

    @GET("users/{id}")
    Call<User> findUserById(@Header("Authorization") String authHeader, @Path("id") Long id);

    @GET("users")
    Call<List<User>> findUsers(@Header("Authorization") String authHeader, @Query("gId") String gId);

    @POST("users/{id}/todos/")
    Call<LocationTodo> createToDoByUser(@Header("Authorization") String authHeader, @Path("id") Long id, @Body LocationTodo todo);

    @POST("users/{id}/todos/{todo_id}")
    Call<LocationTodo> updateToDoByUser(@Header("Authorization") String authHeader, @Path("id") Long id, @Path("todo_id") Long todo_id, @Body LocationTodo todo);

    @GET("users/{id}/todos/{todo_id}")
    Call<LocationTodo> findTodoById(@Header("Authorization") String authHeader, @Path("id") Long id, @Path("todo_id") Long todo_id);

    @GET("users/{id}/todos")
    Call<List<LocationTodo>> findTodosByUser(@Header("Authorization") String authHeader, @Path("id") Long id);

    @DELETE("users/{id}/todos/{todo_id}")
    Call<ResponseBody> deleteToDoByUser(@Header("Authorization") String authHeader, @Path("id") Long id, @Path("todo_id") Long todo_id);




}
