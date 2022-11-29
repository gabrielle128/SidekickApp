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

import com.gingineers.sidekick.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;


public class SignUp extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;

    private EditText edtEmail, edtConfEmail, edtPassword, edtConfPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        Button signUp = findViewById(R.id.btnSignUp);
        signUp.setOnClickListener(this);

        edtEmail = findViewById(R.id.edtEmail);
        edtConfEmail = findViewById(R.id.edtConfEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfPassword = findViewById(R.id.edtConfPassword);

        TextView txtSignIn = findViewById(R.id.txtSignIn);
        txtSignIn.setOnClickListener(this);

        progressBar = findViewById(R.id.progressBar);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSignUp:
                signUp();
                break;

            case R.id.txtSignIn:
                startActivity(new Intent(this, SignIn.class));
        }
    }

    private void signUp() {
        String email = edtEmail.getText().toString().trim();
        String confirmEmail = edtConfEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfPassword.getText().toString().trim();

        if (email.isEmpty()){
            edtEmail.setError("Email is required.");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edtEmail.setError("Please provide a valid email.");
            edtEmail.requestFocus();
            return;
        }

        if (!confirmEmail.equals(email)){
            edtConfEmail.setError("Email does not match");
            edtConfEmail.requestFocus();
            return;
        }

        if (password.isEmpty()){
            edtPassword.setError("Password is required.");
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 6){
            edtPassword.setError("Password must be at least 6 characters");
            edtPassword.requestFocus();
            return;
        }

        if (!confirmPassword.equals(password)){
            edtConfPassword.setError("Password does not match.");
            edtConfPassword.requestFocus();
        }
        else {
            progressBar.setVisibility(View.VISIBLE);

            // create user with email and pass - wala nang gagalawin!!!
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {

                        if (task.isSuccessful()) {
                            Objects.requireNonNull(mAuth.getCurrentUser()).sendEmailVerification().addOnCompleteListener(task12 -> {
                                Toast.makeText(SignUp.this, "Check your email/spam folder to verify.", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                                User user = new User(email);
                                FirebaseDatabase.getInstance().getReference("users")
                                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                        .setValue(user).addOnCompleteListener(task1 -> startActivity(new Intent(SignUp.this, SignIn.class)));
                            });
                        }
                        else {
                            Toast.makeText(SignUp.this, "You have failed to register.", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }
}