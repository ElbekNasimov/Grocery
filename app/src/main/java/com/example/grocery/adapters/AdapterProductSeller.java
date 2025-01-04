package com.example.grocery.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.FilterProduct;
import com.example.grocery.R;
import com.example.grocery.crud.EditProductActivity;
import com.example.grocery.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productList, filterList;
    private FilterProduct filter;
    private double summa;

    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList, double sum) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
        this.summa = sum;
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProduct(this, filterList);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder {
        // holds views of recView
        private ImageView productIconIV;
        private TextView discNoteTV, titleTV, quanTV, reserveTV, locTV, discPriceTV, priceTV;

        public HolderProductSeller(@NonNull View itemView) {
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
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller, parent, false);
        return new HolderProductSeller(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
        // get data
        final ModelProduct modelProduct = productList.get(position);
        String id = modelProduct.getPrID();
        String uid = modelProduct.getUid();
        String isDisc = modelProduct.getPrIsDisc();
        String discNote = modelProduct.getPrDiscNote();
        String discPrice = modelProduct.getPrDiscPrice();
        String cat = modelProduct.getPrCat();
        String loc = modelProduct.getPrLoc();
        String desc = modelProduct.getPrDesc();
        String icon = modelProduct.getPrIcon();
        String qrCode = modelProduct.getPrQRCode();
        String barCode = modelProduct.getPrBarCode();
        String quantity = modelProduct.getPrQuan();

        String title = modelProduct.getPrTitle();
        String timestamp = modelProduct.getTimestamp();
        String price = modelProduct.getPrPrice();
        String isReserve = modelProduct.getPrIsReserve();
        // set Data
        holder.titleTV.setText(title);
        holder.quanTV.setText(quantity + " m/dona");
        holder.locTV.setText("Joyi: " + loc);
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
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_white).into(holder.productIconIV);
        } catch (Exception e) {
            holder.productIconIV.setImageResource(R.drawable.ic_add_shopping_primary);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // handle item click, show item details (in bottom sheet)
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
        TextView addedAtTV = view.findViewById(R.id.addedAtTV);
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
        quanTV.setText(quantity + " m/dona");

        long prTimestamp = Long.parseLong(timestamp);
        Date prDate = new Date(prTimestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        // Format the Date object into a string
        String addedTime = dateFormat.format(prDate);
        addedAtTV.setText(addedTime);

        discNoteTV.setText(discNote);
        priceTV.setText("$ " + price);
        if (isReserve.equals("true")) {
            isReserveChB.setChecked(true);
        }
        discPriceTV.setText("$ " + discPrice);
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

        // show dialog
        bottomSheetDialog.show();

        // edit click
        editBtn.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            // open edit pr activity
            Intent intent = new Intent(context, EditProductActivity.class);
            intent.putExtra("prId", id);
            context.startActivity(intent);
        });

        // del click
        delBtn.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            // show del confirm dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete").setMessage(" Are you agree with it?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // delete
                            deleteProduct(id);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // cancel, dismiss dialog
                            dialog.dismiss();
                        }
                    }).show();
        });

        isReserveChB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isReserve.equals("false")) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("prIsReserve", "true");
                FirebaseAuth auth = FirebaseAuth.getInstance();
                DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Users");
                dataRef.child(auth.getUid()).child("Products").child(id).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "bron qilindi...", Toast.LENGTH_SHORT).show();
                        isReserveChB.setChecked(true);

                        bottomSheetDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }


            if (isReserve.equals("true")) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("prIsReserve", "false");
                FirebaseAuth auth = FirebaseAuth.getInstance();
                DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Users");
                dataRef.child(auth.getUid()).child("Products").child(id).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    private void deleteProduct(String id) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // pr deleted
                        Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed deleting pr
                        Toast.makeText(context, "del pr: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

}
