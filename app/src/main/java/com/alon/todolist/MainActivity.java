package com.alon.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alon.todolist.utils.MySP;
import com.alon.todolist.utils.PermissionUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout main_LYT_signup;
    private EditText main_EDT_name, main_EDT_phone;
    private Button main_BTN_confirm;
    private ProgressBar main_PRB;
    private String name, phoneNumber, mVerificationCode, mVerificationId;
    private Handler handler = new Handler();
    private FirebaseFirestore db;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private Dialog authDialog;
    private Boolean permitManually = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        findAll();
        authDialog = new Dialog(this);
        createAuthDialog(authDialog);
        setCallbacks();
        setClickListeners();
        askForPermissions();
    }


    // Method that check all permissions.
    private void askForPermissions(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.ACTIVITY_RECOGNITION}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkForSP();
                } else{
                    PermissionUtils.setShouldShowStatus(this, Manifest.permission.ACCESS_FINE_LOCATION);
                    if(PermissionUtils.neverAskAgainSelected(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                        displayNeverAskAgainDialog();
                    } else {
                        askForPermissions();
                    }
                }
                break;
        }
    }

    // Method that displays never ask again dialog.
    private void displayNeverAskAgainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We need access to your location for performing necessary task. Please permit the permission through "
                + "Settings screen.\n\nSelect Permissions -> Enable permission");
        builder.setCancelable(false);
        builder.setPositiveButton("Permit Manually", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permitManually = true;
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                displayNeverAskAgainDialog();
            }
        });
        builder.show();
    }

    // Method that checks if shared preferences exist.
    private void checkForSP() {
        name = MySP.getInstance().getString("Name", null);
        phoneNumber = MySP.getInstance().getString("Phone", null);
        if(name != null && phoneNumber != null){
            // If exist -> start to do list activity.
            main_PRB.setVisibility(View.VISIBLE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startToDoListActivity();
                }
            }, 4000);
        } else {
            // If not exist -> show registration ui.
            main_LYT_signup.setVisibility(View.VISIBLE);
        }
    }


    // Method that creates auth code dialog.
    private void createAuthDialog(final Dialog dialog) {
        dialog.setContentView(R.layout.auth_dialog);
        dialog.setCancelable(true);

        final EditText auth_EDT_code;
        auth_EDT_code = findViewById(R.id.auth_EDT_code);
        Button auth_BTN_confirm, auth_BTN_cancel;
        auth_BTN_confirm = dialog.findViewById(R.id.auth_BTN_confirm);
        auth_BTN_cancel = dialog.findViewById(R.id.auth_BTN_cancel);
        auth_BTN_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVerificationCode = auth_EDT_code.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, mVerificationCode);
                signInWithPhoneAuthCredential(credential);
            }
        });

        auth_BTN_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    // Method that sets phone auth callbacks.
    private void setCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                mVerificationId = verificationId;
                authDialog.show();
            }
        };
    }

    // Method that signs in the user.
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("pttt", "signInWithCredential:success");
                            authDialog.dismiss();
                            MySP.getInstance().putString("Name", name);
                            MySP.getInstance().putString("Phone", phoneNumber);
                            saveDataToDB(phoneNumber, name);
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("pttt", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }



    // Method that starts the to do list activity.
    private void startToDoListActivity() {
        Intent intent = new Intent(this, ToDoListActivity.class);
        startActivity(intent);
        finish();
    }

    // Method that finds all views by id.
    private void findAll() {
        main_LYT_signup = findViewById(R.id.main_LYT_signup);
        main_EDT_name = findViewById(R.id.main_EDT_name);
        main_EDT_phone = findViewById(R.id.main_EDT_phone);
        main_BTN_confirm = findViewById(R.id.main_BTN_confirm);
        main_PRB = findViewById(R.id.main_PRB);
    }

    // Method that sets all click listeners.
    private void setClickListeners() {
        main_BTN_confirm.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.main_BTN_confirm:
                main_BTN_confirm.setClickable(false);
                if(checkInputValidation()){
                    main_PRB.setVisibility(View.VISIBLE);
                    name = main_EDT_name.getText().toString();
                    phoneNumber = main_EDT_phone.getText().toString();
                    String fixedPhoneNumber = "+972" + phoneNumber;
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(fixedPhoneNumber, 60, TimeUnit.SECONDS, this, mCallbacks);

                } else {
                    main_BTN_confirm.setClickable(true);
                }
                break;
        }
    }

    // Method that checks input validation.
    private Boolean checkInputValidation(){
        if(main_EDT_name.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_LONG).show();
            return false;
        } else if(main_EDT_phone.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    // Method that saves the data to db.
    private void saveDataToDB(String phone, String name){
        Map<String, Object> initData = new HashMap<>();
        initData.put("text", "");
        db.collection("locations").document(phone).set(initData);
        Map<String, Object> docData = new HashMap<>();
        docData.put("name", name);
        docData.put("phone", phone);
        db.collection("locations").
                document(phone).
                collection("data").
                document("details").
                set(docData).
                addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                main_PRB.setVisibility(View.GONE);
                startToDoListActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                main_BTN_confirm.setClickable(true);
                main_PRB.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(permitManually){
            askForPermissions();
            permitManually = false;
        }
    }
}