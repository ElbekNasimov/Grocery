package com.example.grocery.crud;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.grocery.constants.CaptureAct;
import com.example.grocery.constants.Constants;
import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {

    // UI view
    private ImageButton backBtn;
    private CircularImageView prodIconIV;
    private EditText titleET, descET, categoryET, locationET, quantityET, priceET, discPriceET, discNoteET;
    private TextView qrcodeTV, barcodeTV;
    private SwitchCompat discountSC;
    private Button addPrdBtn;
    private ProgressDialog progressDialog;
    // permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    // image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    // permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;
    // image picked uri
    private Uri image_uri;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // init
        init();

        // in start, discount was unchecked
        discPriceET.setVisibility(View.GONE);
        discNoteET.setVisibility(View.GONE);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // pick product image
        prodIconIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        // pick product category
        categoryET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });

        // pick product location
        locationET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationDialog();
            }
        });

        barcodeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AddProductActivity.this, "ScanCode", Toast.LENGTH_SHORT).show();
                scanCode();
            }
        });

        discountSC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // if discountSwitch is checked, show discount section
                    discPriceET.setVisibility(View.VISIBLE);
                    discNoteET.setVisibility(View.VISIBLE);
                } else {
                    // if not checked hide section
                    discPriceET.setVisibility(View.GONE);
                    discNoteET.setVisibility(View.GONE);
                }
            }
        });

        addPrdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Flow
                1) input data;
                2) validate data;
                3) add data to db
                 */
                inputData();
            }
        });
    }

    private void init(){
        backBtn = findViewById(R.id.backBtn);
        prodIconIV = findViewById(R.id.prodIconIV);
        titleET = findViewById(R.id.titleET);
        descET = findViewById(R.id.descET);
        categoryET = findViewById(R.id.categoryET);
        locationET = findViewById(R.id.locationET);
        quantityET = findViewById(R.id.quantityET);
        priceET = findViewById(R.id.priceET);
        discPriceET = findViewById(R.id.discPriceET);
        barcodeTV = findViewById(R.id.barcodeTV);
        qrcodeTV = findViewById(R.id.qrcodeTV);
        discNoteET = findViewById(R.id.discNoteET);
        discountSC = findViewById(R.id.discountSC);
        addPrdBtn = findViewById(R.id.addPrdBtn);
        progressDialog = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();

        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        // init permissions array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    // pick category begin //
    private void categoryDialog(){
        // dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category").setItems(Constants.categories, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // get picked category
                String category = Constants.categories[which];

                // set picked category
                categoryET.setText(category);
            }
        }).show();
    }
    // pick category end //

    // pick location begin //
    private void locationDialog(){
        // dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Location").setItems(Constants.location, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // get picked location
                String location = Constants.location[which];

                // set picked location
                locationET.setText(location);
            }
        }).show();
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            barcodeTV.setText(result.getContents());
            AlertDialog.Builder builder = new AlertDialog.Builder(AddProductActivity.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    });
    // pick location end //

// input data begin //
    private String title, desc, category, location, quantity, price, barCode, discPrice, discNote;
    private boolean isDisc=false;
    private boolean isReserve=false; // to reserve products

    private void inputData() {
        // 1) input data
        title = titleET.getText().toString().trim();
        desc = descET.getText().toString().trim();
        category = categoryET.getText().toString().trim();
        location = locationET.getText().toString().trim();
        quantity = quantityET.getText().toString().trim();
        price = priceET.getText().toString().trim();
        barCode = barcodeTV.getText().toString().trim();
        isDisc = discountSC.isChecked(); // true/false

        // 2) validate data
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Mahsulot nomi kiriting...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(category)){
            Toast.makeText(this, "Kategoriyani tanlang...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(location)){
            Toast.makeText(this, "Joyni tanlang...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(quantity)){
            Toast.makeText(this, "Miqdorini kiriting...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (desc.isEmpty()){
            desc="";
        }
        if (price.isEmpty()){
            price="0";
        }
        if (isDisc){
            // product is with disc
            discPrice = discPriceET.getText().toString().trim();
            discNote = discNoteET.getText().toString().trim();
            if (TextUtils.isEmpty(discPrice)){
                Toast.makeText(this, "Chegirmani kiriting...", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // product is with disc
            discPrice = "0";
            discNote = "";
        }

        // 3) product adding
        productAdd();
    }
    private void productAdd() {
        progressDialog.setMessage("Adding Product...");
        progressDialog.show();

        String timestamps = "" + System.currentTimeMillis();
        if (image_uri == null) {
            // upload without image
            // setup data to upload
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("prID", "" + timestamps);
            hashMap.put("prTitle", "" + title);
            hashMap.put("prDesc", "" + desc);
            hashMap.put("prCat", "" + category);
            hashMap.put("prLoc", "" + location);
            hashMap.put("prQuan", "" + quantity);
            hashMap.put("prIcon", "");                                  // no image, set empty
            hashMap.put("qrCode", "");                                  // qrCode
            hashMap.put("barCode", barCode);                                  // barCode
            hashMap.put("prPrice", "" + price);
            hashMap.put("prIsReserve", "" + isReserve);
            hashMap.put("prDiscPrice", "" + discPrice);
            hashMap.put("prDiscNote", "" + discNote);
            hashMap.put("prIsDisc", "" + isDisc);
            hashMap.put("timestamp", "" + timestamps);
            hashMap.put("uid", "" + auth.getUid());

            // add to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(auth.getUid()).child("Products").child(timestamps).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "Qo'shildi", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "add pr: " + e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // upload with image
            // upload image to storage

            // name and path of image to be uploaded
            String filePathAndName = "product_images/" + "" + timestamps;


            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // image uploaded
                    // get uri of uploaded image
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                    while (!task.isSuccessful());
                    Uri downloadImageUri = task.getResult();

                    if (task.isSuccessful()){
                        // uri of image received, uploaded to db
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("prID", "" + timestamps);
                        hashMap.put("prTitle", "" + title);
                        hashMap.put("prDesc", "" + desc);
                        hashMap.put("prCat", "" + category);
                        hashMap.put("prLoc", "" + location);
                        hashMap.put("prQuan", "" + quantity);
                        hashMap.put("prIcon", "" + downloadImageUri);
                        hashMap.put("qrCode", "");                                  // qrCode
                        hashMap.put("barCode", barCode);                                 // barCode
                        hashMap.put("prPrice", "" + price);
                        hashMap.put("prIsReserve", "" + isReserve);                             // doing reserve (bron)
                        hashMap.put("prDiscPrice", "" + discPrice);
                        hashMap.put("prDiscNote", "" + discNote);
                        hashMap.put("prIsDisc", "" + isDisc);
                        hashMap.put("timestamp", "" + timestamps);
                        hashMap.put("uid", "" + auth.getUid());
                        // add to db
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                        reference.child(auth.getUid()).child("Products").child(timestamps).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddProductActivity.this, "Qo'shildi", Toast.LENGTH_SHORT).show();
                                        clearData();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddProductActivity.this, "add pr: " + e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // failed uploaded img
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, "pr Image uploaded" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void clearData() {
        //  after upload product, clear data from view
        quantityET.setText("");
        // if need add more, in youtube 5-part, at the 01:04:10
    }
//    // input data end //

//    //   //  Camera & Storage Settings Begin //
    private void showImagePickDialog(){
        // options to display in dialog
        String[] options = {"Camera", "Gallery"};
        // dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // handle clicks
                if (which == 0){
                    // camera clicked
                    if (checkCameraPermission()){
                        // camera permission allowed
                        pickFromCamera();
                    } else {
                        // not allowed, request
                        requestCameraPermission();
                    }
                } else {
                    // gallery clicked
                    if (checkStoragePermission()){
                        // storage permission allowed
                        pickFromGallery();
                    } else {
                        // not allowed, request
                        requestStoragePermission();
                    }
                }
            }
        }).show();
    }

    private void pickFromGallery(){
        // intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){

        // using media store to pick high/original quality image

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image_Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    // handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        // location allowed
                        pickFromCamera();
                    } else {
                        // location denied
                        Toast.makeText(this, "Camera permissions are needed...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        // location allowed
                        pickFromGallery();
                    } else {
                        // location denied
                        Toast.makeText(this, "Storage permission is needed...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

//    //   //  Camera & Storage Settings End //

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                // get picked image
                image_uri = data.getData();
                // set to imageView
                prodIconIV.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                // set to imageView
                prodIconIV.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}