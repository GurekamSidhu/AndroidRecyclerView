package com.example.wattpadcoding.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.example.wattpadcoding.network.ImageDownloader;
import com.example.wattpadcoding.network.ImageLoadedCallback;
import com.example.wattpadcoding.network.model.StoryModel;
import com.example.wattpadcoding.network.model.UserModel;
import com.example.wattpadcoding.ui.StoryListAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.WorkerThread;

/**
 * Some code here has been borrowed from greendao documentation, from
 * https://github.com/greenrobot/greenDAO
 */
public class DataManager {
    private final DaoSession daoSession;
    private final Context context;
    private final ImageDownloader imageDownloader;
    private final Handler handler;
    private final HashMap<String, String> imagesToDownload = new HashMap<String, String>();

    public DataManager(DaoSession daoSession, Context context,
                       ImageLoadedCallback imageLoadedCallback) {
        this.daoSession = daoSession;
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
        imageDownloader = new ImageDownloader(this, imageLoadedCallback);
    }

    /**
     *   Rest client upon getting data, calls this method to further process the data,
     *   this clears the db and then stores the user and story dao's and then also calls the
     *   image downloaded to download all teh required images.
      */
    public void saveStoriesToDb(List<StoryModel> stories) {
        new Thread(() -> {
            clearInfoFromDb();
            for (StoryModel storyModel : stories) {
                storeInfoToDb(storyModel);
            }
            imageDownloader.downloadImages(imagesToDownload);
        }).start();
    }

    @WorkerThread
    /**
     * Just clears data of app from previous run if there is new data, just used this to keep app
     * storage limited and also since i didn't have any user session to associated stories with.
     * Ideally would be nice to save the stories id's and associated them with current user so they
     * can be loaded for that user if already in db, this could be achieved using shared prefs
     */
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

    /**
     * Insert two dao's into Db, one for story object and other for the associated author user,
     * we add both user's avatar and story's cover to be downloaded and stored later
     * Call this on background thread since this inserts data to backend.
     * @param storyModel
     */
    @WorkerThread
    private void storeInfoToDb(StoryModel storyModel) {
        User user = extractUserInfo(storyModel.user);
        Story story = new Story();
        story.setId(UUID.randomUUID().toString());
        story.setTitle(storyModel.title);
        story.setCover(storyModel.cover);
        story.setUserId(user.getId());
        daoSession.getStoryDao().insertOrReplace(story);
        imagesToDownload.put(user.getId(), user.getAvatar());
        imagesToDownload.put(story.getId(), story.getCover());
    }

    /**
     * Creates a user and sets key information that comes with the story model from retrofit,
     * and inserts it in dao
     * worker thread since it inserts data into database
     * @param userModel: user Model that is obtained from Story Model
     * @return: User dao, that is passed back
     */
    @WorkerThread
    private User extractUserInfo(UserModel userModel) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(userModel.name);
        user.setAvatar(userModel.avatar);
        user.setFullName(userModel.fullname);
        daoSession.getUserDao().insertOrReplace(user);
        return user;
    }

    /**
     * Saves an image that is downloaded as a bitmap in local storage for future use, since the
     * stories are limited to 10 stories and images to 20, used internal storage, ideally would
     * be nice to store these to external storage to help store more data
     * @param bitmap bitmpa downloaded from internet
     * @param imageName: UUID for file name
     */
    @WorkerThread
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

    /**
     * this gets the data stored in db and passes it as a lists to story list adapter for display in
     * recycler view.
     * Ideally would be nice to create predefined queries, that can be called quickly here and also
     * add more params to filter stories in asc or dsc order
     * @param storyListAdapter : list adapter
     */
    public void fetchAndSendDataToAdapter(
            StoryListAdapter storyListAdapter) {
        new Thread(() -> {
            List<Story> storyList = daoSession.getStoryDao().queryBuilder()
                    .limit(10).build().list();
            List<User> userList = daoSession.getUserDao().queryBuilder()
                    .limit(10).list();
            // This has to be called on UI thread, used this to avoid creating a new callback that
            // would be called upon db query loaded
            handler.post(() -> storyListAdapter.updateLists(storyList, userList));
        }).start();
    }

    /**
     * Loads image from storage and passes it to callback
     * @param fileName: file to load
     * @param callback call back to call
     */
    @WorkerThread
    public void loadBitmap(String fileName, StoryListAdapter.ImageFetchedFromStorage callback) {
        new Thread(() -> {
            String filePath = context.getFilesDir() + "/" + fileName + ".jpeg";
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(new File(filePath));
                Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                fileInputStream.close();
                handler.post(() -> callback.onImageFetched(bitmap));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
