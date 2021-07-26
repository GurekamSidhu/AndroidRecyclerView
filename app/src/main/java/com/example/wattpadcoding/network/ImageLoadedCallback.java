package com.example.wattpadcoding.network;

/**
 * Just a callback for data manager and rest client to let activity know data is ready
 */
public interface ImageLoadedCallback {
    void imageProcessingFinished();

    void imageProcessingError();
}
