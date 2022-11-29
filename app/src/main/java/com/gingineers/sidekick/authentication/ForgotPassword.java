package com.gingineers.sidekick.authentication;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gingineers.sidekick.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText txtEmailReset;
    private ProgressBar progressBar;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        txtEmailReset = findViewById(R.id.edtEmailAdd);
        Button btnResetPass = findViewById(R.id.btnResetPass);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

        btnResetPass.setOnClickListener(view -> resetPass());
    }

    private void resetPass(){
        String email = txtEmailReset.getText().toString().trim();

        if (email.isEmpty()){
            txtEmailReset.setError("Email is required.");
            txtEmailReset.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            txtEmailReset.setError("Please provide valid email.");
            txtEmailReset.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {

            if (task.isSuccessful()){
                Toast.makeText(ForgotPassword.this, "Check your email to reset your password.", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);

            } else {
                Toast.makeText(ForgotPassword.this, "Something wrong happened, please try again.", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);

            }
        });

    }
}