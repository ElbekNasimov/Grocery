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
import android.view.ViewGroup;
import android.widget.Button;
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

    private String checked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        init();

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
        logoutBtn.setOnClickListener(v -> {
            auth.signOut();
            checkUser();
        });
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
            Toast.makeText(this, "user auth " + user, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainAdminActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "no user " + user, Toast.LENGTH_SHORT).show();
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
                double sum = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String productCategory = "" + ds.child("prCat").getValue();
                    ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                    productList.add(modelProduct);
                    double price = Double.parseDouble("" + ds.child("prPrice").getValue());
                    double quan = Double.parseDouble("" + ds.child("prQuan").getValue());
                    sum += price * quan;
                }
                // setup adapter
                adapterProductSeller = new AdapterProductSeller(MainAdminActivity.this, productList, sum);
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
                // setup adapter
                double sum = 0;
                adapterProductSeller = new AdapterProductSeller(MainAdminActivity.this, productList, sum);
                // set adapter
                productRV.setAdapter(adapterProductSeller);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void loadMyInfo() {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
        reference1.child(auth.getUid()).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // before getting reset list
                double sum = 0;
                int count = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    double prPrice = Double.parseDouble("" + ds.child("prPrice").getValue());
                    double prQuan = Double.parseDouble("" + ds.child("prQuan").getValue());
                    sum += prPrice * prQuan;
                    count++;
                }
                // setup adapter
                // setup adapter
                shopNameTV.setText(Double.toString(sum) + "qator " + count);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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