package com.gingineers.sidekick;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.gingineers.sidekick.authentication.SignIn;
import com.gingineers.sidekick.authentication.SignUp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authStateListener = firebaseAuth -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null){

                String email = user.getEmail();

                assert email != null;
                if (email.equals("gingineers.sidekick@gmail.com")){
                    startActivity(new Intent(getApplicationContext(), AdminHome.class));
                }
                else {
                    startActivity(new Intent(getApplicationContext(), Homescreen.class));
                }

                finish();
            }
        };

        mAuth = FirebaseAuth.getInstance();

        Button signup = findViewById(R.id.btnSignUp);
        signup.setOnClickListener(this);

        Button login = findViewById(R.id.btnLogIn);
        login.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSignUp:
                startActivity(new Intent(this, SignUp.class));
                break;

            case R.id.btnLogIn:
                startActivity(new Intent(this, SignIn.class));
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }

}