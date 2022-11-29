package com.gingineers.sidekick.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

public class budget extends AppCompatActivity {

    private TextView totalbudgetTV;
    private RecyclerView recyclerView;

    private DatabaseReference budgetRef, personalRef;

    private String post_key = "";
    private String item = "";
    private int amount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        budgetRef = FirebaseDatabase.getInstance().getReference().child("budget").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        budgetRef.keepSynced(true);
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(mAuth.getCurrentUser().getUid());
        personalRef.keepSynced(true);

        totalbudgetTV = findViewById(R.id.totalbudgetTV);
        recyclerView = findViewById(R.id.todayRV);

        ImageButton btnBackbudget = findViewById(R.id.btnBackbudget);
        btnBackbudget.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Wallet.class)));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        budgetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              int totalBudget = 0;

              for (DataSnapshot snap: snapshot.getChildren()){
                  Data data = snap.getValue(Data.class);
                  assert data != null;
                  totalBudget += data.getAmount();
                  String sTotal = "Month budget: ₱ " + totalBudget;
                  totalbudgetTV.setText(sTotal);
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FloatingActionButton addbudgetFAB = findViewById(R.id.addbudgetFAB);
        addbudgetFAB.setOnClickListener(v -> additem());

        budgetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount()>0){
                    int totalAmount = 0;
                    for (DataSnapshot snap: snapshot.getChildren()){
                        Data data = snap.getValue(Data.class);
                        assert data != null;
                        totalAmount += data.getAmount();
                        String stTotal = "Month budget: ₱ " + totalAmount;
                        totalbudgetTV.setText(stTotal);
                    }
                    int weeklyBudget = totalAmount/4;
                    int dailyBudget = totalAmount/30;
                    personalRef.child("budget").setValue(totalAmount);
                    personalRef.child("weeklyBudget").setValue(weeklyBudget);
                    personalRef.child("dailyBudget").setValue(dailyBudget);
                }
                else {
                    personalRef.child("budget").setValue(0);
                    personalRef.child("weeklyBudget").setValue(0);
                    personalRef.child("dailyBudget").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // set budget for daily, weekly, monthly
        getMonthTransportationBudgetRatios();
        getMonthFoodBudgetRatios();
        getMonthHouseBudgetRatios();
        getMonthEducationBudgetRatios();
        getMonthHealthBudgetRatios();
        getMonthPersonalBudgetRatios();
        getMonthApparelBudgetRatios();
        getMonthEntertainmentBudgetRatios();
        getMonthShoppingBudgetRatios();
        getMonthOthersBudgetRatios();
    }



    private void additem() {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.inputwallet_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final Spinner itemSpinner = myView.findViewById(R.id.itemspinner);
        final EditText amount = myView.findViewById(R.id.amountET);
        final Button cancel = myView.findViewById(R.id.btnCancel);
        final Button save = myView.findViewById(R.id.btnSaveitem);

        save.setOnClickListener(v -> {

            String budgetAmount = amount.getText().toString();
            String budgetItem = itemSpinner.getSelectedItem().toString();

            if (TextUtils.isEmpty(budgetAmount)){
                amount.setError("Amount is required");
                return;
            }

            if (budgetItem.equals("Select item")){
                Toast.makeText(budget.this, "Select a valid item.", Toast.LENGTH_SHORT).show();
            }

            else {

                String id = budgetRef.push().getKey();
                @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Calendar calendar = Calendar.getInstance();
                String date =dateFormat.format(calendar.getTime());

                MutableDateTime epoch = new MutableDateTime();
                epoch.setDate(0);
                DateTime now = new DateTime();
                Weeks weeks = Weeks.weeksBetween(epoch, now);
                Months months = Months.monthsBetween(epoch, now);

                String itemNday = budgetItem+date;
                String itemNweek = budgetItem+weeks.getWeeks();
                String itemNmonth = budgetItem+months.getMonths();

                // add wallet budget to firebase database
                Data data = new Data(budgetItem, date, id, itemNday, itemNweek, itemNmonth, Integer.parseInt(budgetAmount), months.getMonths(), weeks.getWeeks(), null);
                assert id != null;
                budgetRef.child(id).setValue(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(budget.this, "Budget item added successfully", Toast.LENGTH_SHORT).show();
                    }

                    else {
                        Toast.makeText(budget.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // display items added on budget activity
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>().setQuery(budgetRef, Data.class).build();

        FirebaseRecyclerAdapter<Data, BudgetViewHolder> budgetAdapter = new FirebaseRecyclerAdapter<Data, BudgetViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BudgetViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Data budgetmodel) {

                holder.setItemAmount("Allocated amount: ₱ " + budgetmodel.getAmount());
                holder.setDate("On: " + budgetmodel.getDate());
                holder.setItemName("Budget Item: "+ budgetmodel.getItem());

                holder.notes.setVisibility(View.GONE);

                switch (budgetmodel.getItem()){
                    case "Transportation":
                        holder.imageView.setImageResource(R.drawable.transportation);
                        break;

                    case "Food":
                        holder.imageView.setImageResource(R.drawable.food);
                        break;
                    case "House":
                        holder.imageView.setImageResource(R.drawable.house);
                        break;

                    case "Education":
                        holder.imageView.setImageResource(R.drawable.education);
                        break;

                    case "Health":
                        holder.imageView.setImageResource(R.drawable.health);
                        break;

                    case "Personal":
                        holder.imageView.setImageResource(R.drawable.personal);
                        break;

                    case "Apparel":
                        holder.imageView.setImageResource(R.drawable.apparel);
                        break;

                    case "Entertainment":
                        holder.imageView.setImageResource(R.drawable.entertainment);
                        break;

                    case "Online Shopping":
                        holder.imageView.setImageResource(R.drawable.shopping);
                        break;

                    case "Others":
                        holder.imageView.setImageResource(R.drawable.others);
                        break;
                }

                holder.mView.setOnClickListener(v -> {
                    post_key = getRef(position).getKey();
                    item = budgetmodel.getItem();
                    amount = budgetmodel.getAmount();
                    updateBudget();

                });

            }

            @NonNull
            @Override
            public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrivewallet_layout, parent, false);
                return new BudgetViewHolder(view);

            }
        };

        recyclerView.setAdapter(budgetAdapter);
        budgetAdapter.startListening();
        budgetAdapter.notifyDataSetChanged();

    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public ImageView imageView;
        public TextView notes;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            imageView = itemView.findViewById(R.id.imageView);
            notes = itemView.findViewById(R.id.notes);
        }

        public void setItemName (String itemName){
            TextView item = mView.findViewById(R.id.item);
            item.setText(itemName);
        }

        public void setItemAmount (String itemAmount){
            TextView amount = mView.findViewById(R.id.amount);
            amount.setText(itemAmount);
        }

        public void setDate (String setDate){
            TextView date = mView.findViewById(R.id.date);
            date.setText(setDate);
        }

    }

    private void updateBudget() {
        AlertDialog.Builder updateDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mView = inflater.inflate(R.layout.updatewallet_layout, null);

        updateDialog.setView(mView);
        final AlertDialog dialog = updateDialog.create();

        final TextView mItem = mView.findViewById(R.id.itemNameTV);
        final EditText mAmount = mView.findViewById(R.id.itemAmountET);
        final EditText mNotes = mView.findViewById(R.id.itemNoteET);

        mNotes.setVisibility(View.GONE);

        mItem.setText(item);

        mAmount.setText(String.valueOf(amount));
        mAmount.setSelection(String.valueOf(amount).length());

        Button delBtn = mView.findViewById(R.id.btnDeleteitem);
        Button updateBtn = mView.findViewById(R.id.btnUpdateitem);

        updateBtn.setOnClickListener(v -> {

            amount = Integer.parseInt(mAmount.getText().toString());

            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Calendar calendar = Calendar.getInstance();
            String date =dateFormat.format(calendar.getTime());

            MutableDateTime epoch = new MutableDateTime();
            epoch.setDate(0);
            DateTime now = new DateTime();
            Weeks weeks = Weeks.weeksBetween(epoch, now);
            Months months = Months.monthsBetween(epoch, now);

            String itemNday = item+date;
            String itemNweek = item+weeks.getWeeks();
            String itemNmonth = item+months.getMonths();

            Data data = new Data(item, date, post_key, itemNday, itemNweek, itemNmonth, amount, months.getMonths(), weeks.getWeeks(), null);
            budgetRef.child(post_key).setValue(data).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(budget.this, "Updated succesfully", Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(budget.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                }
            });

            dialog.dismiss();
        });

        delBtn.setOnClickListener(v -> {
            budgetRef.child(post_key).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(budget.this, "Deleted succesfully", Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(budget.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                }
            });

            dialog.dismiss();

        });

        dialog.show();
    }

    private void getMonthTransportationBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Transportation");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayTransRatio = pTotal/30;
                    int weekTransRatio = pTotal/4;
                    int monthTransRatio = pTotal;

                    personalRef.child("dayTransRatio").setValue(dayTransRatio);
                    personalRef.child("weekTransRatio").setValue(weekTransRatio);
                    personalRef.child("monthTransRatio").setValue(monthTransRatio);

                }
                else {
                    personalRef.child("dayTransRatio").setValue(0);
                    personalRef.child("weekTransRatio").setValue(0);
                    personalRef.child("monthTransRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getMonthFoodBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Food");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayFoodRatio = pTotal/30;
                    int weekFoodRatio = pTotal/4;
                    int monthFoodRatio = pTotal;

                    personalRef.child("dayFoodRatio").setValue(dayFoodRatio);
                    personalRef.child("weekFoodRatio").setValue(weekFoodRatio);
                    personalRef.child("monthFoodRatio").setValue(monthFoodRatio);

                }
                else {
                    personalRef.child("dayFoodRatio").setValue(0);
                    personalRef.child("weekFoodRatio").setValue(0);
                    personalRef.child("monthFoodRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getMonthHouseBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("House");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayHouseRatio = pTotal/30;
                    int weekHouseRatio = pTotal/4;
                    int monthHouseRatio = pTotal;

                    personalRef.child("dayHouseRatio").setValue(dayHouseRatio);
                    personalRef.child("weekHouseRatio").setValue(weekHouseRatio);
                    personalRef.child("monthHouseRatio").setValue(monthHouseRatio);

                }
                else {
                    personalRef.child("dayHouseRatio").setValue(0);
                    personalRef.child("weekHouseRatio").setValue(0);
                    personalRef.child("monthHouseRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getMonthEducationBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Education");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayEduRatio = pTotal/30;
                    int weekEduRatio = pTotal/4;
                    int monthEduRatio = pTotal;

                    personalRef.child("dayEduRatio").setValue(dayEduRatio);
                    personalRef.child("weekEduRatio").setValue(weekEduRatio);
                    personalRef.child("monthEduRatio").setValue(monthEduRatio);

                }
                else {
                    personalRef.child("dayEduRatio").setValue(0);
                    personalRef.child("weekEduRatio").setValue(0);
                    personalRef.child("monthEduRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getMonthHealthBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Health");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayHealthRatio = pTotal/30;
                    int weekHealthRatio = pTotal/4;
                    int monthHealthRatio = pTotal;

                    personalRef.child("dayHealthRatio").setValue(dayHealthRatio);
                    personalRef.child("weekHealthRatio").setValue(weekHealthRatio);
                    personalRef.child("monthHealthRatio").setValue(monthHealthRatio);

                }
                else {
                    personalRef.child("dayHealthRatio").setValue(0);
                    personalRef.child("weekHealthRatio").setValue(0);
                    personalRef.child("monthHealthRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getMonthPersonalBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Personal");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayPersonalRatio = pTotal/30;
                    int weekPersonalRatio = pTotal/4;
                    int monthPersonalRatio = pTotal;

                    personalRef.child("dayPersonalRatio").setValue(dayPersonalRatio);
                    personalRef.child("weekPersonalRatio").setValue(weekPersonalRatio);
                    personalRef.child("monthPersonalRatio").setValue(monthPersonalRatio);

                }
                else {
                    personalRef.child("dayPersonalRatio").setValue(0);
                    personalRef.child("weekPersonalRatio").setValue(0);
                    personalRef.child("monthPersonalRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getMonthApparelBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Apparel");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map != null ? map.get("amount") : null;
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayApparelRatio = pTotal/30;
                    int weekApparelRatio = pTotal/4;
                    int monthApparelRatio = pTotal;

                    personalRef.child("dayApparelRatio").setValue(dayApparelRatio);
                    personalRef.child("weekApparelRatio").setValue(weekApparelRatio);
                    personalRef.child("monthApparelRatio").setValue(monthApparelRatio);

                }
                else {
                    personalRef.child("dayApparelRatio").setValue(0);
                    personalRef.child("weekApparelRatio").setValue(0);
                    personalRef.child("monthApparelRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void getMonthEntertainmentBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Entertainment");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayEntRatio = pTotal/30;
                    int weekEntRatio = pTotal/4;
                    int monthEntRatio = pTotal;

                    personalRef.child("dayEntRatio").setValue(dayEntRatio);
                    personalRef.child("weekEntRatio").setValue(weekEntRatio);
                    personalRef.child("monthEntRatio").setValue(monthEntRatio);

                }
                else {
                    personalRef.child("dayEntRatio").setValue(0);
                    personalRef.child("weekEntRatio").setValue(0);
                    personalRef.child("monthEntRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getMonthShoppingBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Online Shopping");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayShopRatio = pTotal/30;
                    int weekShopRatio = pTotal/4;
                    int monthShopRatio = pTotal;

                    personalRef.child("dayShopRatio").setValue(dayShopRatio);
                    personalRef.child("weekShopRatio").setValue(weekShopRatio);
                    personalRef.child("monthShopRatio").setValue(monthShopRatio);

                }
                else {
                    personalRef.child("dayShopRatio").setValue(0);
                    personalRef.child("weekShopRatio").setValue(0);
                    personalRef.child("monthShopRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getMonthOthersBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Others");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        assert map != null;
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayOthersRatio = pTotal/30;
                    int weekOthersRatio = pTotal/4;
                    int monthOthersRatio = pTotal;

                    personalRef.child("dayOthersRatio").setValue(dayOthersRatio);
                    personalRef.child("weekOthersRatio").setValue(weekOthersRatio);
                    personalRef.child("monthOthersRatio").setValue(monthOthersRatio);

                }
                else {
                    personalRef.child("dayOthersRatio").setValue(0);
                    personalRef.child("weekOthersRatio").setValue(0);
                    personalRef.child("monthOthersRatio").setValue(0);
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
        startActivity(new Intent(getApplicationContext(), Wallet.class));
    }
}

