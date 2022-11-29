package com.gingineers.sidekick.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gingineers.sidekick.R;
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

import java.util.Map;
import java.util.Objects;
import java.util.TimerTask;

public class monthlyAnalytics extends AppCompatActivity {

    private String onlineUserId = "";
    private DatabaseReference personalRef;


    private TextView totalAmountSpentOn, analyticsTransportationAmount, analyticsFoodAmount, analyticsHouseAmount, analyticsTEducationAmount, analyticsHealthAmount,
            analyticsPersonalAmount, analyticsApparelAmount, analyticsEntertainmentAmount, analyticsShoppingAmount, analyticsOthersAmount, totalSpent, monthRatioSpending;

    private RelativeLayout relativeLayoutTransportation;
    private RelativeLayout relativeLayoutFood;
    private RelativeLayout relativeLayoutHouse;
    private RelativeLayout relativeLayoutEducation;
    private RelativeLayout relativeLayoutHealth;
    private RelativeLayout relativeLayoutPersonal;
    private RelativeLayout relativeLayoutApparel;
    private RelativeLayout relativeLayoutEntertainment;
    private RelativeLayout relativeLayoutShopping;
    private RelativeLayout relativeLayoutOthers;

    private TextView progress_ratio_transport, progress_ratio_food, progress_ratio_house, progress_ratio_education, progress_ratio_health, progress_ratio_personal,
            progress_ratio_apparel, progress_ratio_entertainment, progress_ratio_shopping, progress_ratio_others;

    private ImageView transport_status, food_status, house_status, education_status, health_status, personal_status, apparel_status,
            entertainment_status, shopping_status, others_status, monthRatioSpending_Image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_analytics);

        ImageButton btnBackmonthly = findViewById(R.id.btnBackmonthly);
        btnBackmonthly.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), chooseAnalytic.class)));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        onlineUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DatabaseReference expenseRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        expenseRef.keepSynced(true);
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(onlineUserId);
        personalRef.keepSynced(true);

        // daily analytics
        totalAmountSpentOn = findViewById(R.id.totalAmountSpentOn);
        totalSpent = findViewById(R.id.totalSpent);
        monthRatioSpending = findViewById(R.id.monthRatioSpending);
        monthRatioSpending_Image = findViewById(R.id.monthRatioSpending_Image);

        // amount per item
        analyticsTransportationAmount = findViewById(R.id.analyticsTransportationAmount);
        analyticsFoodAmount = findViewById(R.id.analyticsFoodAmount);
        analyticsHouseAmount = findViewById(R.id.analyticsHouseAmount);
        analyticsTEducationAmount = findViewById(R.id.analyticsTEducationAmount);
        analyticsHealthAmount = findViewById(R.id.analyticsHealthAmount);
        analyticsPersonalAmount = findViewById(R.id.analyticsPersonalAmount);
        analyticsApparelAmount = findViewById(R.id.analyticsApparelAmount);
        analyticsEntertainmentAmount = findViewById(R.id.analyticsEntertainmentAmount);
        analyticsShoppingAmount = findViewById(R.id.analyticsShoppingAmount);
        analyticsOthersAmount = findViewById(R.id.analyticsOthersAmount);

        // relative layout per item
        relativeLayoutTransportation = findViewById(R.id.relativeLayoutTransportation);
        relativeLayoutFood = findViewById(R.id.relativeLayoutFood);
        relativeLayoutHouse = findViewById(R.id.relativeLayoutHouse);
        relativeLayoutEducation = findViewById(R.id.relativeLayoutEducation);
        relativeLayoutHealth = findViewById(R.id.relativeLayoutHealth);
        relativeLayoutPersonal = findViewById(R.id.relativeLayoutPersonal);
        relativeLayoutApparel = findViewById(R.id.relativeLayoutApparel);
        relativeLayoutEntertainment = findViewById(R.id.relativeLayoutEntertainment);
        relativeLayoutShopping = findViewById(R.id.relativeLayoutShopping);
        relativeLayoutOthers = findViewById(R.id.relativeLayoutOthers);

        // status text view per item
        progress_ratio_transport = findViewById(R.id.progress_ratio_transport);
        progress_ratio_food = findViewById(R.id.progress_ratio_food);
        progress_ratio_house = findViewById(R.id.progress_ratio_house);
        progress_ratio_education = findViewById(R.id.progress_ratio_education);
        progress_ratio_health = findViewById(R.id.progress_ratio_health);
        progress_ratio_personal = findViewById(R.id.progress_ratio_personal);
        progress_ratio_apparel = findViewById(R.id.progress_ratio_apparel);
        progress_ratio_entertainment = findViewById(R.id.progress_ratio_entertainment);
        progress_ratio_shopping = findViewById(R.id.progress_ratio_shopping);
        progress_ratio_others = findViewById(R.id.progress_ratio_others);

        // image view of status per item (green, brown, red)
        transport_status = findViewById(R.id.transport_status);
        food_status = findViewById(R.id.food_status);
        house_status = findViewById(R.id.house_status);
        education_status = findViewById(R.id.education_status);
        health_status = findViewById(R.id.health_status);
        personal_status = findViewById(R.id.personal_status);
        apparel_status = findViewById(R.id.apparel_status);
        entertainment_status = findViewById(R.id.entertainment_status);
        shopping_status = findViewById(R.id.shopping_status);
        others_status = findViewById(R.id.others_status);

        getTotalWeekTransportationExpenses();
        getTotalWeekFoodExpenses();
        getTotalWeekHouseExpenses();
        getTotalWeekEducationExpenses();
        getTotalWeekHealthExpenses();
        getTotalWeekPersonal();
        getTotalWeekApparel();
        getTotalWeekEntertainment();
        getTotalWeekShoppingExpenses();
        getTotalWeekOthersExpenses();
        getTotalMonthSpending();

        new java.util.Timer().schedule(new TimerTask() {
               @Override
               public void run() {
                   loadGraph();

                   setStatusAndImageResource();
               }
           }, 2000
        );

    }

    private void getTotalWeekTransportationExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNmonth = "Transportation"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsTransportationAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("monthTrans").setValue(totalAmount);
                }
                else {
                    relativeLayoutTransportation.setVisibility(View.GONE);
                    personalRef.child("monthTrans").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(monthlyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekFoodExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNmonth = "Food"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsFoodAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("monthFood").setValue(totalAmount);
                }
                else {
                    relativeLayoutFood.setVisibility(View.GONE);
                    personalRef.child("monthFood").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(monthlyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekHouseExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNmonth = "House"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsHouseAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("monthHouse").setValue(totalAmount);
                }
                else {
                    relativeLayoutHouse.setVisibility(View.GONE);
                    personalRef.child("monthHouse").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(monthlyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekEducationExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNmonth = "Education"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsTEducationAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("monthEdu").setValue(totalAmount);
                }
                else {
                    relativeLayoutEducation.setVisibility(View.GONE);
                    personalRef.child("monthEdu").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(monthlyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekHealthExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNmonth = "Health"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsHealthAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("monthHealth").setValue(totalAmount);
                }
                else {
                    relativeLayoutHealth.setVisibility(View.GONE);
                    personalRef.child("monthHealth").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(monthlyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekPersonal() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNmonth = "Personal"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsPersonalAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("monthPersonal").setValue(totalAmount);
                }
                else {
                    relativeLayoutPersonal.setVisibility(View.GONE);
                    personalRef.child("monthPersonal").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(monthlyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekApparel() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNmonth = "Apparel"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsApparelAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("monthApparel").setValue(totalAmount);
                }
                else {
                    relativeLayoutApparel.setVisibility(View.GONE);
                    personalRef.child("monthApparel").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(monthlyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekEntertainment() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNmonth = "Entertainment"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsEntertainmentAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("monthEnt").setValue(totalAmount);
                }
                else {
                    relativeLayoutEntertainment.setVisibility(View.GONE);
                    personalRef.child("monthEnt").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(monthlyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekShoppingExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNmonth = "Online Shopping"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsShoppingAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("monthShop").setValue(totalAmount);
                }
                else {
                    relativeLayoutShopping.setVisibility(View.GONE);
                    personalRef.child("monthShop").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(monthlyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekOthersExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNmonth = "Others"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsOthersAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("monthOthers").setValue(totalAmount);
                }
                else {
                    relativeLayoutOthers.setVisibility(View.GONE);
                    personalRef.child("monthOthers").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(monthlyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalMonthSpending() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("month").equalTo(months.getMonths());
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                    }
                    totalAmountSpentOn.setText("Total Month's spending: ₱ " + totalAmount);
                    totalSpent.setText("Total Spent: ₱ " + totalAmount);
                }
                //                    anyChartView.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGraph(){
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    int transTotal;
                    if (snapshot.hasChild("monthTrans")){
                        transTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthTrans").getValue()).toString());
                    }
                    else {
                        transTotal = 0;
                    }

                    int foodTotal;
                    if (snapshot.hasChild("monthFood")){
                        foodTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthFood").getValue()).toString());
                    }
                    else {
                        foodTotal = 0;
                    }

                    int houseTotal;
                    if (snapshot.hasChild("monthHouse")){
                        houseTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthHouse").getValue()).toString());
                    }
                    else {
                        houseTotal = 0;
                    }

                    int eduTotal;
                    if (snapshot.hasChild("monthEdu")){
                        eduTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthEdu").getValue()).toString());
                    }
                    else {
                        eduTotal = 0;
                    }

                    int healthTotal;
                    if (snapshot.hasChild("monthHealth")){
                        healthTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthHealth").getValue()).toString());
                    }
                    else {
                        healthTotal = 0;
                    }

                    int personalTotal;
                    if (snapshot.hasChild("monthPersonal")){
                        personalTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthPersonal").getValue()).toString());
                    }
                    else {
                        personalTotal = 0;
                    }

                    int apparelTotal;
                    if (snapshot.hasChild("monthApparel")){
                        apparelTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthApparel").getValue()).toString());
                    }
                    else {
                        apparelTotal = 0;
                    }

                    int entTotal;
                    if (snapshot.hasChild("monthEnt")){
                        entTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthEnt").getValue()).toString());
                    }
                    else {
                        entTotal = 0;
                    }

                    int shopTotal;
                    if (snapshot.hasChild("monthShop")){
                        shopTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthShop").getValue()).toString());
                    }
                    else {
                        shopTotal = 0;
                    }

                    int othersTotal;
                    if (snapshot.hasChild("monthOthers")){
                        othersTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthOthers").getValue()).toString());
                    }
                    else {
                        othersTotal = 0;
                    }

                }
                else {
                    Toast.makeText(monthlyAnalytics.this, "Child does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setStatusAndImageResource(){
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    float transTotal;
                    if (snapshot.hasChild("monthTrans")){
                        transTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthTrans").getValue()).toString());
                    }
                    else {
                        transTotal = 0;
                    }

                    float foodTotal;
                    if (snapshot.hasChild("monthFood")){
                        foodTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthFood").getValue()).toString());
                    }
                    else {
                        foodTotal = 0;
                    }

                    float houseTotal;
                    if (snapshot.hasChild("monthHouse")){
                        houseTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthHouse").getValue()).toString());
                    }
                    else {
                        houseTotal = 0;
                    }

                    float eduTotal;
                    if (snapshot.hasChild("monthEdu")){
                        eduTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthEdu").getValue()).toString());
                    }
                    else {
                        eduTotal = 0;
                    }

                    float healthTotal;
                    if (snapshot.hasChild("monthHealth")){
                        healthTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthHealth").getValue()).toString());
                    }
                    else {
                        healthTotal = 0;
                    }

                    float personalTotal;
                    if (snapshot.hasChild("monthPersonal")){
                        personalTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthPersonal").getValue()).toString());
                    }
                    else {
                        personalTotal = 0;
                    }

                    float apparelTotal;
                    if (snapshot.hasChild("monthApparel")){
                        apparelTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthApparel").getValue()).toString());
                    }
                    else {
                        apparelTotal = 0;
                    }

                    float entTotal;
                    if (snapshot.hasChild("monthEnt")){
                        entTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthEnt").getValue()).toString());
                    }
                    else {
                        entTotal = 0;
                    }

                    float shopTotal;
                    if (snapshot.hasChild("monthShop")){
                        shopTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthShop").getValue()).toString());
                    }
                    else {
                        shopTotal = 0;
                    }

                    float othersTotal;
                    if (snapshot.hasChild("monthOthers")){
                        othersTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthOthers").getValue()).toString());
                    }
                    else {
                        othersTotal = 0;
                    }

                    float totalSpent;
                    if (snapshot.hasChild("month")){
                        totalSpent = Integer.parseInt(Objects.requireNonNull(snapshot.child("month").getValue()).toString());
                    }
                    else {
                        totalSpent = 0;
                    }

                    // getting ratios
                    float transRatio;
                    if (snapshot.hasChild("monthTransRatio")){
                        transRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthTransRatio").getValue()).toString());
                    }
                    else {
                        transRatio = 0;
                    }

                    float foodRatio;
                    if (snapshot.hasChild("monthFoodRatio")){
                        foodRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthFoodRatio").getValue()).toString());
                    }
                    else {
                        foodRatio = 0;
                    }

                    float houseRatio;
                    if (snapshot.hasChild("monthHouseRatio")){
                        houseRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthHouseRatio").getValue()).toString());
                    }
                    else {
                        houseRatio = 0;
                    }

                    float eduRatio;
                    if (snapshot.hasChild("monthEduRatio")){
                        eduRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthEduRatio").getValue()).toString());
                    }
                    else {
                        eduRatio = 0;
                    }

                    float healthRatio;
                    if (snapshot.hasChild("monthHealthRatio")){
                        healthRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthHealthRatio").getValue()).toString());
                    }
                    else {
                        healthRatio = 0;
                    }

                    float personalRatio;
                    if (snapshot.hasChild("monthPersonalRatio")){
                        personalRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthPersonalRatio").getValue()).toString());
                    }
                    else {
                        personalRatio = 0;
                    }

                    float apparelRatio;
                    if (snapshot.hasChild("monthApparelRatio")){
                        apparelRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthApparelRatio").getValue()).toString());
                    }
                    else {
                        apparelRatio = 0;
                    }

                    float entRatio;
                    if (snapshot.hasChild("monthEntRatio")){
                        entRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthEntRatio").getValue()).toString());
                    }
                    else {
                        entRatio = 0;
                    }

                    float shopRatio;
                    if (snapshot.hasChild("monthShopRatio")){
                        shopRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthShopRatio").getValue()).toString());
                    }
                    else {
                        shopRatio = 0;
                    }

                    float othersRatio;
                    if (snapshot.hasChild("monthOthersRatio")){
                        othersRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("monthOthersRatio").getValue()).toString());
                    }
                    else {
                        othersRatio = 0;
                    }

                    float totalSpentRatio;
                    if (snapshot.hasChild("budget")){
                        totalSpentRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("budget").getValue()).toString());
                    }
                    else {
                        totalSpentRatio = 0;
                    }

                    // percent status image
                    float monthPercent = (totalSpent/totalSpentRatio)*100;
                    @SuppressLint("DefaultLocale") String monthSpending = String.format("%.2f", monthPercent) + "% used of ₱ " + String.format("%.2f", totalSpentRatio);

                    if (monthPercent < 50){
                        monthRatioSpending.setText(monthSpending);
                        monthRatioSpending_Image.setImageResource(R.drawable.green);
                    }
                    else if (monthPercent >= 50 && monthPercent < 100) {
                        monthRatioSpending.setText(monthSpending);
                        monthRatioSpending_Image.setImageResource(R.drawable.brown);
                    }
                    else if (monthPercent > 100 ){
                        monthRatioSpending.setText(monthSpending);
                        monthRatioSpending_Image.setImageResource(R.drawable.red);
                    }

                    // transportation
                    float transportPercent = (transTotal/transRatio)*100;
                    @SuppressLint("DefaultLocale") String transportSpending = String.format("%.2f", transportPercent) + "% used of ₱ " + String.format("%.2f", transRatio);

                    if (transportPercent < 50){
                        progress_ratio_transport.setText(transportSpending);
                        transport_status.setImageResource(R.drawable.green);
                    }
                    else if (transportPercent >= 50 && transportPercent < 100) {
                        progress_ratio_transport.setText(transportSpending);
                        transport_status.setImageResource(R.drawable.brown);
                    }
                    else {
                        progress_ratio_transport.setText(transportSpending);
                        transport_status.setImageResource(R.drawable.red);
                    }

                    // food
                    float foodPercent = (foodTotal/foodRatio)*100;
                    @SuppressLint("DefaultLocale") String foodSpending = String.format("%.2f", foodPercent) + "% used of ₱ " + String.format("%.2f", foodRatio);

                    if (foodPercent < 50){
                        progress_ratio_food.setText(foodSpending);
                        food_status.setImageResource(R.drawable.green);
                    }
                    else if (foodPercent >= 50 && foodPercent < 100) {
                        progress_ratio_food.setText(foodSpending);
                        food_status.setImageResource(R.drawable.brown);
                    }
                    else {
                        progress_ratio_food.setText(foodSpending);
                        food_status.setImageResource(R.drawable.red);
                    }

                    // house
                    float housePercent = (houseTotal/houseRatio)*100;
                    @SuppressLint("DefaultLocale") String houseSpending = String.format("%.2f", housePercent) + "% used of ₱ " + String.format("%.2f", houseRatio);

                    if (housePercent < 50){
                        progress_ratio_house.setText(houseSpending);
                        house_status.setImageResource(R.drawable.green);
                    }
                    else if (housePercent >= 50 && housePercent < 100) {
                        progress_ratio_house.setText(houseSpending);
                        house_status.setImageResource(R.drawable.brown);
                    }
                    else {
                        progress_ratio_house.setText(houseSpending);
                        house_status.setImageResource(R.drawable.red);
                    }

                    // education
                    float eduPercent = (eduTotal/eduRatio)*100;
                    @SuppressLint("DefaultLocale") String eduSpending = String.format("%.2f", eduPercent) + "% used of ₱ " + String.format("%.2f", eduRatio);

                    if (eduPercent < 50){
                        progress_ratio_education.setText(eduSpending);
                        education_status.setImageResource(R.drawable.green);
                    }
                    else if (eduPercent >= 50 && eduPercent < 100) {
                        progress_ratio_education.setText(eduSpending);
                        education_status.setImageResource(R.drawable.brown);
                    }
                    else {
                        progress_ratio_education.setText(eduSpending);
                        education_status.setImageResource(R.drawable.red);
                    }

                    // health
                    float healthPercent = (healthTotal/healthRatio)*100;
                    @SuppressLint("DefaultLocale") String healthSpending = String.format("%.2f", healthPercent) + "% used of ₱ " + String.format("%.2f", healthRatio);

                    if (healthPercent < 50){
                        progress_ratio_health.setText(healthSpending);
                        health_status.setImageResource(R.drawable.green);
                    }
                    else if (healthPercent >= 50 && healthPercent < 100) {
                        progress_ratio_health.setText(healthSpending);
                        health_status.setImageResource(R.drawable.brown);
                    }
                    else {
                        progress_ratio_health.setText(healthSpending);
                        health_status.setImageResource(R.drawable.red);
                    }

                    // personal
                    float personalPercent = (personalTotal/personalRatio)*100;
                    @SuppressLint("DefaultLocale") String personalSpending = String.format("%.2f", personalPercent) + "% used of ₱ " + String.format("%.2f", personalRatio);

                    if (personalPercent < 50){
                        progress_ratio_personal.setText(personalSpending);
                        personal_status.setImageResource(R.drawable.green);
                    }
                    else if (personalPercent >= 50 && personalPercent < 100) {
                        progress_ratio_personal.setText(personalSpending);
                        personal_status.setImageResource(R.drawable.brown);
                    }
                    else {
                        progress_ratio_personal.setText(personalSpending);
                        personal_status.setImageResource(R.drawable.red);
                    }

                    // apparel
                    float apparelPercent = (apparelTotal/apparelRatio)*100;
                    @SuppressLint("DefaultLocale") String apparelSpending = String.format("%.2f", apparelPercent) + "% used of ₱ " + String.format("%.2f", apparelRatio);

                    if (apparelPercent < 50){
                        progress_ratio_apparel.setText(apparelSpending);
                        apparel_status.setImageResource(R.drawable.green);
                    }
                    else if (apparelPercent >= 50 && apparelPercent < 100) {
                        progress_ratio_apparel.setText(apparelSpending);
                        apparel_status.setImageResource(R.drawable.brown);
                    }
                    else {
                        progress_ratio_apparel.setText(apparelSpending);
                        apparel_status.setImageResource(R.drawable.red);
                    }

                    // entertainment
                    float entPercent = (entTotal/entRatio)*100;
                    @SuppressLint("DefaultLocale") String entSpending = String.format("%.2f", entPercent) + "% used of ₱ " + String.format("%.2f", entRatio);

                    if (entPercent < 50){
                        progress_ratio_entertainment.setText(entSpending);
                        entertainment_status.setImageResource(R.drawable.green);
                    }
                    else if (entPercent >= 50 && entPercent < 100) {
                        progress_ratio_entertainment.setText(entSpending);
                        entertainment_status.setImageResource(R.drawable.brown);
                    }
                    else {
                        progress_ratio_entertainment.setText(entSpending);
                        entertainment_status.setImageResource(R.drawable.red);
                    }


                    // online shopping
                    float shopPercent = (shopTotal/shopRatio)*100;
                    @SuppressLint("DefaultLocale") String shopSpending = String.format("%.2f", shopPercent) + "% used of ₱ " + String.format("%.2f", shopRatio);

                    if (shopPercent < 50){
                        progress_ratio_shopping.setText(shopSpending);
                        shopping_status.setImageResource(R.drawable.green);
                    }
                    else if (shopPercent >= 50 && shopPercent < 100) {
                        progress_ratio_shopping.setText(shopSpending);
                        shopping_status.setImageResource(R.drawable.brown);
                    }
                    else {
                        progress_ratio_shopping.setText(shopSpending);
                        shopping_status.setImageResource(R.drawable.red);
                    }

                    // others
                    float othersPercent = (othersTotal/othersRatio)*100;
                    @SuppressLint("DefaultLocale") String othersSpending = String.format("%.2f", othersPercent) + "% used of ₱ " + String.format("%.2f", othersRatio);

                    if (othersPercent < 50){
                        progress_ratio_others.setText(othersSpending);
                        others_status.setImageResource(R.drawable.green);
                    }
                    else if (othersPercent >= 50 && othersPercent < 100) {
                        progress_ratio_others.setText(othersSpending);
                        others_status.setImageResource(R.drawable.brown);
                    }
                    else {
                        progress_ratio_others.setText(othersSpending);
                        others_status.setImageResource(R.drawable.red);
                    }
                }
                else {
                    Toast.makeText(monthlyAnalytics.this, "setStatusAndImageResource Errors", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}