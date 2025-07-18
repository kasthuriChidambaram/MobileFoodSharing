package com.unavify.app.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unavify.app.R;
import java.util.HashMap;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {
    private String postId;
    private String postCaption;
    private String contentType; // "post" or "comment"
    private String commentId; // Only for comments
    
    private RadioGroup reasonGroup;
    private EditText otherReasonEditText;
    private Button submitButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Get data from intent
        postId = getIntent().getStringExtra("postId");
        postCaption = getIntent().getStringExtra("postCaption");
        contentType = getIntent().getStringExtra("contentType");
        commentId = getIntent().getStringExtra("commentId");

        if (postId == null) {
            Toast.makeText(this, "Error: Post ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
    }

    private void setupUI() {
        // Set title based on content type
        TextView titleText = findViewById(R.id.text_report_title);
        if (contentType != null && contentType.equals("comment")) {
            titleText.setText("Report Comment");
        } else {
            titleText.setText("Report Post");
        }

        // Set post caption for context
        TextView captionText = findViewById(R.id.text_post_caption);
        if (postCaption != null && !postCaption.isEmpty()) {
            captionText.setText(postCaption);
            captionText.setVisibility(View.VISIBLE);
        } else {
            captionText.setVisibility(View.GONE);
        }

        reasonGroup = findViewById(R.id.radio_group_reasons);
        otherReasonEditText = findViewById(R.id.edit_text_other_reason);
        submitButton = findViewById(R.id.button_submit_report);
        cancelButton = findViewById(R.id.button_cancel_report);

        // Show/hide other reason text box based on selection
        reasonGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_other) {
                otherReasonEditText.setVisibility(View.VISIBLE);
            } else {
                otherReasonEditText.setVisibility(View.GONE);
            }
        });

        submitButton.setOnClickListener(v -> submitReport());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void submitReport() {
        // Get selected reason
        int selectedId = reasonGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a reason", Toast.LENGTH_SHORT).show();
            return;
        }

        String reason = "";
        if (selectedId == R.id.radio_spam) {
            reason = "spam";
        } else if (selectedId == R.id.radio_inappropriate) {
            reason = "inappropriate";
        } else if (selectedId == R.id.radio_harassment) {
            reason = "harassment";
        } else if (selectedId == R.id.radio_other) {
            String otherReason = otherReasonEditText.getText().toString().trim();
            if (TextUtils.isEmpty(otherReason)) {
                Toast.makeText(this, "Please provide a reason", Toast.LENGTH_SHORT).show();
                return;
            }
            reason = "other";
        }

        // Get current user
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        
        if (userId.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create report data
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("postId", postId);
        reportData.put("reason", reason);
        reportData.put("reporterUserId", userId);
        reportData.put("timestamp", System.currentTimeMillis());
        reportData.put("contentType", contentType != null ? contentType : "post");
        
        if (commentId != null) {
            reportData.put("commentId", commentId);
        }
        
        if (selectedId == R.id.radio_other) {
            reportData.put("otherReason", otherReasonEditText.getText().toString().trim());
        }

        // Show confirmation dialog
        new AlertDialog.Builder(this)
            .setTitle("Confirm Report")
            .setMessage("Are you sure you want to report this " + 
                       (contentType != null && contentType.equals("comment") ? "comment" : "post") + "?")
            .setPositiveButton("Report", (dialog, which) -> {
                submitReportToFirestore(reportData);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void submitReportToFirestore(Map<String, Object> reportData) {
        submitButton.setEnabled(false);
        
        FirebaseFirestore.getInstance()
            .collection("reportedPosts")
            .add(reportData)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to submit report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                submitButton.setEnabled(true);
            });
    }
} 