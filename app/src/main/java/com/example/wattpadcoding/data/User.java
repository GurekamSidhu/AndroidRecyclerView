package com.example.wattpadcoding.data;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class User {
    @Id
    Long key;

    String id;

    String name;

    String avatar;

    String fullName;

    @Generated(hash = 939962341)
    public User(Long key, String id, String name, String avatar, String fullName) {
        this.key = key;
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.fullName = fullName;
    }

    @Generated(hash = 586692638)
    public User() {
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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
