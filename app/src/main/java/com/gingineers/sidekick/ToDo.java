package com.gingineers.sidekick;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gingineers.sidekick.todo.Model;
import com.gingineers.sidekick.todo.OverdueAdapter;
import com.gingineers.sidekick.todo.TodayAdapter;
import com.gingineers.sidekick.todo.TodoAdapter;
import com.gingineers.sidekick.todo.UpcomingAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ToDo extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    private LinearLayout overdueLL, todayLL, upcomingLL, allTasksLL;

    private DatabaseReference reference;

    private TodoAdapter todoAdapter;
    private List<Model> myDataList;

    private TodayAdapter todayAdapter;
    private List<Model> myTodayList;

    private OverdueAdapter overdueAdapter;
    private List<Model> myOverdueList;

    private UpcomingAdapter upcomingAdapter;
    private List<Model> myUpcomingList;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        bottomNavigationView = findViewById(R.id.botNavView);
        bottomNavigationView.setSelectedItemId(R.id.todo);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()){

                case R.id.todo:
                    return true;

                case R.id.home:
                    startActivity(new Intent(getApplicationContext(), Homescreen.class));
                    overridePendingTransition(0,0);
                    return true;

                case R.id.journal:
                    startActivity(new Intent(getApplicationContext(), Journal.class));
                    overridePendingTransition(0,0);
                    return true;

                case R.id.wallet:
                    startActivity(new Intent(getApplicationContext(), Wallet.class));
                    overridePendingTransition(0,0);
                    return true;
            }

            return false;
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        assert mUser != null;
        String onlineUserId = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("todo").child(onlineUserId);
        reference.keepSynced(true);

        overdueLL = findViewById(R.id.overdueLL);
        todayLL = findViewById(R.id.todayLL);
        upcomingLL = findViewById(R.id.upcomingLL);
        allTasksLL = findViewById(R.id.allTasksLL);

        FloatingActionButton todoFAB = findViewById(R.id.todoFAB);
        todoFAB.setOnClickListener(v -> addTask());

        // overdue
        RecyclerView overdueRV = findViewById(R.id.overdueRV);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        linearLayoutManager2.setReverseLayout(true);
        linearLayoutManager2.setStackFromEnd(true);
        overdueRV.setHasFixedSize(true);
        overdueRV.setLayoutManager(linearLayoutManager2);
        myOverdueList = new ArrayList<>();
        overdueAdapter = new OverdueAdapter(ToDo.this, myOverdueList);
        overdueRV.setAdapter(overdueAdapter);

        // today
        RecyclerView todayRV = findViewById(R.id.todayRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        todayRV.setHasFixedSize(true);
        todayRV.setLayoutManager(linearLayoutManager);
        myTodayList = new ArrayList<>();
        todayAdapter = new TodayAdapter(ToDo.this, myTodayList);
        todayRV.setAdapter(todayAdapter);

        // upcoming
        RecyclerView upcomingRV = findViewById(R.id.upcomingRV);
        LinearLayoutManager linearLayoutManager3 = new LinearLayoutManager(this);
        linearLayoutManager3.setReverseLayout(true);
        linearLayoutManager3.setStackFromEnd(true);
        upcomingRV.setHasFixedSize(true);
        upcomingRV.setLayoutManager(linearLayoutManager3);
        myUpcomingList = new ArrayList<>();
        upcomingAdapter = new UpcomingAdapter(ToDo.this, myUpcomingList);
        upcomingRV.setAdapter(upcomingAdapter);

        // others
        RecyclerView othersRV = findViewById(R.id.othersRV);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        linearLayoutManager1.setReverseLayout(true);
        linearLayoutManager1.setStackFromEnd(true);
        othersRV.setHasFixedSize(true);
        othersRV.setLayoutManager(linearLayoutManager1);
        myDataList = new ArrayList<>();
        todoAdapter = new TodoAdapter(ToDo.this, myDataList);
        othersRV.setAdapter(todoAdapter);

        readItems();

        if (myOverdueList.isEmpty()){
            overdueLL.setVisibility(View.GONE);
        }

        if (myTodayList.isEmpty()){
            todayLL.setVisibility(View.GONE);
        }

        if (myUpcomingList.isEmpty()){
            upcomingLL.setVisibility(View.GONE);
        }

        if (myDataList.isEmpty()){
            allTasksLL.setVisibility(View.GONE);
        }

    }


    private void readItems() {

        overdueTask();
        todayTask();
        upcomingTask();
        allTasks();

    }

    // task for overdue/previous day
    private void overdueTask(){

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy-M-dd");
        Calendar calendar = Calendar.getInstance();
        String now = dateFormat.format(calendar.getTime());

        Query overdue = reference.orderByChild("timestamp").endBefore(now);
        overdue.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myOverdueList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Model model = dataSnapshot.getValue(Model.class);
                    myOverdueList.add(model);
                    overdueLL.setVisibility(View.VISIBLE);
                }
                overdueAdapter.notifyDataSetChanged();

                // sort myUpcomingList by date
                myOverdueList.sort((model, t1) -> t1.getTimestamp().compareTo(model.getTimestamp()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                overdueLL.setVisibility(View.GONE);
            }
        });
    }

    // task for today
    private void todayTask(){

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy-M-dd");
        Calendar calendar = Calendar.getInstance();
        String now = dateFormat.format(calendar.getTime());

        Query today = reference.orderByChild("timestamp").equalTo(now);
        today.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myTodayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Model model = dataSnapshot.getValue(Model.class);
                    myTodayList.add(model);
                    todayLL.setVisibility(View.VISIBLE);
                }
                todayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                todayLL.setVisibility(View.GONE);
            }
        });
    }

    // task for upcoming/next day
    private void upcomingTask(){

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy-M-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        String start = dateFormat.format(calendar.getTime());

        Query upcoming = reference.orderByChild("timestamp").startAt(start);
        upcoming.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myUpcomingList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Model model = dataSnapshot.getValue(Model.class);
                    myUpcomingList.add(model);
                    upcomingLL.setVisibility(View.VISIBLE);

                    // sort myUpcomingList by date
                    myUpcomingList.sort((model1, t1) -> t1.getTimestamp().compareTo(model1.getTimestamp()));

                }
                upcomingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                upcomingLL.setVisibility(View.GONE);
            }
        });
    }


    // all task from firebase
    private void allTasks(){

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Model model = dataSnapshot.getValue(Model.class);
                    myDataList.add(model);

                    allTasksLL.setVisibility(View.VISIBLE);

                    // sort myDataList by date
                    myDataList.sort((model1, t1) -> t1.getTimestamp().compareTo(model1.getTimestamp()));

                }
                todoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.addtodo_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText title = myView.findViewById(R.id.titleET);
        final EditText description = myView.findViewById(R.id.descET);
        Button btnDate = myView.findViewById(R.id.btnDate);
        final TextView date = myView.findViewById(R.id.dateTV);
        final TextView timestamp = myView.findViewById(R.id.timestampTV);
        Button save = myView.findViewById(R.id.btnSave);
        Button cancel = myView.findViewById(R.id.btnCancel);

        btnDate.setOnClickListener(v -> {

            // getting the current date and set it as default in the picker
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog datePickerDialog = new DatePickerDialog(ToDo.this, (view, year1, month1, dayOfMonth) -> {

                int months = month1 + 1;
                String dateTxt = dayOfMonth + "-" + months + "-" + year1;
                date.setText(dateTxt);

                String timestampTxt = year1 + "-" + months + "-" + dayOfMonth;
                timestamp.setText(timestampTxt);

            }, year, month, day);
            datePickerDialog.show();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());

        save.setOnClickListener(v -> {
            String Title = title.getText().toString().trim();
            String Description = description.getText().toString().trim();
            String id = reference.push().getKey();
            String Date = date.getText().toString().trim();
            String Timestamp = timestamp.getText().toString().trim();


            if (TextUtils.isEmpty(Title)){
                title.setError("Title is required");
                return;
            }

            if (TextUtils.isEmpty(Description)){
                description.setError("Description is required");
                return;
            }

            if (TextUtils.isEmpty(Date)){
                date.setError("Set a target date");
                return;
            }

            else {

                Model model = new Model(Title, Description, id, Date, Timestamp);
                assert id != null;
                reference.child(id).setValue(model).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(ToDo.this, "Task has been added successfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String error = Objects.requireNonNull(task.getException()).toString();
                        Toast.makeText(ToDo.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), Homescreen.class));
    }

}