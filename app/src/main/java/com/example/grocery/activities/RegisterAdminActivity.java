package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RegisterAdminActivity extends AppCompatActivity implements LocationListener {

    private ImageButton backBtn, gpsBtn;
    private CircularImageView profileCIV;
    private EditText usernameET, shopNameET, emailET, passET, confPassET, phoneET, deliveryET,
            countryET, stateET, cityET, addressET;
    private Button regAdminBtn;

    // permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int CAMERA_REQUEST_CODE = 300;
    // image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    // permission arrays
    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;
    // image picked uri
    private Uri image_uri;

    private double longitude = 0.0, latitude = 0.0;

    private LocationManager locationManager;

    // Firebase
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_admin);

        // init views
        init();

        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

//        gpsBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // detect current location
//                if (checkLocationPermission()){
//                    // already allowed
////                    detectLocation();
//                } else {
//                    // not allowed, request
//                    requestLocationPermissions();
//                }
//            }
//        });

//        profileCIV.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // pick image
////                showImagePickDialog();
//            }
//        });

        regAdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // register user
                inputData();
            }
        });
    }

    private void init(){
        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileCIV = findViewById(R.id.profileCIV);
        regAdminBtn = findViewById(R.id.regAdminBtn);

        usernameET = findViewById(R.id.usernameET);
        shopNameET = findViewById(R.id.shopNameET);
        emailET = findViewById(R.id.emailET);
        passET = findViewById(R.id.passET);
        confPassET = findViewById(R.id.confPassET);
        phoneET = findViewById(R.id.phoneET);
        deliveryET = findViewById(R.id.deliveryET);
        countryET = findViewById(R.id.countryET);
        stateET = findViewById(R.id.stateET);
        cityET = findViewById(R.id.cityET);
        addressET = findViewById(R.id.addressET);

        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        // init permissions array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private String username, shopName, email, password, confPassword, phoneNumber, deliveryFee, country, state, city, address;
    private void inputData() {
        // input data
        username = usernameET.getText().toString().trim();
        shopName = shopNameET.getText().toString().trim();
        email = emailET.getText().toString().trim();
        password = passET.getText().toString().trim();
        confPassword = confPassET.getText().toString().trim();
        phoneNumber = phoneET.getText().toString().trim();
        deliveryFee = deliveryET.getText().toString().trim();
        country = countryET.getText().toString().trim();
        state = stateET.getText().toString().trim();
        city = cityET.getText().toString().trim();
        address = addressET.getText().toString().trim();

        // validate data
        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "Enter Name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(shopName)){
            Toast.makeText(this, "Enter Shop Name...", Toast.LENGTH_SHORT).show();
            return;
        }
//        if (TextUtils.isEmpty(phoneNumber)){
//            Toast.makeText(this, "Enter Phone Number...", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (TextUtils.isEmpty(deliveryFee)){
//            Toast.makeText(this, "Enter Delivery Fee...", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (latitude == 0.0 || longitude == 0.0){
//            Toast.makeText(this, "Click GPS Button...", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length()<6){
            Toast.makeText(this, "Password must be at-least 6 char...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confPassword)){
            Toast.makeText(this, "Passwords doesn't match...", Toast.LENGTH_SHORT).show();
        }
        createAccount();
    }

    private void createAccount() {
        progressDialog.setMessage("Creating Acc...");
        progressDialog.show();

        // create Acc
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // acc created
                saveFirebaseData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterAdminActivity.this,
                        "create Admin Acc: " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveFirebaseData() {
        progressDialog.setMessage("Saving Acc info...");

        String timestamp = "" + System.currentTimeMillis();
        if (image_uri==null){
            // save info without image

            // setup data to save
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", ""+auth.getUid());
            hashMap.put("username", ""+username);
            hashMap.put("email", ""+email);
            hashMap.put("password", ""+password);
//            hashMap.put("shopName", ""+shopName);
//            hashMap.put("phone", ""+phoneNumber);
//            hashMap.put("delivery", ""+deliveryFee);
//            hashMap.put("country", ""+country);
//            hashMap.put("state", ""+state);
//            hashMap.put("city", ""+city);
//            hashMap.put("address", ""+address);
//            hashMap.put("longitude", ""+longitude);
//            hashMap.put("latitude", ""+latitude);
            hashMap.put("timestamp", ""+timestamp);
            hashMap.put("accountType", ""+"Seller");
//            hashMap.put("online", ""+"true");
//            hashMap.put("shopOpen", ""+"true");
//            hashMap.put("profileImg", "");

            // save to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(auth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progressDialog.dismiss();
                    startActivity(new Intent(RegisterAdminActivity.this, MainAdminActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RegisterAdminActivity.this,
                            "user saving: " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // save info with image

            // name and path of image
            String filePathAndName = "profile_images/" + ""+auth.getUid();
            // upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // get url of uploaded image
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    Uri downloadImageUri = uriTask.getResult();

                    if (uriTask.isSuccessful()){
                        // setup data to save
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", ""+auth.getUid());
                        hashMap.put("username", ""+username);
                        hashMap.put("email", ""+email);
                        hashMap.put("password", ""+password);
                        hashMap.put("shopName", ""+shopName);
                        hashMap.put("phone", ""+phoneNumber);
                        hashMap.put("delivery", ""+deliveryFee);
                        hashMap.put("country", ""+country);
                        hashMap.put("state", ""+state);
                        hashMap.put("city", ""+city);
                        hashMap.put("address", ""+address);
                        hashMap.put("longitude", ""+longitude);
                        hashMap.put("latitude", ""+latitude);
                        hashMap.put("timestamp", ""+timestamp);
                        hashMap.put("accountType", ""+"Seller");
                        hashMap.put("online", ""+"true");
                        hashMap.put("shopOpen", ""+"true");
                        hashMap.put("profileImg", ""+downloadImageUri); // uri of uploaded img

                        // save to db
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                        reference.child(auth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                startActivity(new Intent(RegisterAdminActivity.this, MainAdminActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterAdminActivity.this,
                                        "user saving with profImg: " + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RegisterAdminActivity.this,
                            "upload profile: " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    //    //  Location Settings Begin //
    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermissions(){
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        // location allowed
//                        detectLocation();
                    } else {
                        // location denied
                        Toast.makeText(this, "Location permission is needed...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
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

    // detect location
//    private void detectLocation() {
//        Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//    }

    // Override methods for implements
    @Override
    public void onLocationChanged(@NonNull Location location) {
        // location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        findAddress();
    }

    private void findAddress(){
        // find address, country, state, city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0); // complete address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            // set addresses
            countryET.setText(country);
            stateET.setText(state);
            cityET.setText(city);
            addressET.setText(address);
        } catch (Exception e){
            Toast.makeText(this, "findAddress() error: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // gps location disabled
        Toast.makeText(this, "Please turn on the location...", Toast.LENGTH_SHORT).show();
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }
//   //  Location Settings End //


//   //  Camera & Storage Settings Begin //
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
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

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

//   //  Camera & Storage Settings End //

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                // get picked image
                image_uri = data.getData();
                // set to imageView
                profileCIV.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                // set to imageView
                profileCIV.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}