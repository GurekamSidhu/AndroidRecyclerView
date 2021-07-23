package com.example.wattpadcoding.network.api;

import com.example.wattpadcoding.network.model.ResponseModel;
import com.example.wattpadcoding.network.model.StoryModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StoryApi {

    @GET("stories?offset=0&limit=10&fields=stories(id,title,cover,user)&filter=new")
    Call<ResponseModel> getStories();
}
