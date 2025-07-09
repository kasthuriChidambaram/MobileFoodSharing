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
import com.unavify.app.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {
    private String postId;
    private RecyclerView recyclerView;
    private EditText commentEditText;
    private Button sendButton;
    private ProgressBar progressBar;
    private CommentAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "Error: Post ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recycler_view_comments);
        commentEditText = findViewById(R.id.edit_text_comment);
        sendButton = findViewById(R.id.button_send_comment);
        progressBar = findViewById(R.id.progress_bar_comments);

        adapter = new CommentAdapter(commentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendComment());

        loadComments();
    }

    private void loadComments() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            commentList.add(comment);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
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
                            loadComments(); // Refresh comments
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
        
        public CommentAdapter(List<Comment> comments) {
            this.comments = comments;
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
            
            if (comment.profileImageUrl != null && !comment.profileImageUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(comment.profileImageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(holder.profileImage);
            } else {
                holder.profileImage.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
        
        @Override
        public int getItemCount() {
            return comments.size();
        }
        
        static class CommentViewHolder extends RecyclerView.ViewHolder {
            ImageView profileImage;
            TextView usernameText;
            TextView commentText;
            
            public CommentViewHolder(@NonNull View itemView) {
                super(itemView);
                profileImage = itemView.findViewById(R.id.image_comment_user);
                usernameText = itemView.findViewById(R.id.text_comment_username);
                commentText = itemView.findViewById(R.id.text_comment_body);
            }
        }
    }
} 