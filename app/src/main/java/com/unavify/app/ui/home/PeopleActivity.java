package com.unavify.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unavify.app.R;
import com.unavify.app.ui.home.PeopleAdapter;
import java.util.ArrayList;
import java.util.List;

public class PeopleActivity extends AppCompatActivity {
    private RecyclerView recyclerViewUsers;
    private PeopleAdapter peopleAdapter;
    private List<User> userList = new ArrayList<>();
    private List<User> filteredUserList = new ArrayList<>();
    private EditText searchEditText;
    private TextView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);

        setupUI();
        loadAllUsers();
    }

    private void setupUI() {
        // Setup back button
        backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());

        // Setup search
        searchEditText = findViewById(R.id.edit_text_search);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup RecyclerView
        recyclerViewUsers = findViewById(R.id.recycler_view_users);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        peopleAdapter = new PeopleAdapter(this, filteredUserList);
        recyclerViewUsers.setAdapter(peopleAdapter);

        // Set click listener for user profile circles
        peopleAdapter.setOnUserClickListener(user -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("user_phone", user.phone);
            intent.putExtra("username", user.username);
            intent.putExtra("profile_image_url", user.profileImageUrl);
            startActivity(intent);
        });
    }

    private void loadAllUsers() {
        Log.d("PEOPLE_ACTIVITY", "Loading all users from Firestore");
        
        FirebaseFirestore.getInstance()
            .collection("users")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                userList.clear();
                int fetchedCount = querySnapshot.getDocuments().size();
                Log.d("PEOPLE_ACTIVITY", "Fetched users: " + fetchedCount);
                
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String phone = doc.getId();
                    String username = doc.getString("username");
                    String profileImageUrl = doc.getString("profileImageUrl");
                    
                    Log.d("PEOPLE_ACTIVITY", "Processing user: phone=" + phone + ", username=" + username);
                    userList.add(new User(phone, username, profileImageUrl));
                }
                
                // Initially show all users
                filteredUserList.clear();
                filteredUserList.addAll(userList);
                peopleAdapter.notifyDataSetChanged();
                
                Log.d("PEOPLE_ACTIVITY", "User adapter notified, total users: " + userList.size());
            })
            .addOnFailureListener(e -> {
                Log.e("PEOPLE_ACTIVITY", "Failed to fetch users from Firestore", e);
            });
    }

    private void filterUsers(String query) {
        filteredUserList.clear();
        
        if (query.isEmpty()) {
            // Show all users if search is empty
            filteredUserList.addAll(userList);
        } else {
            // Filter users by username
            String lowerCaseQuery = query.toLowerCase();
            for (User user : userList) {
                if (user.username != null && user.username.toLowerCase().contains(lowerCaseQuery)) {
                    filteredUserList.add(user);
                }
            }
        }
        
        peopleAdapter.notifyDataSetChanged();
        Log.d("PEOPLE_ACTIVITY", "Filtered users: " + filteredUserList.size() + " for query: " + query);
    }
} 