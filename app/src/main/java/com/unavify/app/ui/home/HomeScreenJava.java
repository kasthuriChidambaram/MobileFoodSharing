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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.ImageButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import android.view.MenuItem;
import com.unavify.app.ui.home.UserProfileActivity;

@AndroidEntryPoint
public class HomeScreenJava extends AppCompatActivity {
    private AuthViewModelJava viewModel;
    private String currentUsername = null;
    private String currentProfileImageUrl = null;
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        loadCurrentUserProfile();

        Log.d("FEED_DEBUG", "onCreate: HomeScreenJava started");
        // Removed: ImageButton btnAddPost = findViewById(R.id.btn_add_post);
        // Removed: ImageButton btnEditProfile = findViewById(R.id.btn_edit_profile);
        // Removed: ImageButton btnSignOut = findViewById(R.id.btn_sign_out);

        // Get the ViewModel using Hilt's ViewModelProvider
        viewModel = new ViewModelProvider(this).get(AuthViewModelJava.class);

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    // Already on home, do nothing
                    return true;
                } else if (id == R.id.nav_add_post) {
                    Intent addPostIntent = new Intent(HomeScreenJava.this, AddPostActivity.class);
                    startActivityForResult(addPostIntent, ADD_POST_REQUEST_CODE);
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent editProfileIntent = new Intent(HomeScreenJava.this, EditProfileActivity.class);
                    if (currentUsername != null) editProfileIntent.putExtra("username", currentUsername);
                    if (currentProfileImageUrl != null) editProfileIntent.putExtra("profileImageUrl", currentProfileImageUrl);
                    startActivity(editProfileIntent);
                    return true;
                } else if (id == R.id.nav_sign_out) {
                    viewModel.signOut();
                    Intent signOutIntent = new Intent(HomeScreenJava.this, com.unavify.app.ui.auth.LoginScreenJava.class);
                    signOutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(signOutIntent);
                    finish();
                    return true;
                }
                return false;
            }
        });

        recyclerViewPosts = findViewById(R.id.recycler_view_posts);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(this, postList);
        recyclerViewPosts.setAdapter(postAdapter);

        // Setup users RecyclerView
        recyclerViewUsers = findViewById(R.id.recycler_view_users);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        userAdapter = new UserAdapter(this, userList);
        recyclerViewUsers.setAdapter(userAdapter);

        // Set click listener for user profile circles
        userAdapter.setOnUserClickListener(user -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("user_phone", user.phone);
            intent.putExtra("username", user.username);
            intent.putExtra("profile_image_url", user.profileImageUrl);
            startActivity(intent);
        });

        // Load posts and users
        loadPosts();
        loadCommunityMembers();
    }

    private void loadCurrentUserProfile() {
        String phone = FirebaseAuth.getInstance().getCurrentUser() != null ?
            FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() : null;
        if (phone == null) return;
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(phone)
            .get()
            .addOnSuccessListener(userDoc -> {
                currentUsername = userDoc.getString("username");
                currentProfileImageUrl = userDoc.getString("profileImageUrl");
            });
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

    private void loadCommunityMembers() {
        Log.d("FEED_DEBUG", "loadCommunityMembers: Fetching users from Firestore");
        FirebaseFirestore.getInstance()
            .collection("users")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                userList.clear();
                int fetchedCount = querySnapshot.getDocuments().size();
                Log.d("FEED_DEBUG", "Fetched users: " + fetchedCount);
                
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String phone = doc.getId();
                    String username = doc.getString("username");
                    String profileImageUrl = doc.getString("profileImageUrl");
                    
                    Log.d("FEED_DEBUG", "Processing user: phone=" + phone + ", username=" + username);
                    userList.add(new User(phone, username, profileImageUrl));
                }
                
                userAdapter.notifyDataSetChanged();
                Log.d("FEED_DEBUG", "User adapter notified, total users: " + userList.size());
            })
            .addOnFailureListener(e -> {
                Log.e("FEED_DEBUG", "Failed to fetch users from Firestore", e);
            });
    }
} 