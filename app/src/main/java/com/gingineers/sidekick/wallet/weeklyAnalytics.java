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
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import java.util.Map;
import java.util.Objects;
import java.util.TimerTask;

public class weeklyAnalytics extends AppCompatActivity {

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
        setContentView(R.layout.activity_weekly_analytics);

        ImageButton btnBackweekly = findViewById(R.id.btnBackweekly);
        btnBackweekly.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), chooseAnalytic.class)));

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
        getTotalWeekSpending();

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
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Transportation"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekTrans").setValue(totalAmount);
                }
                else {
                    relativeLayoutTransportation.setVisibility(View.GONE);
                    personalRef.child("weekTrans").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(weeklyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekFoodExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Food"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekFood").setValue(totalAmount);
                }
                else {
                    relativeLayoutFood.setVisibility(View.GONE);
                    personalRef.child("weekFood").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(weeklyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekHouseExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "House"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekHouse").setValue(totalAmount);
                }
                else {
                    relativeLayoutHouse.setVisibility(View.GONE);
                    personalRef.child("weekHouse").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(weeklyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekEducationExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Education"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekEdu").setValue(totalAmount);
                }
                else {
                    relativeLayoutEducation.setVisibility(View.GONE);
                    personalRef.child("weekEdu").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(weeklyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekHealthExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Health"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekHealth").setValue(totalAmount);
                }
                else {
                    relativeLayoutHealth.setVisibility(View.GONE);
                    personalRef.child("weekHealth").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(weeklyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekPersonal() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Personal"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekPersonal").setValue(totalAmount);
                }
                else {
                    relativeLayoutPersonal.setVisibility(View.GONE);
                    personalRef.child("weekPersonal").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(weeklyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekApparel() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Apparel"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekApparel").setValue(totalAmount);
                }
                else {
                    relativeLayoutApparel.setVisibility(View.GONE);
                    personalRef.child("weekApparel").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(weeklyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekEntertainment() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Entertainment"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekEnt").setValue(totalAmount);
                }
                else {
                    relativeLayoutEntertainment.setVisibility(View.GONE);
                    personalRef.child("weekEnt").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(weeklyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekShoppingExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Online Shopping"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekShop").setValue(totalAmount);
                }
                else {
                    relativeLayoutShopping.setVisibility(View.GONE);
                    personalRef.child("weekShop").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(weeklyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekOthersExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Others"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekOthers").setValue(totalAmount);
                }
                else {
                    relativeLayoutOthers.setVisibility(View.GONE);
                    personalRef.child("weekOthers").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(weeklyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeekSpending() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("week").equalTo(weeks.getWeeks());
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
                    totalAmountSpentOn.setText("Total Week's spending: ₱ " + totalAmount);
                    totalSpent.setText("Total Spent: ₱ " + totalAmount);
                }
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
                    if (snapshot.hasChild("weekTrans")){
                        transTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekTrans").getValue()).toString());
                    }
                    else {
                        transTotal = 0;
                    }

                    int foodTotal;
                    if (snapshot.hasChild("weekFood")){
                        foodTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekFood").getValue()).toString());
                    }
                    else {
                        foodTotal = 0;
                    }

                    int houseTotal;
                    if (snapshot.hasChild("weekHouse")){
                        houseTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekHouse").getValue()).toString());
                    }
                    else {
                        houseTotal = 0;
                    }

                    int eduTotal;
                    if (snapshot.hasChild("weekEdu")){
                        eduTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekEdu").getValue()).toString());
                    }
                    else {
                        eduTotal = 0;
                    }

                    int healthTotal;
                    if (snapshot.hasChild("weekHealth")){
                        healthTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekHealth").getValue()).toString());
                    }
                    else {
                        healthTotal = 0;
                    }

                    int personalTotal;
                    if (snapshot.hasChild("weekPersonal")){
                        personalTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekPersonal").getValue()).toString());
                    }
                    else {
                        personalTotal = 0;
                    }

                    int apparelTotal;
                    if (snapshot.hasChild("weekApparel")){
                        apparelTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekApparel").getValue()).toString());
                    }
                    else {
                        apparelTotal = 0;
                    }

                    int entTotal;
                    if (snapshot.hasChild("weekEnt")){
                        entTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekEnt").getValue()).toString());
                    }
                    else {
                        entTotal = 0;
                    }

                    int shopTotal;
                    if (snapshot.hasChild("weekShop")){
                        shopTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekShop").getValue()).toString());
                    }
                    else {
                        shopTotal = 0;
                    }

                    int othersTotal;
                    if (snapshot.hasChild("weekOthers")){
                        othersTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekOthers").getValue()).toString());
                    }
                    else {
                        othersTotal = 0;
                    }

                }
                else {
                    Toast.makeText(weeklyAnalytics.this, "Child does not exist", Toast.LENGTH_SHORT).show();
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
                    if (snapshot.hasChild("weekTrans")){
                        transTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekTrans").getValue()).toString());
                    }
                    else {
                        transTotal = 0;
                    }

                    float foodTotal;
                    if (snapshot.hasChild("weekFood")){
                        foodTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekFood").getValue()).toString());
                    }
                    else {
                        foodTotal = 0;
                    }

                    float houseTotal;
                    if (snapshot.hasChild("weekHouse")){
                        houseTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekHouse").getValue()).toString());
                    }
                    else {
                        houseTotal = 0;
                    }

                    float eduTotal;
                    if (snapshot.hasChild("weekEdu")){
                        eduTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekEdu").getValue()).toString());
                    }
                    else {
                        eduTotal = 0;
                    }

                    float healthTotal;
                    if (snapshot.hasChild("weekHealth")){
                        healthTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekHealth").getValue()).toString());
                    }
                    else {
                        healthTotal = 0;
                    }

                    float personalTotal;
                    if (snapshot.hasChild("weekPersonal")){
                        personalTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekPersonal").getValue()).toString());
                    }
                    else {
                        personalTotal = 0;
                    }

                    float apparelTotal;
                    if (snapshot.hasChild("weekApparel")){
                        apparelTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekApparel").getValue()).toString());
                    }
                    else {
                        apparelTotal = 0;
                    }

                    float entTotal;
                    if (snapshot.hasChild("weekEnt")){
                        entTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekEnt").getValue()).toString());
                    }
                    else {
                        entTotal = 0;
                    }

                    float shopTotal;
                    if (snapshot.hasChild("weekShop")){
                        shopTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekShop").getValue()).toString());
                    }
                    else {
                        shopTotal = 0;
                    }

                    float othersTotal;
                    if (snapshot.hasChild("weekOthers")){
                        othersTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekOthers").getValue()).toString());
                    }
                    else {
                        othersTotal = 0;
                    }

                    float totalSpent;
                    if (snapshot.hasChild("week")){
                        totalSpent = Integer.parseInt(Objects.requireNonNull(snapshot.child("week").getValue()).toString());
                    }
                    else {
                        totalSpent = 0;
                    }

                    // getting ratios
                    float transRatio;
                    if (snapshot.hasChild("weekTransRatio")){
                        transRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekTransRatio").getValue()).toString());
                    }
                    else {
                        transRatio = 0;
                    }

                    float foodRatio;
                    if (snapshot.hasChild("weekFoodRatio")){
                        foodRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekFoodRatio").getValue()).toString());
                    }
                    else {
                        foodRatio = 0;
                    }

                    float houseRatio;
                    if (snapshot.hasChild("weekHouseRatio")){
                        houseRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekHouseRatio").getValue()).toString());
                    }
                    else {
                        houseRatio = 0;
                    }

                    float eduRatio;
                    if (snapshot.hasChild("weekEduRatio")){
                        eduRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekEduRatio").getValue()).toString());
                    }
                    else {
                        eduRatio = 0;
                    }

                    float healthRatio;
                    if (snapshot.hasChild("weekHealthRatio")){
                        healthRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekHealthRatio").getValue()).toString());
                    }
                    else {
                        healthRatio = 0;
                    }

                    float personalRatio;
                    if (snapshot.hasChild("weekPersonalRatio")){
                        personalRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekPersonalRatio").getValue()).toString());
                    }
                    else {
                        personalRatio = 0;
                    }

                    float apparelRatio;
                    if (snapshot.hasChild("weekApparelRatio")){
                        apparelRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekApparelRatio").getValue()).toString());
                    }
                    else {
                        apparelRatio = 0;
                    }

                    float entRatio;
                    if (snapshot.hasChild("weekEntRatio")){
                        entRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekEntRatio").getValue()).toString());
                    }
                    else {
                        entRatio = 0;
                    }

                    float shopRatio;
                    if (snapshot.hasChild("weekShopRatio")){
                        shopRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekShopRatio").getValue()).toString());
                    }
                    else {
                        shopRatio = 0;
                    }

                    float othersRatio;
                    if (snapshot.hasChild("weekOthersRatio")){
                        othersRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("weekOthersRatio").getValue()).toString());
                    }
                    else {
                        othersRatio = 0;
                    }

                    float totalSpentRatio;
                    if (snapshot.hasChild("weeklyBudget")){
                        totalSpentRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("weeklyBudget").getValue()).toString());
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

                    // transport
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
                    Toast.makeText(weeklyAnalytics.this, "setStatusAndImageResource Errors", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}