package com.gingineers.sidekick.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class weekSpending extends AppCompatActivity {

    private TextView txtHeader, totalWeekAmount;
    private ProgressBar progressBar;

    private WeekSpendingAdapter weekSpendingAdapter;
    private List<Data> myDataList;

    private String onlineUserId = "";
    private DatabaseReference expensesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_spending);

        ImageButton btnBackweek = findViewById(R.id.btnBackweek);
        btnBackweek.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Wallet.class)));

        txtHeader = findViewById(R.id.txtHeader);
        totalWeekAmount = findViewById(R.id.totalWeekAmount);
        progressBar = findViewById(R.id.progressBar);

        RecyclerView recyclerView = findViewById(R.id.todayRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        onlineUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        myDataList = new ArrayList<>();
        weekSpendingAdapter = new WeekSpendingAdapter(weekSpending.this, myDataList);
        recyclerView.setAdapter(weekSpendingAdapter);

        if (getIntent().getExtras()!=null){
            String type = getIntent().getStringExtra("type");
            if (type.equals("week")){
                readWeekSpendingItems();
            }
            else if (type.equals("month")){
                readMonthSpendingItems();
            }
        }



    }

    @SuppressLint("SetTextI18n")
    private void readMonthSpendingItems() {

        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);
        txtHeader.setText("This Month Spending");

        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        expensesRef.keepSynced(true);
        Query query = expensesRef.orderByChild("month").equalTo(months.getMonths());
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Data data = dataSnapshot.getValue(Data.class);
                    myDataList.add(data);
                }

                weekSpendingAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                int totalAmount = 0;
                for (DataSnapshot ds: snapshot.getChildren()){
                    Map<String, Object> map = (Map<String, Object>)ds.getValue();
                    assert map != null;
                    Object total = map.get("amount");
                    int pTotal = Integer.parseInt(String.valueOf(total));
                    totalAmount += pTotal;

                    totalWeekAmount.setText("Total of Month Spending: ₱ "+totalAmount);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void readWeekSpendingItems() {

        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);
        txtHeader.setText("This Week Spending");

        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        expensesRef.keepSynced(true);
        Query query = expensesRef.orderByChild("week").equalTo(weeks.getWeeks());
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              myDataList.clear();
              for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                  Data data = dataSnapshot.getValue(Data.class);
                  myDataList.add(data);
              }

              weekSpendingAdapter.notifyDataSetChanged();
              progressBar.setVisibility(View.GONE);

              int totalAmount = 0;
              for (DataSnapshot ds: snapshot.getChildren()){
                  Map<String, Object> map = (Map<String, Object>)ds.getValue();
                  Object total = map.get("amount");
                  int pTotal = Integer.parseInt(String.valueOf(total));
                  totalAmount += pTotal;

                  totalWeekAmount.setText("Total of Week Spending: ₱ "+totalAmount);
              }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}