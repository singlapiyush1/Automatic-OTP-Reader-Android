package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_USER_CONSENT = 200;
    OTP_Receiver smsBroadcastReceiver;
    EditText enterOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.forgot_password, null);
        final EditText newPassword = mView.findViewById(R.id.newPassword);
        final EditText confirmPassword = mView.findViewById(R.id.confirmPassword);
        Button resetPassword = mView.findViewById(R.id.setNewPassword);


        View mView1 = getLayoutInflater().inflate(R.layout.employee_id, null);
        EditText employeeID = mView.findViewById(R.id.employeeID);
        Button sendOTP = mView1.findViewById(R.id.sendOTP);
        TextView resendOTP = mView1.findViewById(R.id.resendOTP);


        View mView2 = getLayoutInflater().inflate(R.layout.verify_otp, null);
        Button verifyOTP = mView2.findViewById(R.id.verifyOTP);
        enterOTP = mView2.findViewById(R.id.enterOTP);

        mBuilder.setView(mView).setCancelable(false);
        final AlertDialog dialog = mBuilder.create();

        mBuilder.setView(mView1);
        final AlertDialog dialog1 = mBuilder.create();

        mBuilder.setView(mView2);
        final AlertDialog dialog2 = mBuilder.create();

        Button forgotPassword = findViewById(R.id.forgotPassword);

        forgotPassword.setOnClickListener(view -> {

            dialog1.show();

            sendOTP.setOnClickListener(view1 -> {
                dialog2.show();

                startSmsUserConsent();
            });

            verifyOTP.setOnClickListener(view1 -> {
                dialog.show();

                resetPassword.setOnClickListener(view11 -> {

                    if ((!newPassword.getText().toString().isEmpty() && !confirmPassword.getText().toString().isEmpty()) && newPassword.getText().toString().equals(confirmPassword.getText().toString())) {
                        Toast.makeText(MainActivity.this,
                                R.string.success_login_msg,
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        dialog1.dismiss();
                        dialog2.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this,
                                R.string.error_login_msg,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private void startSmsUserConsent() {
        SmsRetrieverClient client = SmsRetriever.getClient(this);
        //We can add sender phone number or leave it blank
        // I'm adding null here
        client.startSmsUserConsent(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_USER_CONSENT) {
            if ((resultCode == RESULT_OK) && (data != null)) {
                //That gives all message to us.
                // We need to get the code from inside with regex
                String message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                getOtpFromMessage(message);
            }
        }
    }


    private void getOtpFromMessage(String message) {
        // This will match any 6 digit number in the message
        Pattern pattern = Pattern.compile("(|^)\\d{6}");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            enterOTP.setText(matcher.group(0));
        }
    }

    private void registerBroadcastReceiver() {
        smsBroadcastReceiver = new OTP_Receiver();
        smsBroadcastReceiver.smsBroadcastReceiverListener = new OTP_Receiver.SmsBroadcastReceiverListener(){
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, REQ_USER_CONSENT);
                    }

                    @Override
                    public void onFailure() {

                    }
                };
        IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        registerReceiver(smsBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(smsBroadcastReceiver);
    }
}