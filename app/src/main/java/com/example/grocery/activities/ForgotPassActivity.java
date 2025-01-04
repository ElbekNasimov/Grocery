package com.example.grocery.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.grocery.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText emailET;
    private Button recoverBtn;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        init();

        backBtn.setOnClickListener(v -> onBackPressed());

        recoverBtn.setOnClickListener(v -> recoverPass());
    }

    private String email;
    private void recoverPass() {
        email = emailET.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "invalid email", Toast.LENGTH_SHORT).show();
        }
        progressDialog.setMessage("sending for reset password");
        progressDialog.show();

        auth.sendPasswordResetEmail(email).addOnSuccessListener(unused -> {
            // instruction sent
            progressDialog.dismiss();
            Toast.makeText(ForgotPassActivity.this, "Reset email sent. Check email inbox", Toast.LENGTH_SHORT).show();

        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(ForgotPassActivity.this, "Password reset: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    protected void init(){
        backBtn = findViewById(R.id.backBtn);
        emailET = findViewById(R.id.emailET);
        recoverBtn = findViewById(R.id.recoveryBtn);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
    }
}