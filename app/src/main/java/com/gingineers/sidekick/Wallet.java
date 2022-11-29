package com.gingineers.sidekick;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gingineers.sidekick.wallet.budget;
import com.gingineers.sidekick.wallet.chooseAnalytic;
import com.gingineers.sidekick.wallet.history;
import com.gingineers.sidekick.wallet.todaySpending;
import com.gingineers.sidekick.wallet.weekSpending;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

public class Wallet extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    private TextView budgetTV, todayTV, weekTV, monthTV, savingsTV;

    private DatabaseReference budgetRef;
    private DatabaseReference personalRef;
    private String onlineUserID = "";

    private int totalAmountBudget = 0;
    private int totalAmountBudgetB = 0;
    private int totalAmountBudgetC = 0;

    SwipeRefreshLayout refreshLayout;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        bottomNavigationView = findViewById(R.id.botNavView);
        bottomNavigationView.setSelectedItemId(R.id.wallet);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()){

                case R.id.wallet:
                    return true;

                case R.id.todo:
                    startActivity(new Intent(getApplicationContext(), ToDo.class));
                    overridePendingTransition(0,0);
                    return true;

                case R.id.journal:
                    startActivity(new Intent(getApplicationContext(), Journal.class));
                    overridePendingTransition(0,0);
                    return true;

                case R.id.home:
                    startActivity(new Intent(getApplicationContext(), Homescreen.class));
                    overridePendingTransition(0,0);
                    return true;
            }

            return false;
        });

        CardView budgetCV = findViewById(R.id.budgetCV);
        CardView todayCV = findViewById(R.id.todayCV);
        CardView weekCV = findViewById(R.id.weekCV);
        CardView monthCV = findViewById(R.id.monthCV);
        CardView analyticsCV = findViewById(R.id.analyticsCV);
        CardView historyCV = findViewById(R.id.historyCV);

        budgetTV = findViewById(R.id.budgetTV);
        todayTV = findViewById(R.id.todayTV);
        weekTV = findViewById(R.id.weekTV);
        monthTV = findViewById(R.id.monthTV);
        savingsTV = findViewById(R.id.savingsTV);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        onlineUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        budgetRef = FirebaseDatabase.getInstance().getReference("budget").child(onlineUserID);
        budgetRef.keepSynced(true);
        DatabaseReference expenseRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserID);
        expenseRef.keepSynced(true);
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(onlineUserID);
        personalRef.keepSynced(true);

        // swipe refresh activity
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            startActivity(new Intent(getApplicationContext(), Wallet.class));
        });

        budgetCV.setOnClickListener(v -> {
            Intent intent = new Intent(Wallet.this, budget.class);
            startActivity(intent);
        });

        todayCV.setOnClickListener(v -> {
            Intent intent = new Intent(Wallet.this, todaySpending.class);
            startActivity(intent);
        });

        weekCV.setOnClickListener(v -> {
            Intent intent = new Intent(Wallet.this, weekSpending.class);
            intent.putExtra("type", "week");
            startActivity(intent);
        });

        monthCV.setOnClickListener(v -> {
            Intent intent = new Intent(Wallet.this, weekSpending.class);
            intent.putExtra("type", "month");
            startActivity(intent);
        });

        analyticsCV.setOnClickListener(v -> {
            Intent intent = new Intent(Wallet.this, chooseAnalytic.class);
            intent.putExtra("type", "month");
            startActivity(intent);
        });

        historyCV.setOnClickListener(v -> {
            Intent intent = new Intent(Wallet.this, history.class);
            intent.putExtra("type", "month");
            startActivity(intent);
        });

        budgetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.getChildrenCount()>0){
                    for (DataSnapshot ds: snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmountBudgetB += pTotal;
                    }
                    totalAmountBudgetC = totalAmountBudgetB;
                    personalRef.child("budget").setValue(totalAmountBudgetC);
                }
                else {
                    personalRef.child("budget").setValue(0);
                    Toast.makeText(Wallet.this, "Please set a budget", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        getBudgetAmount();
        getTodaySpentAmount();
        getWeekSpentAmount();
        getMonthSpentAmount();
        getSavings();


    }


    private void getBudgetAmount() {
        budgetRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmountBudget += pTotal;
                        budgetTV.setText("₱ " + totalAmountBudget);
                    }

                } else {
                    totalAmountBudget = 0;
                    budgetTV.setText("₱ " + 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTodaySpentAmount() {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserID);
        Query query = reference.orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmount = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Map<String, Object> map = (Map<String, Object>) ds.getValue();
                    assert map != null;
                    Object total = map.get("amount");
                    int pTotal = Integer.parseInt(String.valueOf(total));
                    totalAmount += pTotal;
                    todayTV.setText("₱ " + totalAmount);
                }
                personalRef.child("today").setValue(totalAmount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Wallet.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void getWeekSpentAmount() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // Set epoch time
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserID);
        Query query = reference.orderByChild("week").equalTo(weeks.getWeeks());
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmount = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Map<String, Object> map = (Map<String, Object>) ds.getValue();
                    assert map != null;
                    Object total = map.get("amount");
                    int pTotal = Integer.parseInt(String.valueOf(total));
                    totalAmount += pTotal;
                    weekTV.setText("₱ " + totalAmount);
                }
                personalRef.child("week").setValue(totalAmount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void getMonthSpentAmount() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // Set epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserID);
        Query query = reference.orderByChild("month").equalTo(months.getMonths());
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmount = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Map<String, Object> map = (Map<String, Object>) ds.getValue();
                    assert map != null;
                    Object total = map.get("amount");
                    int pTotal = Integer.parseInt(String.valueOf(total));
                    totalAmount += pTotal;
                    monthTV.setText("₱ " + totalAmount);
                }
                personalRef.child("month").setValue(totalAmount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getSavings() {
        personalRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    int budget;
                    if (snapshot.hasChild("budget")){
                        budget = Integer.parseInt(Objects.requireNonNull(snapshot.child("budget").getValue()).toString());
                    }
                    else {
                        budget = 0;
                    }

                    int monthSpending;
                    if (snapshot.hasChild("month")){
                        monthSpending = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(snapshot.child("month").getValue()).toString()));
                    }
                    else {
                        monthSpending = 0;
                    }

                    int savings = budget - monthSpending;
                    savingsTV.setText("₱ "+ savings);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), Homescreen.class));
    }
}