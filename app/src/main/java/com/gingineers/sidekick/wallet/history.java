package com.gingineers.sidekick.wallet;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gingineers.sidekick.R;
import com.gingineers.sidekick.Wallet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class history extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    private TextView historyTotalAmountSpent;
    private RecyclerView recyclerView;

    private TodayItemsAdapter todayItemsAdapter;
    private List<Data> myDataList;

    private String onlineUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ImageButton btnBackhistory = findViewById(R.id.btnBackhistory);
        btnBackhistory.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Wallet.class)));

        Button btnSearch = findViewById(R.id.btnSearch);
        historyTotalAmountSpent = findViewById(R.id.historyTotalAmountSpent);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        onlineUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        recyclerView = findViewById(R.id.todayRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        myDataList = new ArrayList<>();
        todayItemsAdapter = new TodayItemsAdapter(history.this, myDataList);
        recyclerView.setAdapter(todayItemsAdapter);

        btnSearch.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        int months = month+1;
        String date = dayOfMonth+"-"+"0"+months+"-"+year;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myDataList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Data data = snapshot.getValue(Data.class);
                    myDataList.add(data);
                }
                todayItemsAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);

                int totalAmount = 0;
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Map<String,Object> map = (Map<String, Object>) ds.getValue();
                    assert map != null;
                    Object total = map.get("amount");
                    int pTotal = Integer.parseInt(String.valueOf(total));
                    totalAmount += pTotal;
                    if (totalAmount > 0) {
                        historyTotalAmountSpent.setVisibility(View.VISIBLE);
                        historyTotalAmountSpent.setText("This day you have spent: ₱ " + totalAmount);
                    }
                    else {
                        historyTotalAmountSpent.setText("You have not spent this day.");
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(history.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}