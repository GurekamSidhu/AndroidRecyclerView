package com.example.wattpadcoding.network.model;

import java.util.List;

/**
 * This models the response from api, which contains stories and the next url to call
 */
public class ResponseModel {
    public List<StoryModel> stories;
    public String nextUrl;
}
