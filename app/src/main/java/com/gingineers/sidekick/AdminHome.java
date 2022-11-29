package com.gingineers.sidekick;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.gingineers.sidekick.admin.ViewFeedback;
import com.gingineers.sidekick.admin.ViewUsers;
import com.google.firebase.auth.FirebaseAuth;

public class AdminHome extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        ImageButton logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminHome.this, MainActivity.class));
            finish();
        });

        ImageButton exit = findViewById(R.id.btnExit);
        exit.setOnClickListener(view -> {
            finishAffinity();
            System.exit(0);
        });

        CardView viewUsers = findViewById(R.id.viewUsers);
        viewUsers.setOnClickListener(view -> startActivity(new Intent(AdminHome.this, ViewUsers.class)));

        CardView viewFeedback = findViewById(R.id.viewFeedback);
        viewFeedback.setOnClickListener(view -> startActivity(new Intent(AdminHome.this, ViewFeedback.class)));

    }
}