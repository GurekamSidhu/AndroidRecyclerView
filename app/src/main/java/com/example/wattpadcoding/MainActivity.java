package com.example.wattpadcoding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import com.example.wattpadcoding.data.Story;
import com.example.wattpadcoding.data.StoryDao;
import com.example.wattpadcoding.data.User;
import com.example.wattpadcoding.network.ImageLoadedCallback;
import com.example.wattpadcoding.network.NetworkController;
import com.example.wattpadcoding.ui.StoryListAdapter;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    StoryListAdapter storyListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.story_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        storyListAdapter = new StoryListAdapter(getApp().getDaoSession().getStoryDao().loadAll(),
                getApp().getDaoSession().getUserDao().loadAll(), this);
        recyclerView.setAdapter(storyListAdapter);

        NetworkController
                networkController = new NetworkController(getApp().getDaoSession(), getApplicationContext(),
                new ImageLoadedCallback() {
                    @Override
                    public void imageProcessingFinished() {
                        finishLoadingImages();
                    }

                    @Override
                    public void imageProcessingError() {
                        showErrorDialog();
                    }
                });
        if (isNetworkConnected()) {
            networkController.getStories();
        } else {
            finishLoadingImages();
        }
    }

    private void showErrorDialog() {
        if (isNetworkConnected()) {
            Toast.makeText(this, "Stories failed to load from internet",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please connect to internet to sync stories",
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void finishLoadingImages() {
        Toast.makeText(this, "Stories saved for offline use", Toast.LENGTH_LONG).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Story> storyList = getApp().getDaoSession().getStoryDao().queryBuilder()
                        .limit(10).build().list();
                List<User> userList = getApp().getDaoSession().getUserDao().queryBuilder()
                        .limit(10).list();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        storyListAdapter.updateLists(storyList, userList);
                    }
                });
            }
        }).start();
    }

    private MainApplication getApp() {
        return (MainApplication)getApplicationContext();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}