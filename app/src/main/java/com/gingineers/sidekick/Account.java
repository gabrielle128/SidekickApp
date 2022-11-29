package com.gingineers.sidekick;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.gingineers.sidekick.admin.FeedbackModel;
import com.gingineers.sidekick.authentication.ForgotPassword;
import com.gingineers.sidekick.authentication.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class Account extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    private TextView txtEmail;

    private DatabaseReference reference;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), Homescreen.class)));

        CardView about = findViewById(R.id.btnAbout);
        about.setOnClickListener(view -> startActivity(new Intent(Account.this, Information.class)));

        CardView logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Account.this, MainActivity.class));
            finish();
        });

        CardView changePassword = findViewById(R.id.btnChangePass);
        changePassword.setOnClickListener(view -> startActivity(new Intent(Account.this, ForgotPassword.class)));

        CardView btnFeedback = findViewById(R.id.btnFeedback);
        btnFeedback.setOnClickListener(view -> sendFeedback());

        CardView exit = findViewById(R.id.exit);
        exit.setOnClickListener(view -> {
            finishAffinity();
            System.exit(0);
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        assert mUser != null;
        String onlineUserId = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("feedback");
        reference.keepSynced(true);

        // display user email in account activity
        txtEmail = findViewById(R.id.txtEmail);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(onlineUserId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                String userId = user.getEmail();
                txtEmail.setText(userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

    private void sendFeedback() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.sendfeedback_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        // initialize widgets on sendfeedback_layout
        final EditText email = myView.findViewById(R.id.emailET);
        final EditText feedback = myView.findViewById(R.id.feedbackET);
        final Button cancel = myView.findViewById(R.id.btnCancel);
        final Button send = myView.findViewById(R.id.btnSend);

        cancel.setOnClickListener(v -> dialog.dismiss());

        send.setOnClickListener(view -> {
            String Email = email.getText().toString().trim();
            String Feedback = feedback.getText().toString().trim();
            String id = reference.push().getKey();

            Calendar calendar = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd-MMMM-yyyy hh:mm a");
            String date = simpleDateFormat.format(calendar.getTime());

            Calendar calendarTS = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat timestampDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            String timestamp = timestampDateFormat.format(calendarTS.getTime());

            if (TextUtils.isEmpty(Email)){
                email.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(Feedback)){
                feedback.setError("Feedback is required");
            }
            else {

                FeedbackModel model = new FeedbackModel(Email, Feedback, date, timestamp, id);
                assert id != null;
                reference.child(id).setValue(model).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(Account.this, "Feedback has been sent successfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String error = Objects.requireNonNull(task.getException()).toString();
                        Toast.makeText(Account.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}