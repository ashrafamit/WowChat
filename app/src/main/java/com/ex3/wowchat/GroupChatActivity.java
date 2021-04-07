package com.ex3.wowchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText usermessageInput;
    private ScrollView mScrollview;
    private TextView displaytextMessage;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef,groupNameRef,groupMessageKeyRef;

    private String currentGroupName,currentUserId,currentUserName,currentDate,currentTime;
    private help h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentGroupName=getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this,currentGroupName,Toast.LENGTH_SHORT).show();

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("User");
        groupNameRef=FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupName);



        InitializeFields();

        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();

                usermessageInput.setText("");

                mScrollview.fullScroll(ScrollView.FOCUS_DOWN);
               // mScrollview.removeAllViews();

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

       groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    DisplayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    DisplayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void InitializeFields() {
        mToolbar=(Toolbar)findViewById(R.id.group_chat_bar_layoyt);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);


        sendMessageButton=(ImageButton)findViewById(R.id.send_message_button);
        usermessageInput=(EditText)findViewById(R.id.input_group_message);
        displaytextMessage=(TextView)findViewById(R.id.group_chat_text_display);
        mScrollview=(ScrollView)findViewById(R.id.my_scroll_view);
    }

    private void getUserInfo() {
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentUserName=dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void SaveMessageInfoToDatabase() {
        String message=usermessageInput.getText().toString();
        String messageKey=groupNameRef.push().getKey();

        if (TextUtils.isEmpty(message)){
            Toast.makeText(GroupChatActivity.this,"Please Write Message First...",Toast.LENGTH_SHORT).show();
        }else{
            Calendar calfordate=Calendar.getInstance();
            SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM dd,yyyy");
            currentDate=currentDateFormat.format(calfordate.getTime());

            Calendar calforTime=Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
            currentTime=currentTimeFormat.format(calforTime.getTime());


            HashMap<String,Object> groupMessageKey= new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef=groupNameRef.child(messageKey);

            HashMap<String,Object> messageInfoMap= new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);

            groupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }


    private void DisplayMessage(DataSnapshot dataSnapshot) {
       /* for (DataSnapshot ds: dataSnapshot.getChildren()){
            h=ds.getValue(help.class);
            displaytextMessage.append(h.getName()+":\n"+h.getMessage()+"\n"+h.getTime()+"     "+h.getDate()+"\n\n\n");
        }*/

        Iterator iterator=dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String Chatdate=(String)((DataSnapshot)iterator.next()).getValue();
            String Chatmessage=(String)((DataSnapshot)iterator.next()).getValue();
            String Chatname=(String)((DataSnapshot)iterator.next()).getValue();
            String Chattime=(String)((DataSnapshot)iterator.next()).getValue();

            displaytextMessage.append(Chatname+":\n"+Chatmessage+"\n"+Chattime+"      "+Chatdate+"\n\n\n");
            mScrollview.fullScroll(ScrollView.FOCUS_DOWN);
        }

    }

}
