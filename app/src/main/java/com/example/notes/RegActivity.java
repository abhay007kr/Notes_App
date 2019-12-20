package com.example.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegActivity extends AppCompatActivity {

    private EditText inputName, inputEmail, inputPassword;
    private Button btnSignUp ;
    private ProgressBar progressBar;
    private FirebaseAuth auth;          //provided by firebase for email-password Verification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();          //to get data at particular Instance
        inputName=(EditText)findViewById(R.id.regInName);
        btnSignUp = (Button) findViewById(R.id.regButton);
        inputEmail = (EditText) findViewById(R.id.regInEmail);
        inputPassword = (EditText) findViewById(R.id.regInPass);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(RegActivity.this, "Succesfully Registered" , Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    Toast.makeText(RegActivity.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    startActivity(new Intent(RegActivity.this, MainActivity.class));
                                    finish();
                                }
                            }
                        });
            }
        });
    }
        @Override
        protected void onResume() {             //to resume the function Again
            super.onResume();
            progressBar.setVisibility(View.GONE);
        }
}