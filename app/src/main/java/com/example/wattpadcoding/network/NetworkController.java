package com.example.wattpadcoding.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.wattpadcoding.data.DaoSession;
import com.example.wattpadcoding.data.Story;
import com.example.wattpadcoding.data.User;
import com.example.wattpadcoding.network.api.StoryApi;
import com.example.wattpadcoding.network.model.ResponseModel;
import com.example.wattpadcoding.network.model.StoryModel;
import com.example.wattpadcoding.network.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkController implements Callback<ResponseModel> {
    private static final String BASE_URL = "https://www.wattpad.com/api/v3/";
    private DaoSession daoSession;
    private Context context;
    HashMap<String, String> imageList = new HashMap<String, String>();
    ImageLoadedCallback imageLoadedCallback;

    public NetworkController(DaoSession daoSession, Context applicationContext,
                             ImageLoadedCallback imageLoadedCallback) {
        this.daoSession = daoSession;
        this.context = applicationContext;
        this.imageLoadedCallback = imageLoadedCallback;
    }

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
            ResponseModel responseModel = response.body();
            assert responseModel != null;
            clearInfoFromDb();
            for (StoryModel storyModel : responseModel.stories) {
                storeInfoToDb(storyModel);
            }
            downloadImages();
        } else {
            System.out.println(response.errorBody());
            imageLoadedCallback.imageProcessingError();
        }
    }

    @Override
    public void onFailure(Call<ResponseModel> call, Throwable t) {
        t.printStackTrace();
        imageLoadedCallback.imageProcessingError();
    }

    private void clearInfoFromDb() {
        daoSession.getStoryDao().deleteAll();
        daoSession.getUserDao().deleteAll();
        File dir = context.getFilesDir();
        if (dir.isDirectory()) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                file.delete();
            }
        }
    }

    private void storeInfoToDb(StoryModel storyModel) {
        User user = extractUserInfo(storyModel.user);
        Story story = new Story();
        story.setId(UUID.randomUUID().toString());
        story.setTitle(storyModel.title);
        story.setCover(storyModel.cover);
        story.setUserId(user.getId());
        daoSession.getStoryDao().insertOrReplace(story);
        imageList.put(user.getId(), user.getAvatar());
        imageList.put(story.getId(), story.getCover());
    }

    private User extractUserInfo(UserModel userModel) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(userModel.name);
        user.setAvatar(userModel.avatar);
        user.setFullName(userModel.fullname);
        daoSession.getUserDao().insertOrReplace(user);
        return user;
    }

    private void downloadImages() {
        List<Runnable> runnableList = new ArrayList<>();
        for (String key : imageList.keySet()) {
            runnableList.add(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap;
                    try {
                        InputStream inputStream = new URL(imageList.get(key)).openStream();
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        saveImage(bitmap, key + ".jpeg");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        ExecutorService executorService = Executors.newCachedThreadPool();
        for (Runnable runnable : runnableList) {
            executorService.execute(runnable);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        imageLoadedCallback.imageProcessingFinished();
    }

    public void saveImage(Bitmap bitmap, String imageName) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = context.openFileOutput(imageName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
