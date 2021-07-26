package com.example.wattpadcoding.network;

import com.example.wattpadcoding.data.DataManager;
import com.example.wattpadcoding.network.api.StoryApi;
import com.example.wattpadcoding.network.model.ResponseModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient implements Callback<ResponseModel> {
    private static final String BASE_URL = "https://www.wattpad.com/api/v3/";
    private DataManager dataManager;
    ImageLoadedCallback imageLoadedCallback;

    public RestClient(DataManager dataManager, ImageLoadedCallback imageLoadedCallback) {
        this.dataManager = dataManager;
        this.imageLoadedCallback = imageLoadedCallback;
    }

    /**
     * This is a call using retrofit which maps the response to models predefined
     * Just easy to use and saved me time
     * Ideally would create a separate client which calls the endpoint to fetch stories and with
     * dedicated search options
     */
    public void getStories() {
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        StoryApi storyApi = retrofit.create(StoryApi.class);
        Call<ResponseModel> call = storyApi.getStories();
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
        if (response.isSuccessful()) {
            // Extract the response and call data manager to save the stories
            ResponseModel responseModel = response.body();
            if (responseModel == null) {
                imageLoadedCallback.imageProcessingError();
            } else {
                // Call data manager to save stories if there are some to save
                dataManager.saveStoriesToDb(responseModel.stories);
            }
        } else {
            imageLoadedCallback.imageProcessingError();
        }
    }

    @Override
    public void onFailure(Call<ResponseModel> call, Throwable t) {
        t.printStackTrace();
        imageLoadedCallback.imageProcessingError();
    }
}
