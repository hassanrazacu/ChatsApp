package com.aqsatech.chatsapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aqsatech.chatsapp.databinding.ActivityOTPBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;

import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class OTPActivity extends AppCompatActivity {


    ActivityOTPBinding binding;
    FirebaseAuth mAuth;

    String verificationId;
    String phoneNumber;
    ProgressDialog dialog;

    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOTPBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setTitle("Please Wait...");
        dialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();

        getSupportActionBar().hide();

        phoneNumber = getIntent().getStringExtra("phoneNumber");

        binding.phonelbl.setText("Verify " + phoneNumber);

        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                dialog.dismiss();
                Toast.makeText(OTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                verificationId=s;
                mResendToken=forceResendingToken;

                dialog.dismiss();

                Toast.makeText(OTPActivity.this, "verification code sent.", Toast.LENGTH_SHORT).show();
            }
        };

        if(TextUtils.isEmpty(phoneNumber))
        {
            Toast.makeText(this, "Please Enter the Phone Number", Toast.LENGTH_SHORT).show();
        }
        else {
            startPhoneNumberVerification(phoneNumber);
        }
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String code=binding.codeET.getText().toString().trim();
                if(TextUtils.isEmpty(code))
                {
                    Toast.makeText(OTPActivity.this, "Please Enter the OTP code.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    verifyPhoneNumberWithCode(verificationId,code);
                }
            }

        });
        binding.resendOtpTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode(phoneNumber,mResendToken);
            }
        });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        dialog.setMessage("Verifying Phone Number");
        dialog.show();
        PhoneAuthOptions options=PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L,TimeUnit.SECONDS)
                .setCallbacks(mCallbacks)
                .setActivity(this)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationId,code);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken mResendToken) {
        dialog.setMessage("Resending Code");
        dialog.show();
        PhoneAuthOptions options=PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L,TimeUnit.SECONDS)
                .setActivity(this)
                .setForceResendingToken(mResendToken)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        dialog.setMessage("Logging In");
        dialog.show();

        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    dialog.dismiss();
                    Toast.makeText(OTPActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(OTPActivity.this,ProfileActivity.class);
                    startActivity(intent);
                    finishAffinity();
                }
                else
                {
                    dialog.dismiss();
                    Toast.makeText(OTPActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}