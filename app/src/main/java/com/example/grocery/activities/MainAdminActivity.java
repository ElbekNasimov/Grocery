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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.constants.Constants;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterProductSeller;
import com.example.grocery.crud.AddProductActivity;
import com.example.grocery.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainAdminActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private TextView nameTV, shopNameTV, emailTV, tabProdsTV, tabOrdersTV, filterPrTV;
    private EditText searchET;
    private ImageButton logoutBtn, addProductBtn, filterPrBtn;
    private ImageView profCIV;
    private ProgressDialog progressDialog;
    private RecyclerView productRV;
    private RelativeLayout toolbarRL, productsRL, ordersRL;
    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        init();

        checkUser();

        loadAllProducts();

        showProductsUI();

        // search
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        addProductBtn.setOnClickListener(v -> startActivity(new Intent(MainAdminActivity.this,
                AddProductActivity.class)));

        tabProdsTV.setOnClickListener(v -> {
            // load products
            showProductsUI();
        });

        tabOrdersTV.setOnClickListener(v -> {
            // load orders
            showOrdersUI();
        });

        filterPrBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainAdminActivity.this);
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
        logoutBtn.setOnClickListener(v -> makeMeOffline());
    }

    private void init(){
        toolbarRL = findViewById(R.id.toolbarRL);
        nameTV = findViewById(R.id.nameTV);
        logoutBtn = findViewById(R.id.logoutBtn);
        addProductBtn = findViewById(R.id.addProductBtn);
        profCIV = findViewById(R.id.profCIV);
        shopNameTV = findViewById(R.id.shopNameTV);
        emailTV = findViewById(R.id.emailTV);
        tabProdsTV = findViewById(R.id.tabProdsTV);
        tabOrdersTV = findViewById(R.id.tabOrdersTV);
        productsRL = findViewById(R.id.productsRL);
        ordersRL = findViewById(R.id.ordersRL);
        searchET = findViewById(R.id.searchET);
        filterPrBtn = findViewById(R.id.filterPrBtn);
        productRV = findViewById(R.id.productRV);
        filterPrTV = findViewById(R.id.filterPrTV);

        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void checkUser() {
        FirebaseUser user = auth.getCurrentUser();

        if (user==null){
            startActivity(new Intent(MainAdminActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();
        // get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // before getting reset list
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                    productList.add(modelProduct);
                }
                // setup adapter
                adapterProductSeller = new AdapterProductSeller(MainAdminActivity.this, productList);
                // set adapter
                productRV.setAdapter(adapterProductSeller);
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
        reference.child(auth.getUid()).child("Products").addValueEventListener(new ValueEventListener() {
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
                adapterProductSeller = new AdapterProductSeller(MainAdminActivity.this, productList);
                // set adapter
                productRV.setAdapter(adapterProductSeller);
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
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // get data from db
                            String username = "" + ds.child("username").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String shopName = "" + ds.child("shopName").getValue();
                            String email = "" + ds.child("email").getValue();
                            String profileImg = "" + ds.child("profileImg").getValue();
                            // set data to UI
                            nameTV.setText(username + "(" + accountType + ")");
                            shopNameTV.setText(shopName);
                            emailTV.setText(email);
                            try {
                                Picasso.get().load(profileImg).placeholder(R.drawable.ic_store_gray).into(profCIV);
                            } catch (Exception e){
                                profCIV.setImageResource(R.drawable.ic_store_gray);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
//                        Toast.makeText(MainAdminActivity.this, "loadMyInfo: " + error.toString(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void showProductsUI(){
        // show products UI and hide orders UI
        productsRL.setVisibility(View.VISIBLE);
        ordersRL.setVisibility(View.GONE);

        tabProdsTV.setTextColor(getResources().getColor(R.color.black));
        tabProdsTV.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTV.setTextColor(getResources().getColor(R.color.white));
        tabOrdersTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI(){
        // show orders UI and hide products UI
        productsRL.setVisibility(View.GONE);
        ordersRL.setVisibility(View.VISIBLE);

        tabOrdersTV.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTV.setBackgroundResource(R.drawable.shape_rect04);

        tabProdsTV.setTextColor(getResources().getColor(R.color.white));
        tabProdsTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MainAdminActivity.this, "makeMeOnline: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}