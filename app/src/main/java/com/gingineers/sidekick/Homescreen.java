package com.gingineers.sidekick;

import static com.gingineers.sidekick.calendar.CalendarUtils.daysInMonthArray;
import static com.gingineers.sidekick.calendar.CalendarUtils.monthYearFromDate;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.gingineers.sidekick.calendar.CalendarAdapter;
import com.gingineers.sidekick.calendar.CalendarUtils;
import com.gingineers.sidekick.calendar.EventModel;
import com.gingineers.sidekick.calendar.WeekView;
import com.gingineers.sidekick.journal.createjournal;
import com.gingineers.sidekick.todo.Model;
import com.gingineers.sidekick.wallet.Data;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class Homescreen extends AppCompatActivity implements CalendarAdapter.OnItemListener {

    BottomNavigationView bottomNavigationView;

    ImageButton btnAccount;

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;

    private DatabaseReference reference, expensesRef, eventRef;

    private long backPressedTime;
    private Toast backToast;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);
        initWidgets();
        CalendarUtils.selectedDate = LocalDate.now();
        setMonthView();

        btnAccount = findViewById(R.id.btnAccount);

        btnAccount.setOnClickListener(view -> {
            Intent intentLoadAccount = new Intent(Homescreen.this, Account.class);
            startActivity(intentLoadAccount);
        });

        // bottom navigation bar
        bottomNavigationView = findViewById(R.id.botNavView);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()){

                case R.id.home:
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

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        assert mUser != null;
        String onlineUserId = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("todo").child(onlineUserId);
        reference.keepSynced(true);
        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        expensesRef.keepSynced(true);
        eventRef = FirebaseDatabase.getInstance().getReference().child("event").child(onlineUserId);
        eventRef.keepSynced(true);

        // quick action buttons
        FloatingActionButton todoFAB = findViewById(R.id.todoFAB);
        FloatingActionButton journalFAB = findViewById(R.id.journalFAB);
        FloatingActionButton todayFAB = findViewById(R.id.todayFAB);
        FloatingActionButton eventFAB = findViewById(R.id.eventFAB);

        todoFAB.setOnClickListener(v -> addTask());
        journalFAB.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), createjournal.class)));
        todayFAB.setOnClickListener(v -> addTodaySpending());
        eventFAB.setOnClickListener(view -> addEvent());

    }

    // calendar in
    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRV);
        monthYearText = findViewById(R.id.monthYearTV);
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> daysInMonth = daysInMonthArray(CalendarUtils.selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    // previous month
    public void previousMonthAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusMonths(1);
        setMonthView();
    }

    // previous month
    public void nextMonthAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusMonths(1);
        setMonthView();
    }

    // a day in calendar is clicked
    @Override
    public void onItemClick(int position, LocalDate date) {
        if(date != null) {
            CalendarUtils.selectedDate = date;
            setMonthView();
        }
    }

    public void weeklyAction(View view) {
        startActivity(new Intent(this, WeekView.class));
    }
    // calendar out

    // class for todoFAB when clicked
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

            DatePickerDialog datePickerDialog = new DatePickerDialog(Homescreen.this, (view, year1, month1, dayOfMonth) -> {

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
                        Toast.makeText(Homescreen.this, "Task has been added successfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String error = Objects.requireNonNull(task.getException()).toString();
                        Toast.makeText(Homescreen.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            dialog.dismiss();
        });
        dialog.show();
    }

    // class for todayFAB when clicked
    private void addTodaySpending(){
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
                Toast.makeText(Homescreen.this, "Select a valid item.", Toast.LENGTH_SHORT).show();
            }

            if (TextUtils.isEmpty(notes)){
                note.setError("Note is required");
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
                        Toast.makeText(Homescreen.this, "Budget item added successfully", Toast.LENGTH_SHORT).show();
                    }

                    else {
                        Toast.makeText(Homescreen.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

    }

    //class for eventFAB when clicked
    private void addEvent(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.addevent_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();

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

        cancel.setOnClickListener(view -> dialog.dismiss());

        btnDate.setOnClickListener(view -> {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog datePickerDialog = new DatePickerDialog(Homescreen.this, (view1, year1, month1, dayOfMonth) -> {

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

                TimePickerDialog timePickerDialog = new TimePickerDialog(Homescreen.this, (view12, hourOfDay, minute) -> {

                    // initialize hour and minute
                    t1Hour = hourOfDay;
                    t1Minute = minute;

                    // initialize calendar
                    Calendar calendar = Calendar.getInstance();

                    // set hour and minute
                    calendar.set(0, 0, 0, t1Hour, t1Minute);

                    // set selected time on text view
                    time.setText(android.text.format.DateFormat.format("hh:mm aa", calendar));
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
                eventRef.child(id).setValue(model).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(Homescreen.this, "Event has been added succesfully.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String error = Objects.requireNonNull(task.getException()).toString();
                        Toast.makeText(Homescreen.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            dialog.dismiss();
        });
        dialog.show();
    }


    // double back press to exit application
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            finishAffinity();
            System.exit(0);
        } else {
            backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}