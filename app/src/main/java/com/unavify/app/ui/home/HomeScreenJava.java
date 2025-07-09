package com.unavify.app.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unavify.app.R;
import com.unavify.app.ui.auth.AuthViewModelJava;
import com.unavify.app.ui.profile.AddPostActivity;
import com.unavify.app.ui.profile.EditProfileActivity;
import dagger.hilt.android.AndroidEntryPoint;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.Query;

@AndroidEntryPoint
public class HomeScreenJava extends AppCompatActivity {
    private AuthViewModelJava viewModel;
    private ShapeableImageView profileImageView;
    private TextView welcomeText;
    private TextView subtitleText;
    private String currentUsername = null;
    private String currentProfileImageUrl = null;
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d("FEED_DEBUG", "onCreate: HomeScreenJava started");
        welcomeText = findViewById(R.id.welcome_text);
        subtitleText = findViewById(R.id.subtitle_text);
        Button signOutButton = findViewById(R.id.sign_out_button);
        com.google.android.material.floatingactionbutton.FloatingActionButton fabAddPost = findViewById(R.id.fab_add_post);
        com.google.android.material.floatingactionbutton.FloatingActionButton fabEditProfile = findViewById(R.id.fab_edit_profile);
        profileImageView = findViewById(R.id.profile_image);

        subtitleText.setText("Your Food Recipe Community");

        // Get the ViewModel using Hilt's ViewModelProvider
        viewModel = new ViewModelProvider(this).get(AuthViewModelJava.class);

        // Load user profile data
        loadUserProfile();

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.signOut();
                Intent intent = new Intent(HomeScreenJava.this, com.unavify.app.ui.auth.LoginScreenJava.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        fabAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPostActivity.class);
            startActivityForResult(intent, ADD_POST_REQUEST_CODE);
        });

        fabEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            if (currentUsername != null) intent.putExtra("username", currentUsername);
            if (currentProfileImageUrl != null) intent.putExtra("profileImageUrl", currentProfileImageUrl);
            startActivity(intent);
        });

        recyclerViewPosts = findViewById(R.id.recycler_view_posts);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(this, postList);
        recyclerViewPosts.setAdapter(postAdapter);

        // Load posts
        loadPosts();
    }

    private static final int ADD_POST_REQUEST_CODE = 1001;
    private static final int COMMENT_REQUEST_CODE = 1002;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.d("FEED_DEBUG", "onActivityResult called: requestCode=" + requestCode + ", resultCode=" + resultCode);
        
        if (requestCode == ADD_POST_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.d("FEED_DEBUG", "AddPostActivity returned with RESULT_OK");
            if (data != null) {
                boolean postUploaded = data.getBooleanExtra("post_uploaded", false);
                Log.d("FEED_DEBUG", "post_uploaded extra: " + postUploaded);
                if (postUploaded) {
                    // Post was uploaded successfully, refresh the feed
                    Log.d("FEED_DEBUG", "Post uploaded successfully, refreshing feed");
                    loadPosts();
                }
            } else {
                Log.d("FEED_DEBUG", "data is null");
            }
        } else if (requestCode == COMMENT_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.d("FEED_DEBUG", "CommentActivity returned with RESULT_OK, refreshing feed");
            loadPosts(); // Refresh feed to update comment counts
        } else {
            Log.d("FEED_DEBUG", "Not the right request code or result code");
        }
    }

    private void loadUserProfile() {
        String phoneNumber = FirebaseAuth.getInstance().getCurrentUser() != null ?
            FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() : null;
        
        if (phoneNumber != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(phoneNumber)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        this.currentUsername = username;
                        this.currentProfileImageUrl = profileImageUrl;
                        
                        // Update welcome text
                        if (username != null && !username.isEmpty()) {
                            welcomeText.setText("Welcome, " + username + "! \uD83C\uDF7D\uFE0F");
                        } else {
                            welcomeText.setText("Welcome to Unavify! \uD83C\uDF7D\uFE0F");
                        }
                        
                        // Load profile image
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .circleCrop()
                                .into(profileImageView);
                        }
                    } else {
                        welcomeText.setText("Welcome to Unavify! \uD83C\uDF7D\uFE0F");
                    }
                })
                .addOnFailureListener(e -> {
                    welcomeText.setText("Welcome to Unavify! \uD83C\uDF7D\uFE0F");
                });
        } else {
            welcomeText.setText("Welcome to Unavify! \uD83C\uDF7D\uFE0F");
        }
    }

    private void loadPosts() {
        Log.d("FEED_DEBUG", "loadPosts: Fetching posts from Firestore");
        FirebaseFirestore.getInstance()
            .collection("posts")
            .whereEqualTo("isPublic", true)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                postList.clear();
                int fetchedCount = querySnapshot.getDocuments().size();
                Log.d("FEED_DEBUG", "Fetched posts: " + fetchedCount);
                Toast.makeText(this, "Fetched posts: " + fetchedCount, Toast.LENGTH_LONG).show();
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String postId = doc.getId();
                    String userId = doc.getString("userId");
                    String caption = doc.getString("caption");
                    String phone = doc.getString("phone");
                    boolean isPublic = doc.contains("isPublic") ? doc.getBoolean("isPublic") : true;
                    List<String> taggedUserPhones = (List<String>) doc.get("taggedUserPhones");
                    String mediaUrl = doc.getString("mediaUrl");
                    Log.d("FEED_DEBUG", "Processing post: postId=" + postId + ", caption=" + caption + ", phone=" + phone + ", mediaUrl=" + mediaUrl);
                    // Fetch user info for each post using phone as document ID
                    if (phone != null && !phone.isEmpty()) {
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(phone)
                            .get()
                            .addOnSuccessListener(userDoc -> {
                                String username = userDoc.getString("username");
                                String userProfileImageUrl = userDoc.getString("profileImageUrl");
                                Log.d("FEED_DEBUG", "Fetched user info for phone=" + phone + ": username=" + username + ", profileImageUrl=" + userProfileImageUrl);
                                
                                // Fetch comment count for this post
                                Log.d("FEED_DEBUG", "About to fetch comments for postId: " + postId + " with caption: " + caption);
                                FirebaseFirestore.getInstance()
                                    .collection("posts")
                                    .document(postId)
                                    .collection("comments")
                                    .get()
                                    .addOnSuccessListener(commentsSnapshot -> {
                                        int commentCount = commentsSnapshot.size();
                                        Log.d("FEED_DEBUG", "Comment count for post " + postId + " (caption: " + caption + "): " + commentCount);
                                        postList.add(new Post(postId, userId, username, userProfileImageUrl, caption, isPublic, taggedUserPhones, mediaUrl, commentCount));
                                        postAdapter.notifyDataSetChanged();
                                        Log.d("FEED_DEBUG", "Adapter notified after adding post with user info and comment count: postId=" + postId + ", commentCount=" + commentCount);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FEED_DEBUG", "Failed to fetch comment count for post " + postId, e);
                                        postList.add(new Post(postId, userId, username, userProfileImageUrl, caption, isPublic, taggedUserPhones, mediaUrl, 0));
                                        postAdapter.notifyDataSetChanged();
                                        Log.d("FEED_DEBUG", "Adapter notified after adding post with user info and default comment count: postId=" + postId);
                                    });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FEED_DEBUG", "Failed to fetch user info for phone=" + phone, e);
                                // Fetch comment count even for default user info
                                FirebaseFirestore.getInstance()
                                    .collection("posts")
                                    .document(postId)
                                    .collection("comments")
                                    .get()
                                    .addOnSuccessListener(commentsSnapshot -> {
                                        int commentCount = commentsSnapshot.size();
                                        postList.add(new Post(postId, userId, "User", null, caption, isPublic, taggedUserPhones, mediaUrl, commentCount));
                                        postAdapter.notifyDataSetChanged();
                                        Log.d("FEED_DEBUG", "Adapter notified after adding post with default user info and comment count: postId=" + postId);
                                    })
                                    .addOnFailureListener(commentError -> {
                                        postList.add(new Post(postId, userId, "User", null, caption, isPublic, taggedUserPhones, mediaUrl, 0));
                                        postAdapter.notifyDataSetChanged();
                                        Log.d("FEED_DEBUG", "Adapter notified after adding post with default user info and default comment count: postId=" + postId);
                                    });
                            });
                    } else {
                        // Fetch comment count for posts with no phone
                        FirebaseFirestore.getInstance()
                            .collection("posts")
                            .document(postId)
                            .collection("comments")
                            .get()
                            .addOnSuccessListener(commentsSnapshot -> {
                                int commentCount = commentsSnapshot.size();
                                postList.add(new Post(postId, userId, "User", null, caption, isPublic, taggedUserPhones, mediaUrl, commentCount));
                                postAdapter.notifyDataSetChanged();
                                Log.d("FEED_DEBUG", "Adapter notified after adding post with no phone and comment count: postId=" + postId);
                            })
                            .addOnFailureListener(e -> {
                                postList.add(new Post(postId, userId, "User", null, caption, isPublic, taggedUserPhones, mediaUrl, 0));
                                postAdapter.notifyDataSetChanged();
                                Log.d("FEED_DEBUG", "Adapter notified after adding post with no phone and default comment count: postId=" + postId);
                            });
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e("FEED_DEBUG", "Failed to fetch posts from Firestore", e);
            });
    }
} 