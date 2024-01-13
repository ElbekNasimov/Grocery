package com.example.grocery.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.FilterProduct;
import com.example.grocery.FilterProductUser;
import com.example.grocery.R;
import com.example.grocery.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productsList, filterList;
    private FilterProductUser filter;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = productsList;
    }

    class HolderProductUser extends RecyclerView.ViewHolder{
        // ui views
        private ImageView productIconIV;
        private TextView discNoteTV, titleTV, quanTV, reserveTV, locTV, discPriceTV, priceTV;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);

            productIconIV = itemView.findViewById(R.id.productIconIV);
            discNoteTV = itemView.findViewById(R.id.discNoteTV);
            titleTV = itemView.findViewById(R.id.titleTV);
            quanTV = itemView.findViewById(R.id.quanTV);
            reserveTV = itemView.findViewById(R.id.reserveTV);
            locTV = itemView.findViewById(R.id.locTV);
            discPriceTV = itemView.findViewById(R.id.discPriceTV);
            priceTV = itemView.findViewById(R.id.priceTV);
        }
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user, parent, false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {

        // get data
        ModelProduct modelProduct = productsList.get(position);
        String isDisc = modelProduct.getPrIsDisc();
        String discNote = modelProduct.getPrDiscNote();
        String discPrice = modelProduct.getPrDiscPrice();
        String category = modelProduct.getPrCat();
        String location = modelProduct.getPrLoc();
        String price = modelProduct.getPrPrice();
        String desc = modelProduct.getPrDesc();
        String title = modelProduct.getPrTitle();
        String quantity = modelProduct.getPrQuan();
        String id = modelProduct.getPrID();
        String timestamp = modelProduct.getTimestamp();
        String prIcon = modelProduct.getPrIcon();
        String isReserve = modelProduct.getPrIsReserve();

        // set data
        holder.titleTV.setText(title);
        holder.quanTV.setText(quantity + " m");
        holder.locTV.setText("Joyi: " + location);
        if (isReserve.equals("true")) {
            holder.reserveTV.setText("Bron qilingan");
            holder.reserveTV.setTextColor(Color.RED);
        } else {
            holder.reserveTV.setText("");
        }
        holder.discNoteTV.setText(discNote);
        holder.discPriceTV.setText("$ " + discPrice);
        holder.priceTV.setText("$ " + price);
        if (isDisc.equals("true")) {
            // product is on discount
            holder.discPriceTV.setVisibility(View.VISIBLE);
            holder.discNoteTV.setVisibility(View.VISIBLE);
            holder.priceTV.setPaintFlags(holder.priceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // add strike through
        } else {
            // product is not on discount
            holder.discPriceTV.setVisibility(View.GONE);
            holder.discNoteTV.setVisibility(View.GONE);
            holder.priceTV.setPaintFlags(0);
        }
        try {
            Picasso.get().load(prIcon).placeholder(R.drawable.ic_add_shopping_white).into(holder.productIconIV);
        } catch (Exception e) {
            holder.productIconIV.setImageResource(R.drawable.ic_add_shopping_primary);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show product details
                detailsBottomSheet(modelProduct); //here modelProduct contains of clicked product
            }
        });
    }

    private void detailsBottomSheet(ModelProduct modelProduct) {
        // bottom sheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        // inflate view for bottomSheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_pr_details_seller, null);
        // set view to bottomSheet
        bottomSheetDialog.setContentView(view);

        // init views of bottomSheet
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        ImageButton editBtn = view.findViewById(R.id.editBtn);
        ImageButton delBtn = view.findViewById(R.id.delBtn);
        ImageView prIconIV = view.findViewById(R.id.prIconIV);
        CheckBox isReserveChB = view.findViewById(R.id.isReserveChB);
        TextView discNoteTV = view.findViewById(R.id.discNoteTV);
        TextView titleTV = view.findViewById(R.id.titleTV);
        TextView descTV = view.findViewById(R.id.descTV);
        TextView catTV = view.findViewById(R.id.catTV);
        TextView quanTV = view.findViewById(R.id.quanTV);
        TextView discPriceTV = view.findViewById(R.id.discPriceTV);
        TextView priceTV = view.findViewById(R.id.priceTV);

        // get data
        String id = modelProduct.getPrID();
        String uid = modelProduct.getUid();
        String isDisc = modelProduct.getPrIsDisc();
        String discNote = modelProduct.getPrDiscNote();
        String discPrice = modelProduct.getPrDiscPrice();
        String cat = modelProduct.getPrCat();
        String loc = modelProduct.getPrLoc();
        String desc = modelProduct.getPrDesc();
        String icon = modelProduct.getPrIcon();
        String quantity = modelProduct.getPrQuan();
        String title = modelProduct.getPrTitle();
        String timestamp = modelProduct.getTimestamp();
        String isReserve = modelProduct.getPrIsReserve();
        String price = modelProduct.getPrPrice();

        // set data
        titleTV.setText(title);
        descTV.setText(desc);
        catTV.setText(cat);
        quanTV.setText(quantity + " m");
        discNoteTV.setText(discNote);
        priceTV.setText("$ " + price);
        if (isReserve.equals("true")) {
            isReserveChB.setChecked(true);
        }
        discPriceTV.setText("$ " + discPrice);

        // users cannot change and delete items
        delBtn.setVisibility(View.GONE);
        editBtn.setVisibility(View.GONE);

        if (isDisc.equals("true")) {
            // product is on discount
            discPriceTV.setVisibility(View.VISIBLE);
            discNoteTV.setVisibility(View.VISIBLE);
            priceTV.setPaintFlags(priceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // add strike through
        } else {
            // product is not on discount
            discPriceTV.setVisibility(View.GONE);
            discNoteTV.setVisibility(View.GONE);
        }
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(prIconIV);
        } catch (Exception e) {
            prIconIV.setImageResource(R.drawable.ic_add_shopping_white);
        }

        bottomSheetDialog.show();

        isReserveChB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isReserve.equals("false")) {

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("prIsReserve", "true");
                DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Users");
                dataRef.child("8hmukKJ8PgaUhzOYusjWQYYvqkp1").child("Products").child(id)
                        .updateChildren(hashMap).addOnSuccessListener(unused -> {
                            Toast.makeText(context, "bron qilindi...", Toast.LENGTH_SHORT).show();

                            bottomSheetDialog.dismiss();
                        }).addOnFailureListener(e -> Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show());

            }

            if (isReserve.equals("true")) {

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("prIsReserve", "false");
                DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Users");
                dataRef.child("8hmukKJ8PgaUhzOYusjWQYYvqkp1").child("Products").child(id)
                        .updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "bron bekor bo'ldi...", Toast.LENGTH_SHORT).show();
                        isReserveChB.setChecked(false);
                        bottomSheetDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // back click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProductUser(this, filterList);
        }
        return filter;
    }

}