package com.gingineers.sidekick.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gingineers.sidekick.R;
import com.gingineers.sidekick.Wallet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class todaySpending extends AppCompatActivity {

    private TextView totalAmountSpentOn;
    private ProgressBar progressBar;

    private String onlineUserId = "";
    private DatabaseReference expensesRef;

    private TodayItemsAdapter todayItemsAdapter;
    private List<Data> myDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_spending);

        ImageButton btnBacktoday = findViewById(R.id.btnBacktoday);
        btnBacktoday.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Wallet.class)));

        totalAmountSpentOn = findViewById(R.id.totalAmountSpentOn);
        progressBar = findViewById(R.id.progressBar);

        FloatingActionButton addItemFAB = findViewById(R.id.addItemFAB);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        onlineUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        expensesRef.keepSynced(true);


        RecyclerView recyclerView = findViewById(R.id.todayRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        myDataList = new ArrayList<>();
        todayItemsAdapter = new TodayItemsAdapter(todaySpending.this, myDataList);
        recyclerView.setAdapter(todayItemsAdapter);

        readItems();

        addItemFAB.setOnClickListener(v -> addItemSpentOn());

    }

    private void readItems() {

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               myDataList.clear();
               for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                   Data data = dataSnapshot.getValue(Data.class);
                   myDataList.add(data);
               }

               todayItemsAdapter.notifyDataSetChanged();
               progressBar.setVisibility(View.GONE);

               int totalAmount = 0;
               for (DataSnapshot ds: snapshot.getChildren()){
                   Map<String, Object> map = (Map<String, Object>)ds.getValue();
                   assert map != null;
                   Object total = map.get("amount");
                   int pTotal = Integer.parseInt(String.valueOf(total));
                   totalAmount += pTotal;

                   totalAmountSpentOn.setText("Total of Today Spending: ₱ "+totalAmount);
               }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addItemSpentOn() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.inputwallet_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final Spinner itemSpinner = myView.findViewById(R.id.itemspinner);
        final EditText amount = myView.findViewById(R.id.amountET);
        final EditText note = myView.findViewById(R.id.noteET);
        final Button cancel = myView.findViewById(R.id.btnCancel);
        final Button save = myView.findViewById(R.id.btnSaveitem);

        note.setVisibility(View.VISIBLE);

        save.setOnClickListener(v -> {

            String Amount = amount.getText().toString();
            String Item = itemSpinner.getSelectedItem().toString();
            String notes = note.getText().toString();

            if (TextUtils.isEmpty(Amount)){
                amount.setError("Amount is required");
                return;
            }

            if (Item.equals("Select item")){
                Toast.makeText(todaySpending.this, "Select a valid item.", Toast.LENGTH_SHORT).show();
            }

            if (TextUtils.isEmpty(notes)){
                note.setError("Note is required");
                return;
            }

            else {

                String id = expensesRef.push().getKey();
                @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Calendar calendar = Calendar.getInstance();
                String date =dateFormat.format(calendar.getTime());

                MutableDateTime epoch = new MutableDateTime();
                epoch.setDate(0);
                DateTime now = new DateTime();
                Weeks weeks = Weeks.weeksBetween(epoch, now);
                Months months = Months.monthsBetween(epoch, now);

                String itemNday = Item+date;
                String itemNweek = Item+weeks.getWeeks();
                String itemNmonth = Item+months.getMonths();

                // add wallet budget to firebase database
                Data data = new Data(Item, date, id, itemNday, itemNweek,itemNmonth, Integer.parseInt(Amount), months.getMonths(), weeks.getWeeks(), notes);
                assert id != null;
                expensesRef.child(id).setValue(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(todaySpending.this, "Budget item added successfully", Toast.LENGTH_SHORT).show();
                    }

                    else {
                        Toast.makeText(todaySpending.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}