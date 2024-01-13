package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.constants.Constants;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterProductUser;
import com.example.grocery.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTV, filterPrTV;
    private EditText searchET;
    private ImageButton logoutBtn, filterPrBtn;
    private RecyclerView productRV;
    private ProgressDialog progressDialog;
    private ArrayList<ModelProduct> productList;
    private AdapterProductUser adapterProductUser;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        init();

        checkUser();

        loadAllProducts();

//        logoutBtn.setVisibility(View.GONE);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeOffline();
            }
        });

        // search
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        filterPrBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainUserActivity.this);
            builder.setTitle("Kategoriyani tanlang").setItems(Constants.categories1, (dialog, which) -> {
                // get selected item
                String selected = Constants.categories1[which];
                filterPrTV.setText(selected);
                if (selected.equals("Hammasi")) {
                    // load all
                    loadAllProducts();
                } else {
                    // load filtered
                    loadFilteredProducts(selected);
                }
            }).show();
        });

    }

    private void makeMeOffline() {
        // after logging out, make user offline
        progressDialog.setMessage("Logging Out User...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        // update value to db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).updateChildren(hashMap).addOnSuccessListener(unused -> {
            // update successfully
            auth.signOut();
            checkUser();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(MainUserActivity.this, "makeMeOnline: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void checkUser() {
        FirebaseUser user = auth.getCurrentUser();
        if (user==null){
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();
        // get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child("8hmukKJ8PgaUhzOYusjWQYYvqkp1").child("Products")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // before getting reset list
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                    productList.add(modelProduct);
                }
                // setup adapter
                adapterProductUser = new AdapterProductUser(MainUserActivity.this, productList);
                // set adapter
                productRV.setAdapter(adapterProductUser);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadFilteredProducts(String selected) {
        productList = new ArrayList<>();
        // get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child("8hmukKJ8PgaUhzOYusjWQYYvqkp1").child("Products")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // before getting reset list
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String productCategory = "" + ds.child("prCat").getValue();

                    // if selected category matches category then add in list
                    if (selected.equals(productCategory)){
                        ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                        productList.add(modelProduct);
                    }
                }
                // setup adapter
                adapterProductUser = new AdapterProductUser(MainUserActivity.this, productList);
                // set adapter
                productRV.setAdapter(adapterProductUser);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String username = "" + ds.child("username").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            nameTV.setText(username + "(" + accountType + ")");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
//                        Toast.makeText(MainUserActivity.this, "loadMyInfo: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void init(){
        nameTV = findViewById(R.id.nameTV);
        searchET = findViewById(R.id.searchET);
        logoutBtn = findViewById(R.id.logoutBtn);
        filterPrTV = findViewById(R.id.filterPrTV);
        filterPrBtn = findViewById(R.id.filterPrBtn);
        productRV = findViewById(R.id.productRV);

        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

}