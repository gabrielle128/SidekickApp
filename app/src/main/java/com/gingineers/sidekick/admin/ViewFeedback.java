package com.gingineers.sidekick.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gingineers.sidekick.AdminHome;
import com.gingineers.sidekick.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;

public class ViewFeedback extends AppCompatActivity {

    private DatabaseReference reference;

    private FeedbackAdapter feedbackAdapter;
    private ArrayList<FeedbackModel> feedbackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_feedback);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> startActivity(new Intent(ViewFeedback.this, AdminHome.class)));

        reference = FirebaseDatabase.getInstance().getReference("feedback");

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        feedbackList = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(this, feedbackList);
        recyclerView.setAdapter(feedbackAdapter);

        viewFeedback();

    }

    private void viewFeedback() {

        reference = FirebaseDatabase.getInstance().getReference("feedback");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                feedbackList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    FeedbackModel model = dataSnapshot.getValue(FeedbackModel.class);
                    feedbackList.add(model);

                    // sort feedback by timestamp
                    feedbackList.sort(Comparator.comparing(FeedbackModel::getTimestamp));
                }
                feedbackAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}