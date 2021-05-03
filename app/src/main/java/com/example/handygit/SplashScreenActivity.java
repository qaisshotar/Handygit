package com.example.handygit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.handygit.Common.Common;
import com.example.handygit.Model.UserModel;
import com.example.handygit.Utils.UserUtils;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SplashScreenActivity extends AppCompatActivity {

    private  final static int LOGIN_REQUEST_CODE=7171;
    private List<AuthUI.IdpConfig>provider;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    @BindView(R.id.progress_bar)
    ProgressBar progress_bar;

    FirebaseDatabase database;
    DatabaseReference userInfoRef;

    @Override
    protected void onStart() {
        super.onStart();
        delaysplaSplashScreen();
    }

    private void delaysplaSplashScreen() {

        progress_bar.setVisibility(View.VISIBLE);

        Completable.timer(3, TimeUnit.SECONDS,
                AndroidSchedulers.mainThread())
                .subscribe(() ->

                        firebaseAuth.addAuthStateListener(listener)
                );

    }

    @Override
    protected void onStop() {
        
        if(firebaseAuth !=null && listener !=null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        init();
    }

    private void init() {
        ButterKnife.bind(this);

        database = FirebaseDatabase.getInstance();
        userInfoRef=database.getReference(Common.USER_INFO_REFERENCE);

        provider= Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build()
        );
        firebaseAuth= FirebaseAuth.getInstance();
        listener = myFirebaseAuth ->{

            FirebaseUser user = myFirebaseAuth.getCurrentUser();
            if(user!=null)
            {
                FirebaseInstanceId.getInstance()
                        .getInstanceId()
                        .addOnFailureListener(e -> Toast.makeText(SplashScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                        .addOnSuccessListener(instanceIdResult -> {
                            Log.d("TOKEN",instanceIdResult.getToken());
                            UserUtils.updateToken(SplashScreenActivity.this,instanceIdResult.getToken());
                        });
                checkUserFromFirebase();
            }
            else
                showLoginLayout();
        };

    }

    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout=new AuthMethodPickerLayout
                .Builder(R.layout.layout_sign_in)
                .setPhoneButtonId(R.id.btn_phone_sign_in)
                .build();

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setIsSmartLockEnabled(false)

                .setAvailableProviders(provider)
                .build(),LOGIN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOGIN_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode==RESULT_OK)
            {
                FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();

            }

            else
            {
                Toast.makeText(this, "[ERORR]:"+response.getError().getMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    }



    private void checkUserFromFirebase() {
        userInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            UserModel userModel =dataSnapshot.getValue(UserModel.class);
                            goToHomeActivity(userModel);

                        }
                        else {

                        showRegisterLayout();

                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void goToHomeActivity(UserModel userModel) {
        Common.currentUser = userModel;
        startActivity(new Intent(this,HomeActivity.class));
        finish();
    }


    private void showRegisterLayout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        View itemView = getLayoutInflater().from(this).inflate(R.layout.layout_register, null);
        TextInputEditText edt_first_name = (TextInputEditText) itemView.findViewById(R.id.edt_first_name);
        TextInputEditText edt_last_name = (TextInputEditText) itemView.findViewById(R.id.edt_last_name);
        TextInputEditText edt_phone = (TextInputEditText) itemView.findViewById(R.id.edt_phone_number);


        Button btn_continue = (Button) itemView.findViewById(R.id.btn_register);

        if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null &&
                TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
            edt_phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();

        btn_continue.setOnClickListener(v -> {
            if (TextUtils.isEmpty(edt_first_name.getText().toString())) {

                Toast.makeText(this, "please enter first name", Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (TextUtils.isEmpty(edt_last_name.getText().toString())) {

                    Toast.makeText(this, "please enter last name", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (TextUtils.isEmpty(edt_phone.getText().toString())) {

                        Toast.makeText(this, "please enter phone number", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    } else {

                        UserModel model = new UserModel();
                        model.setFirstName(edt_first_name.getText().toString());
                        model.setLastName(edt_last_name.getText().toString());
                        model.setPhoneNumber(edt_phone.getText().toString());

                        userInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(model)
                                .addOnFailureListener(e ->
                                        {
                                            dialog.dismiss();
                                            Toast.makeText(SplashScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Register Succesfully", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    goToHomeActivity(model);

                                });
                    }


                }

            }

        });
    }}