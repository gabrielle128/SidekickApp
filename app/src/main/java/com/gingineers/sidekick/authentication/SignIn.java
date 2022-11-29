package com.gingineers.sidekick.authentication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gingineers.sidekick.AdminHome;
import com.gingineers.sidekick.Homescreen;
import com.gingineers.sidekick.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignIn extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;

    private EditText editTxtEmail1, editTxtPassword1;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        Button create = findViewById(R.id.btnCreate);
        create.setOnClickListener(this);

        Button signIn = findViewById(R.id.btnSignIn);
        signIn.setOnClickListener(this);

        editTxtEmail1 = findViewById(R.id.edtEmail1);
        editTxtPassword1 = findViewById(R.id.edtPassword1);

        progressBar = findViewById(R.id.progressBar);

        TextView forgotPass = findViewById(R.id.txtForgotPass);
        forgotPass.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnCreate:
                startActivity(new Intent(this, SignUp.class));
                break;

            case R.id.btnSignIn:
                userLogin();
                break;

            case R.id.txtForgotPass:
                startActivity(new Intent(this, ForgotPassword.class));
        }
    }


    private void userLogin() {
        String email = editTxtEmail1.getText().toString().trim();
        String password = editTxtPassword1.getText().toString().trim();

        if (email.isEmpty()){
            editTxtEmail1.setError("Email is required.");
            editTxtEmail1.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTxtEmail1.setError("Please provide a valid email.");
            editTxtEmail1.requestFocus();
            return;
        }

        if (password.isEmpty()){
            editTxtPassword1.setError("Password is required.");
            editTxtPassword1.requestFocus();
            return;
        }

        if (password.length() < 6){
            editTxtPassword1.setError("Password must be at least 6 characters");
            editTxtPassword1.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // login with email verif - wala na din gagalawin!!
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                if (Objects.requireNonNull(mAuth.getCurrentUser()).isEmailVerified()){

                    String email1 = editTxtEmail1.getText().toString();

                    if (email1.equals("gingineers.sidekick@gmail.com")){
                        startActivity(new Intent(getApplicationContext(), AdminHome.class));
                    }
                    else {
                        startActivity(new Intent(getApplicationContext(), Homescreen.class));
                    }

                }
                else{
                    Toast.makeText(SignIn.this, "Check your email/spam folder to verify.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }

            } else{
                Toast.makeText(SignIn.this, "Failed to login! Please check your credentials", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}