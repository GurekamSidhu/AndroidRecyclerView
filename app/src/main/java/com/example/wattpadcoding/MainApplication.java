package com.example.wattpadcoding;

import android.app.Application;

import com.example.wattpadcoding.data.DaoMaster;
import com.example.wattpadcoding.data.DaoSession;

import org.greenrobot.greendao.database.Database;

public class MainApplication extends Application {
    private String DATABASE_NAME = "stories-db";
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        setupDataBase();
    }

    /**
     * Using devopen helper but ideally should create a custom helper to init the db for specfic use
     */
    private void setupDataBase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, DATABASE_NAME);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
