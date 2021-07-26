package com.example.wattpadcoding;

import androidx.annotation.MainThread;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.wattpadcoding.data.DataManager;
import com.example.wattpadcoding.network.ImageLoadedCallback;
import com.example.wattpadcoding.network.RestClient;
import com.example.wattpadcoding.ui.StoryListAdapter;

public class MainActivity extends AppCompatActivity {
    private StoryListAdapter storyListAdapter;
    private DataManager dataManager;
    private RestClient restClient;
    private Handler handler;
    private ImageLoadedCallback imageLoadedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler(Looper.getMainLooper());

        // Setup data loaded callback, to update UI once the fetch call is available
        initImageLoadedCallback();
        initDataManagerAndRestClient();

        // Setup recycler view and it's adapter
        RecyclerView recyclerView = findViewById(R.id.story_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        initStoryListAdapter();
        recyclerView.setAdapter(storyListAdapter);

        // Call to load stories or display the ones stored for offline use
        if (isNetworkConnected()) {
            restClient.getStories();
        } else {
            finishLoadingImages();
        }
    }

    @MainThread
    private void initImageLoadedCallback() {
        imageLoadedCallback = new ImageLoadedCallback() {
            @Override
            public void imageProcessingFinished() {
                // This callback is called from a background thread, so we need to make sure that
                // finishLoadingImages which contains a toast is called on UI thread.
                handler.post(() -> finishLoadingImages());
            }

            @Override
            public void imageProcessingError() {
                // This callback is called from a background thread, so we need to make sure that
                // finishLoadingImages which contains a toast is called on UI thread.
                handler.post(() -> showErrorDialog());
            }
        };
    }

    @UiThread
    private void finishLoadingImages() {
        if (isNetworkConnected()) {
            Toast.makeText(this, "Stories saved for offline use",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Stories loaded from storage",
                    Toast.LENGTH_LONG).show();
        }
        // We use data Manager to fetch, load and save data for future use
        dataManager.fetchAndSendDataToAdapter(storyListAdapter);
    }

    @UiThread
    private void showErrorDialog() {
        if (isNetworkConnected()) {
            Toast.makeText(this, "Stories failed to load from internet",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please connect to internet to sync stories",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Would be even nicer to have a dedicated network manager, but since the use for network was
     * limited, just created a function for that purpose
     *
     * @return true if network connected
     */
    @MainThread
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @MainThread
    private void initDataManagerAndRestClient() {
        dataManager = new DataManager(getApp().getDaoSession(), getApplicationContext(),
                imageLoadedCallback);
        restClient = new RestClient(dataManager, imageLoadedCallback);
    }

    @MainThread
    private void initStoryListAdapter() {
        storyListAdapter = new StoryListAdapter(getApp().getDaoSession().getStoryDao().loadAll(),
                getApp().getDaoSession().getUserDao().loadAll(), this, dataManager);
    }

    private MainApplication getApp() {
        return (MainApplication) getApplicationContext();
    }
}