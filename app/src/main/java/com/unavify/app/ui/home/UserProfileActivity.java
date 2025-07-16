package com.unavify.app.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.unavify.app.R;
import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.widget.ImageButton;

public class UserProfileActivity extends AppCompatActivity {
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private String userPhone;
    private String username;
    private String profileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Get user data from intent
        userPhone = getIntent().getStringExtra("user_phone");
        username = getIntent().getStringExtra("username");
        profileImageUrl = getIntent().getStringExtra("profile_image_url");

        Log.d("USER_PROFILE", "onCreate - userPhone: " + userPhone + ", username: " + username + ", profileImageUrl: " + profileImageUrl);

        if (userPhone == null) {
            Log.e("USER_PROFILE", "userPhone is null, finishing activity");
            finish();
            return;
        }

        setupUI();
        loadUserPosts();
    }

    private void setupUI() {
        // Set up header
        TextView usernameText = findViewById(R.id.text_username);
        ShapeableImageView profileImage = findViewById(R.id.image_profile);
        TextView backButton = findViewById(R.id.button_back);
        
        // Set up back button
        backButton.setOnClickListener(v -> finish());
        
        if (username != null) {
            usernameText.setText(username);
        }

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this)
                .load(profileImageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .circleCrop()
                .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Set up RecyclerView
        recyclerViewPosts = findViewById(R.id.recycler_view_posts);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(this, postList);
        recyclerViewPosts.setAdapter(postAdapter);
    }

    private void loadUserPosts() {
        Log.d("USER_PROFILE", "Loading posts for user: " + userPhone);
        
        // First, try a simple query by phone number
        FirebaseFirestore.getInstance()
            .collection("posts")
            .whereEqualTo("phone", userPhone)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                postList.clear();
                int fetchedCount = querySnapshot.getDocuments().size();
                Log.d("USER_PROFILE", "Fetched posts for phone " + userPhone + ": " + fetchedCount);
                
                if (fetchedCount == 0) {
                    Log.d("USER_PROFILE", "No posts found for user: " + userPhone);
                    return;
                }
                
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String postId = doc.getId();
                    String userId = doc.getString("userId");
                    String caption = doc.getString("caption");
                    String phone = doc.getString("phone");
                    boolean isPublic = doc.contains("isPublic") ? doc.getBoolean("isPublic") : true;
                    List<String> taggedUserPhones = (List<String>) doc.get("taggedUserPhones");
                    String mediaUrl = doc.getString("mediaUrl");
                    
                    Log.d("USER_PROFILE", "Processing post: postId=" + postId + ", caption=" + caption + ", phone=" + phone);
                    
                    // Only add public posts
                    if (isPublic) {
                        // Fetch comment count for this post
                        FirebaseFirestore.getInstance()
                            .collection("posts")
                            .document(postId)
                            .collection("comments")
                            .get()
                            .addOnSuccessListener(commentsSnapshot -> {
                                int commentCount = commentsSnapshot.size();
                                postList.add(new Post(postId, userId, username, profileImageUrl, caption, isPublic, taggedUserPhones, mediaUrl, commentCount));
                                postAdapter.notifyDataSetChanged();
                                Log.d("USER_PROFILE", "Added post with comment count: " + commentCount);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("USER_PROFILE", "Failed to fetch comment count for post " + postId, e);
                                postList.add(new Post(postId, userId, username, profileImageUrl, caption, isPublic, taggedUserPhones, mediaUrl, 0));
                                postAdapter.notifyDataSetChanged();
                            });
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e("USER_PROFILE", "Failed to fetch posts from Firestore for user: " + userPhone, e);
            });
    }
} 