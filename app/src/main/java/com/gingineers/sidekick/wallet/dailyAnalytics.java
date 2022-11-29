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

import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.TimerTask;

public class dailyAnalytics extends AppCompatActivity {

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
        setContentView(R.layout.activity_daily_analytics);

        ImageButton btnBackdaily = findViewById(R.id.btnBackdaily);
        btnBackdaily.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), chooseAnalytic.class)));

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
        getTotalDaySpending();

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

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());
        String itemNday = "Transportation"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
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
                    personalRef.child("dayTrans").setValue(totalAmount);
                }
                else {
                    relativeLayoutTransportation.setVisibility(View.GONE);
                    personalRef.child("dayTrans").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dailyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekFoodExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());
        String itemNday = "Food"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
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
                    personalRef.child("dayFood").setValue(totalAmount);
                }
                else {
                    relativeLayoutFood.setVisibility(View.GONE);
                    personalRef.child("dayFood").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dailyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekHouseExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());
        String itemNday = "House"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsHouseAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("dayHouse").setValue(totalAmount);
                } else {
                    relativeLayoutHouse.setVisibility(View.GONE);
                    personalRef.child("dayHouse").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dailyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekEducationExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());
        String itemNday = "Education"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsTEducationAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("dayEdu").setValue(totalAmount);
                } else {
                    relativeLayoutEducation.setVisibility(View.GONE);
                    personalRef.child("dayEdu").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dailyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekHealthExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());
        String itemNday = "Health"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsHealthAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("dayHealth").setValue(totalAmount);
                } else {
                    relativeLayoutHealth.setVisibility(View.GONE);
                    personalRef.child("dayHealth").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dailyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekPersonal() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());
        String itemNday = "Personal"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsPersonalAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("dayPersonal").setValue(totalAmount);
                } else {
                    relativeLayoutPersonal.setVisibility(View.GONE);
                    personalRef.child("dayPersonal").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dailyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekApparel() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());
        String itemNday = "Apparel"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsApparelAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("dayApparel").setValue(totalAmount);
                } else {
                    relativeLayoutApparel.setVisibility(View.GONE);
                    personalRef.child("dayApparel").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dailyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekEntertainment() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());
        String itemNday = "Entertainment"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsEntertainmentAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("dayEnt").setValue(totalAmount);
                } else {
                    relativeLayoutEntertainment.setVisibility(View.GONE);
                    personalRef.child("dayEnt").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dailyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekShoppingExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());
        String itemNday = "Online Shopping"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsShoppingAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("dayShop").setValue(totalAmount);
                } else {
                    relativeLayoutShopping.setVisibility(View.GONE);
                    personalRef.child("dayShop").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dailyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getTotalWeekOthersExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); // set to epoch time

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());
        String itemNday = "Others"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsOthersAmount.setText("Spent: ₱ " + totalAmount);
                    }
                    personalRef.child("dayOthers").setValue(totalAmount);
                } else {
                    relativeLayoutOthers.setVisibility(View.GONE);
                    personalRef.child("dayOthers").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dailyAnalytics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDaySpending() {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = dateFormat.format(calendar.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    int totalAmount = 0;
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                    }
                    totalAmountSpentOn.setText("Total Day's spending: ₱ " + totalAmount);
                    totalSpent.setText("Total Spent: ₱ " + totalAmount);
                }
                else {
                    totalAmountSpentOn.setText("You have not spent today");
//                    anyChartView.setVisibility(View.GONE);
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
                    if (snapshot.hasChild("dayTrans")){
                        transTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayTrans").getValue()).toString());
                    }
                    else {
                        transTotal = 0;
                    }

                    int foodTotal;
                    if (snapshot.hasChild("dayFood")){
                        foodTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayFood").getValue()).toString());
                    }
                    else {
                        foodTotal = 0;
                    }

                    int houseTotal;
                    if (snapshot.hasChild("dayHouse")){
                        houseTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayHouse").getValue()).toString());
                    }
                    else {
                        houseTotal = 0;
                    }

                    int eduTotal;
                    if (snapshot.hasChild("dayEdu")){
                        eduTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayEdu").getValue()).toString());
                    }
                    else {
                        eduTotal = 0;
                    }

                    int healthTotal;
                    if (snapshot.hasChild("dayHealth")){
                        healthTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayHealth").getValue()).toString());
                    }
                    else {
                        healthTotal = 0;
                    }

                    int personalTotal;
                    if (snapshot.hasChild("dayPersonal")){
                        personalTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayPersonal").getValue()).toString());
                    }
                    else {
                        personalTotal = 0;
                    }

                    int apparelTotal;
                    if (snapshot.hasChild("dayApparel")){
                        apparelTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayApparel").getValue()).toString());
                    }
                    else {
                        apparelTotal = 0;
                    }

                    int entTotal;
                    if (snapshot.hasChild("dayEnt")){
                        entTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayEnt").getValue()).toString());
                    }
                    else {
                        entTotal = 0;
                    }

                    int shopTotal;
                    if (snapshot.hasChild("dayShop")){
                        shopTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayShop").getValue()).toString());
                    }
                    else {
                        shopTotal = 0;
                    }

                    int othersTotal;
                    if (snapshot.hasChild("dayOthers")){
                        othersTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayOthers").getValue()).toString());
                    }
                    else {
                        othersTotal = 0;
                    }

                }
                else {
                    Toast.makeText(dailyAnalytics.this, "Child does not exist", Toast.LENGTH_SHORT).show();
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
                    if (snapshot.hasChild("dayTrans")){
                        transTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayTrans").getValue()).toString());
                    }
                    else {
                        transTotal = 0;
                    }

                    float foodTotal;
                    if (snapshot.hasChild("dayFood")){
                        foodTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayFood").getValue()).toString());
                    }
                    else {
                        foodTotal = 0;
                    }

                    float houseTotal;
                    if (snapshot.hasChild("dayHouse")){
                        houseTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayHouse").getValue()).toString());
                    }
                    else {
                        houseTotal = 0;
                    }

                    float eduTotal;
                    if (snapshot.hasChild("dayEdu")){
                        eduTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayEdu").getValue()).toString());
                    }
                    else {
                        eduTotal = 0;
                    }

                    float healthTotal;
                    if (snapshot.hasChild("dayHealth")){
                        healthTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayHealth").getValue()).toString());
                    }
                    else {
                        healthTotal = 0;
                    }

                    float personalTotal;
                    if (snapshot.hasChild("dayPersonal")){
                        personalTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayPersonal").getValue()).toString());
                    }
                    else {
                        personalTotal = 0;
                    }

                    float apparelTotal;
                    if (snapshot.hasChild("dayApparel")){
                        apparelTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayApparel").getValue()).toString());
                    }
                    else {
                        apparelTotal = 0;
                    }

                    float entTotal;
                    if (snapshot.hasChild("dayEnt")){
                        entTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayEnt").getValue()).toString());
                    }
                    else {
                        entTotal = 0;
                    }

                    float shopTotal;
                    if (snapshot.hasChild("dayShop")){
                        shopTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayShop").getValue()).toString());
                    }
                    else {
                        shopTotal = 0;
                    }

                    float othersTotal;
                    if (snapshot.hasChild("dayOthers")){
                        othersTotal = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayOthers").getValue()).toString());
                    }
                    else {
                        othersTotal = 0;
                    }

                    float totalSpent;
                    if (snapshot.hasChild("today")){
                        totalSpent = Integer.parseInt(Objects.requireNonNull(snapshot.child("today").getValue()).toString());
                    }
                    else {
                        totalSpent = 0;
                    }

                    // getting ratios
                    float transRatio;
                    if (snapshot.hasChild("dayTransRatio")){
                        transRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayTransRatio").getValue()).toString());
                    }
                    else {
                        transRatio = 0;
                    }

                    float foodRatio;
                    if (snapshot.hasChild("dayFoodRatio")){
                        foodRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayFoodRatio").getValue()).toString());
                    }
                    else {
                        foodRatio = 0;
                    }

                    float houseRatio;
                    if (snapshot.hasChild("dayHouseRatio")){
                        houseRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayHouseRatio").getValue()).toString());
                    }
                    else {
                        houseRatio = 0;
                    }

                    float eduRatio;
                    if (snapshot.hasChild("dayEduRatio")){
                        eduRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayEduRatio").getValue()).toString());
                    }
                    else {
                        eduRatio = 0;
                    }

                    float healthRatio;
                    if (snapshot.hasChild("dayHealthRatio")){
                        healthRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayHealthRatio").getValue()).toString());
                    }
                    else {
                        healthRatio = 0;
                    }

                    float personalRatio;
                    if (snapshot.hasChild("dayPersonalRatio")){
                        personalRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayPersonalRatio").getValue()).toString());
                    }
                    else {
                        personalRatio = 0;
                    }

                    float apparelRatio;
                    if (snapshot.hasChild("dayApparelRatio")){
                        apparelRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayApparelRatio").getValue()).toString());
                    }
                    else {
                        apparelRatio = 0;
                    }

                    float entRatio;
                    if (snapshot.hasChild("dayEntRatio")){
                        entRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayEntRatio").getValue()).toString());
                    }
                    else {
                        entRatio = 0;
                    }

                    float shopRatio;
                    if (snapshot.hasChild("dayShopRatio")){
                        shopRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayShopRatio").getValue()).toString());
                    }
                    else {
                        shopRatio = 0;
                    }

                    float othersRatio;
                    if (snapshot.hasChild("dayOthersRatio")){
                        othersRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("dayOthersRatio").getValue()).toString());
                    }
                    else {
                        othersRatio = 0;
                    }

                    float totalSpentRatio;
                    if (snapshot.hasChild("dailyBudget")){
                        totalSpentRatio = Integer.parseInt(Objects.requireNonNull(snapshot.child("dailyBudget").getValue()).toString());
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
                    Toast.makeText(dailyAnalytics.this, "setStatusAndImageResource Errors", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}