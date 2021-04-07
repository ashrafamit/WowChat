package com.ex3.wowchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userProfileStatus;
    private CircleImageView userProfileImage;

    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference rootRaf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        rootRaf=FirebaseDatabase.getInstance().getReference();
        InitializeFields();

        userName.setVisibility(View.INVISIBLE);

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetriveUserInfo();
    }

    private void InitializeFields() {
        updateAccountSettings=(Button)findViewById(R.id.update_settings_button);
        userName=(EditText)findViewById(R.id.set_user_name);
        userProfileStatus=(EditText)findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView)findViewById(R.id.set_profile_image);
    }


    private void UpdateSettings() {
        String setUserName=userName.getText().toString();
        String setStatus=userProfileStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)){
            Toast.makeText(SettingsActivity.this,"Please write your name first...",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus)){
            Toast.makeText(SettingsActivity.this,"Please write your status...",Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String,String>profileMap=new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
            rootRaf.child("User").child(currentUserId).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this,"Profile Updated Successfully...",Toast.LENGTH_SHORT).show();
                    }else
                    {
                        String message= task.getException().toString();
                        Toast.makeText(SettingsActivity.this,"Error : "+message,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void RetriveUserInfo() {
        rootRaf.child("User").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){
                    String retriveUserName=dataSnapshot.child("name").getValue().toString();
                    String retriveStatus=dataSnapshot.child("status").getValue().toString();
                    String retriveProfileImage=dataSnapshot.child("image").getValue().toString();

                    userName.setText(retriveUserName);
                    userProfileStatus.setText(retriveStatus);
                }else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                    String retriveUserName=dataSnapshot.child("name").getValue().toString();
                    String retriveStatus=dataSnapshot.child("status").getValue().toString();

                    userName.setText(retriveUserName);
                    userProfileStatus.setText(retriveStatus);
                }
                else {
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this,"Please set and update your profile information...",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent= new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
