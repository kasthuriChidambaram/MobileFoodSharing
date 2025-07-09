package com.unavify.app.ui.home;

import java.util.List;

public class Post {
    public String postId;
    public String userId;
    public String username;
    public String userProfileImageUrl;
    public String caption;
    public boolean isPublic;
    public List<String> taggedUserPhones;
    public String mediaUrl;
    public int commentCount;

    public Post(String postId, String userId, String username, String userProfileImageUrl, String caption, boolean isPublic, List<String> taggedUserPhones, String mediaUrl) {
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.userProfileImageUrl = userProfileImageUrl;
        this.caption = caption;
        this.isPublic = isPublic;
        this.taggedUserPhones = taggedUserPhones;
        this.mediaUrl = mediaUrl;
        this.commentCount = 0;
    }

    public Post(String postId, String userId, String username, String userProfileImageUrl, String caption, boolean isPublic, List<String> taggedUserPhones, String mediaUrl, int commentCount) {
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.userProfileImageUrl = userProfileImageUrl;
        this.caption = caption;
        this.isPublic = isPublic;
        this.taggedUserPhones = taggedUserPhones;
        this.mediaUrl = mediaUrl;
        this.commentCount = commentCount;
    }
} 