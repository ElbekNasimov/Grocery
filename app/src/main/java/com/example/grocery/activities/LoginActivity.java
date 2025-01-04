package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    // UI views
    private EditText emailET, passET;
    private TextView forgotTV, noAccTV;
    private Button loginBtn;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // init UI views
        init();

//        noAccTV.setVisibility(View.GONE);

        noAccTV.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class));
            finish();
        });

        forgotTV.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, ForgotPassActivity.class));
                finish();
        });

        loginBtn.setOnClickListener(v -> loginUser());
    }

    protected void init(){
        emailET = findViewById(R.id.emailET);
        passET = findViewById(R.id.passET);
        forgotTV = findViewById(R.id.forgotTV);
        noAccTV = findViewById(R.id.noAccTV);
        loginBtn = findViewById(R.id.loginBtn);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

    }

    private String email, password;
    private void loginUser() {
        email = emailET.getText().toString().trim();
        password = passET.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            // logged in successfully
            checkUserType();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(LoginActivity.this, "sign in: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    private void checkUserType() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String accountType = ""+ds.child("accountType").getValue();
                    if (accountType.equals("Seller")){
                        // user is seller
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "" + accountType, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainAdminActivity.class));
                        finish();
                    } else {
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}