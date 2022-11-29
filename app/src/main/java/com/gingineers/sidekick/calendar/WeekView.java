package com.gingineers.sidekick.calendar;

import static com.gingineers.sidekick.calendar.CalendarUtils.daysInWeekArray;
import static com.gingineers.sidekick.calendar.CalendarUtils.monthYearFromDate;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gingineers.sidekick.Homescreen;
import com.gingineers.sidekick.Journal;
import com.gingineers.sidekick.R;
import com.gingineers.sidekick.ToDo;
import com.gingineers.sidekick.Wallet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class WeekView extends AppCompatActivity implements CalendarAdapter.OnItemListener {

    BottomNavigationView bottomNavigationView;

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;

    private DatabaseReference reference;
    private String onlineUserId;


    private EventAdapter eventAdapter;
    private List<EventModel> myEventList;

    public WeekView() {
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_view);
        initWidgets();
        setWeekView();

        bottomNavigationView = findViewById(R.id.botNavView);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()){

                case R.id.home:
                    startActivity(new Intent(getApplicationContext(), Homescreen.class));
                    overridePendingTransition(0,0);
                    return true;

                case R.id.todo:
                    startActivity(new Intent(getApplicationContext(), ToDo.class));
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

    }

    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRV);
        monthYearText = findViewById(R.id.monthYearTV);
        ImageButton btnBackevent = findViewById(R.id.btnBackevent);
        btnBackevent.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Homescreen.class)));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        assert mUser != null;
        onlineUserId = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("event").child(onlineUserId);
        reference.keepSynced(true);

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(view -> createEvent());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        myEventList = new ArrayList<>();
        eventAdapter = new EventAdapter(WeekView.this, myEventList);
        recyclerView.setAdapter(eventAdapter);

        readItems();

    }

    private void setWeekView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> days = daysInWeekArray(CalendarUtils.selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(days, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
//        setEventAdapter();
    }

    // previous week - left arrow
    public void previousWeekAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
        setWeekView();
    }

    // next week - right arrow
    public void nextWeekAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
        setWeekView();
    }

    // a date is clicked
    @Override
    public void onItemClick(int position, LocalDate date) {
        CalendarUtils.selectedDate = date;
        setWeekView();
    }

    private void createEvent(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.addevent_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        // initialize widgets from addevent_layout
        final EditText name = myView.findViewById(R.id.eventNameET);
        final EditText content = myView.findViewById(R.id.eventContentET);
        final ImageView btnDate = myView.findViewById(R.id.btnDate);
        final ImageView btnTime = myView.findViewById(R.id.btnTime);
        final TextView date = myView.findViewById(R.id.dateTV);
        final TextView time = myView.findViewById(R.id.timeTV);
        final TextView timestamp = myView.findViewById(R.id.timestampTV);

        final Button cancel = myView.findViewById(R.id.btnCancel);
        final Button save = myView.findViewById(R.id.btnSave);

        cancel.setOnClickListener(v -> dialog.dismiss());

        btnDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog datePickerDialog = new DatePickerDialog(WeekView.this, (view, year1, month1, dayOfMonth) -> {

                int months = month1 + 1;
                String dateTxt = dayOfMonth + "-" + months + "-" + year1;
                date.setText(dateTxt);

                String timestampTxt = year1 + "-" + months + "-" + dayOfMonth;
                timestamp.setText(timestampTxt);

            }, year, month, day);
            datePickerDialog.show();
        });

        btnTime.setOnClickListener(new View.OnClickListener() {

            int t1Hour, t1Minute;

            @Override
            public void onClick(View view) {

                TimePickerDialog timePickerDialog = new TimePickerDialog(WeekView.this, (view1, hourOfDay, minute) -> {
                    // initialize hour and minute
                    t1Hour = hourOfDay;
                    t1Minute = minute;
                    // initialize calendar
                    Calendar calendar = Calendar.getInstance();
                    // set hour and minute
                    calendar.set(0, 0, 0, t1Hour, t1Minute);
                    // set selected time on text view
                    time.setText(DateFormat.format("hh:mm aa", calendar));
                }, 12,0,false);
                // display previous selected time
                timePickerDialog.updateTime(t1Hour, t1Minute);
                // show dialog
                timePickerDialog.show();
            }
        });

        save.setOnClickListener(view -> {
            // get datas from layout
            String id = reference.push().getKey();
            String Name = name.getText().toString().trim();
            String Content = content.getText().toString().trim();
            String Date = date.getText().toString().trim();
            String Time = time.getText().toString().trim();
            String Timestamp = timestamp.getText().toString().trim();

            // set error if empty
            if (TextUtils.isEmpty(Name)){
                name.setError("Event title is required");
                return;
            }
            if (TextUtils.isEmpty(Content)){
                content.setError("Event description is required");
                return;
            }
            if (TextUtils.isEmpty(Date)){
                date.setError("Please select a date");
                return;
            }
            if (TextUtils.isEmpty(Time)){
                time.setError("Please select a time");
                return;
            }
            else {

                EventModel model = new EventModel(id, Name, Content, Date, Time, Timestamp);
                assert id != null;
                reference.child(id).setValue(model).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(WeekView.this, "Event has been added successfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String error = Objects.requireNonNull(task.getException()).toString();
                        Toast.makeText(WeekView.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            dialog.dismiss();
        });
        dialog.show();

    }

    private void readItems(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("event").child(onlineUserId);
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myEventList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    EventModel eventModel = dataSnapshot.getValue(EventModel.class);
                    myEventList.add(eventModel);
                    myEventList.sort((eventModel1, t1) -> t1.getTimestamp().compareTo(eventModel1.getTimestamp()));
                }
                eventAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
