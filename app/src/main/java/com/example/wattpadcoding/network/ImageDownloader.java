package com.example.wattpadcoding.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.wattpadcoding.data.DataManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImageDownloader {
    private final DataManager dataManager;
    private final ImageLoadedCallback imageLoadedCallback;

    public ImageDownloader(DataManager dataManager,
                           ImageLoadedCallback imageLoadedCallback) {
        this.dataManager = dataManager;
        this.imageLoadedCallback = imageLoadedCallback;
    }

    /**
     * This downloaded all images that are passed by data manager, and also passes the image to
     * data manager to save it for future use.
     * Here we create as many threads as there are images to download to process faster, and these
     * are all executed in parallel to process them all at once
     * One downside here is that executor service waits for thread termination, where as a better
     * approach would be to have a poll that polls for all threads completed and once successful,
     * then call the callback
     * @param imageList
     */
    public void downloadImages(HashMap<String, String> imageList) {
        List<Runnable> runnableList = new ArrayList<>();
        for (String key : imageList.keySet()) {
            runnableList.add(() -> {
                Bitmap bitmap;
                try {
                    InputStream inputStream = new URL(imageList.get(key)).openStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    dataManager.saveImage(bitmap, key + ".jpeg");
                } catch (IOException e) {
                    e.printStackTrace();
                    imageLoadedCallback.imageProcessingError();
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
}
