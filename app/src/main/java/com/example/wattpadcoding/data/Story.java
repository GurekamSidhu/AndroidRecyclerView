package com.example.wattpadcoding.data;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class Story {
    @Id
    Long key;

    String id;

    String title;

    String cover;

    String userId;

    @Generated(hash = 1446500152)
    public Story(Long key, String id, String title, String cover, String userId) {
        this.key = key;
        this.id = id;
        this.title = title;
        this.cover = cover;
        this.userId = userId;
    }

    @Generated(hash = 922655990)
    public Story() {
    }

    public Long getKey() {
        return this.key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return this.cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
