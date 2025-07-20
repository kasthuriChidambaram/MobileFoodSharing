package com.unavify.app.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.unavify.app.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;

public class CommentActivity extends AppCompatActivity {
    private String postId;
    private String postCaption;
    private RecyclerView recyclerView;
    private EditText commentEditText;
    private Button sendButton;
    private ProgressBar progressBar;
    private TextView typingIndicator;
    private CommentAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();
    private ListenerRegistration commentsListener;
    private ListenerRegistration typingListener;
    private Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable typingRunnable;
    private String currentUserId;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        postId = getIntent().getStringExtra("postId");
        postCaption = getIntent().getStringExtra("postCaption");
        
        if (postId == null) {
            Toast.makeText(this, "Error: Post ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        setupUI();
        setupRealTimeListeners();
        loadCurrentUserInfo();
    }

    private void setupUI() {
        recyclerView = findViewById(R.id.recycler_view_comments);
        commentEditText = findViewById(R.id.edit_text_comment);
        sendButton = findViewById(R.id.button_send_comment);
        progressBar = findViewById(R.id.progress_bar_comments);
        typingIndicator = findViewById(R.id.text_typing_indicator);

        // Set post caption if available
        TextView postCaptionText = findViewById(R.id.text_post_caption);
        if (postCaptionText != null && postCaption != null) {
            postCaptionText.setText(postCaption);
        }

        adapter = new CommentAdapter(commentList, postId, postCaption);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendComment());

        // Add typing indicator
        commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    updateTypingStatus(true);
                } else {
                    updateTypingStatus(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRealTimeListeners() {
        // Real-time comments listener
        commentsListener = FirebaseFirestore.getInstance()
                .collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        int previousSize = commentList.size();
                    commentList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            commentList.add(comment);
                        }
                    }
                        
                        // Check if new comments were added
                        boolean hasNewComments = commentList.size() > previousSize;
                        
                    adapter.notifyDataSetChanged();
                        
                        // Scroll to bottom for new comments with animation
                        if (commentList.size() > 0) {
                            if (hasNewComments) {
                                // Animate new comments
                                recyclerView.smoothScrollToPosition(commentList.size() - 1);
                                
                                // Show a subtle notification for new comments
                                if (commentList.size() > previousSize && previousSize > 0) {
                                    Toast.makeText(this, "New comment added!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                });

        // Real-time typing indicator
        typingListener = FirebaseFirestore.getInstance()
                .collection("posts")
                .document(postId)
                .collection("typing")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    List<String> typingUsers = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        String userId = doc.getString("userId");
                        String username = doc.getString("username");
                        long timestamp = doc.getLong("timestamp");
                        
                        // Only show typing for users who typed in the last 5 seconds
                        if (userId != null && !userId.equals(currentUserId) && 
                            System.currentTimeMillis() - timestamp < 5000) {
                            typingUsers.add(username != null ? username : "Someone");
                        }
                    }

                    if (typingUsers.size() > 0) {
                        String typingText = typingUsers.size() == 1 ? 
                            typingUsers.get(0) + " is typing..." :
                            typingUsers.get(0) + " and " + (typingUsers.size() - 1) + " others are typing...";
                        typingIndicator.setText(typingText);
                        typingIndicator.setVisibility(View.VISIBLE);
                        
                        // Add subtle animation to typing indicator
                        typingIndicator.setAlpha(0.7f);
                        typingIndicator.animate().alpha(1.0f).setDuration(500).start();
                    } else {
                        typingIndicator.setVisibility(View.GONE);
                    }
                });
    }

    private void loadCurrentUserInfo() {
        String phone = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() : "";

        if (phone != null && !phone.isEmpty()) {
            FirebaseFirestore.getInstance().collection("users").document(phone).get()
                .addOnSuccessListener(userDoc -> {
                    currentUsername = userDoc.getString("username");
                })
                .addOnFailureListener(e -> {
                    currentUsername = "User";
                });
        } else {
            currentUsername = "User";
        }
    }

    private void updateTypingStatus(boolean isTyping) {
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }

        if (isTyping) {
            // Update typing status
            Map<String, Object> typingData = new HashMap<>();
            typingData.put("userId", currentUserId);
            typingData.put("username", currentUsername);
            typingData.put("timestamp", System.currentTimeMillis());

            FirebaseFirestore.getInstance()
                    .collection("posts")
                    .document(postId)
                    .collection("typing")
                    .document(currentUserId)
                    .set(typingData);
        } else {
            // Remove typing status after delay
            typingRunnable = () -> {
                FirebaseFirestore.getInstance()
                        .collection("posts")
                        .document(postId)
                        .collection("typing")
                        .document(currentUserId)
                        .delete();
            };
            typingHandler.postDelayed(typingRunnable, 2000);
        }
    }

    private void sendComment() {
        String text = commentEditText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        sendButton.setEnabled(false);
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        String phone = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() : "";

        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            sendButton.setEnabled(true);
            return;
        }

        // Get user info first
        FirebaseFirestore.getInstance().collection("users").document(phone).get()
            .addOnSuccessListener(userDoc -> {
                String username = userDoc.getString("username");
                String profileImageUrl = userDoc.getString("profileImageUrl");
                
                Map<String, Object> comment = new HashMap<>();
                comment.put("userId", userId);
                comment.put("username", username != null ? username : "User");
                comment.put("profileImageUrl", profileImageUrl);
                comment.put("text", text);
                comment.put("timestamp", System.currentTimeMillis());

                FirebaseFirestore.getInstance()
                        .collection("posts")
                        .document(postId)
                        .collection("comments")
                        .add(comment)
                        .addOnSuccessListener(documentReference -> {
                            commentEditText.setText("");
                            sendButton.setEnabled(true);
                            updateTypingStatus(false);
                            Toast.makeText(this, "Comment added!", Toast.LENGTH_SHORT).show();
                            
                            // Set result to indicate comment was added
                            setResult(RESULT_OK);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to send comment", Toast.LENGTH_SHORT).show();
                            sendButton.setEnabled(true);
                        });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to get user info", Toast.LENGTH_SHORT).show();
                sendButton.setEnabled(true);
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentsListener != null) {
            commentsListener.remove();
        }
        if (typingListener != null) {
            typingListener.remove();
        }
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }
        // Remove typing status when leaving
        if (currentUserId != null) {
            FirebaseFirestore.getInstance()
                    .collection("posts")
                    .document(postId)
                    .collection("typing")
                    .document(currentUserId)
                    .delete();
        }
    }

    // Comment model
    public static class Comment {
        public String userId;
        public String username;
        public String profileImageUrl;
        public String text;
        public long timestamp;
        
        public Comment() {}
        
        public Comment(String userId, String username, String profileImageUrl, String text, long timestamp) {
            this.userId = userId;
            this.username = username;
            this.profileImageUrl = profileImageUrl;
            this.text = text;
            this.timestamp = timestamp;
        }
    }

    // Adapter for comments
    public static class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
        private final List<Comment> comments;
        private final String postId;
        private final String postCaption;
        
        public CommentAdapter(List<Comment> comments, String postId, String postCaption) {
            this.comments = comments;
            this.postId = postId;
            this.postCaption = postCaption;
        }
        
        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.item_comment, null);
            return new CommentViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            Comment comment = comments.get(position);
            holder.usernameText.setText(comment.username != null ? comment.username : "User");
            holder.commentText.setText(comment.text);
            
            // Format timestamp
            String timeAgo = getTimeAgo(comment.timestamp);
            holder.timestampText.setText(timeAgo);
            
            if (comment.profileImageUrl != null && !comment.profileImageUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(comment.profileImageUrl)
                        .placeholder(R.drawable.profile_placeholder)
                        .error(R.drawable.profile_placeholder)
                        .circleCrop()
                        .into(holder.profileImage);
            } else {
                holder.profileImage.setImageResource(R.drawable.profile_placeholder);
            }

            // Report button click listener
            holder.reportButton.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(holder.itemView.getContext(), ReportActivity.class);
                intent.putExtra("postId", this.postId);
                intent.putExtra("postCaption", this.postCaption);
                intent.putExtra("contentType", "comment");
                intent.putExtra("commentId", comment.userId + "_" + comment.timestamp); // Create a unique comment ID
                if (holder.itemView.getContext() instanceof android.app.Activity) {
                    ((android.app.Activity) holder.itemView.getContext()).startActivityForResult(intent, 1004);
                } else {
                    holder.itemView.getContext().startActivity(intent);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return comments.size();
        }
        
        private String getTimeAgo(long timestamp) {
            long timeDiff = System.currentTimeMillis() - timestamp;
            long seconds = timeDiff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            
            if (days > 0) return days + "d ago";
            if (hours > 0) return hours + "h ago";
            if (minutes > 0) return minutes + "m ago";
            return "Just now";
        }
        
        static class CommentViewHolder extends RecyclerView.ViewHolder {
            ImageView profileImage;
            TextView usernameText;
            TextView commentText;
            TextView timestampText;
            android.widget.ImageButton reportButton;
            
            public CommentViewHolder(@NonNull View itemView) {
                super(itemView);
                profileImage = itemView.findViewById(R.id.image_comment_user);
                usernameText = itemView.findViewById(R.id.text_comment_username);
                commentText = itemView.findViewById(R.id.text_comment_body);
                timestampText = itemView.findViewById(R.id.text_comment_timestamp);
                reportButton = itemView.findViewById(R.id.button_report_comment);
            }
        }
    }
} 