package com.kangkan.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Api;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kangkan.chatapp.Adapter.MessageAdapter;
import com.kangkan.chatapp.Fragments.APIService;
import com.kangkan.chatapp.Model.Chat;
import com.kangkan.chatapp.Model.User;
import com.kangkan.chatapp.Notifications.Client;
import com.kangkan.chatapp.Notifications.Data;
import com.kangkan.chatapp.Notifications.MyResponse;
import com.kangkan.chatapp.Notifications.Sender;
import com.kangkan.chatapp.Notifications.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView circleImageView;
    TextView textView;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

     Intent intent;

     ImageButton imageButton;
     EditText editText;

     MessageAdapter messageAdapter;
     List<Chat> mchat;

     RecyclerView recyclerView;

     ValueEventListener seenListener;

     APIService apiService;

     boolean  notify =false;

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar=findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessageActivity.this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        apiService= Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        circleImageView=findViewById(R.id.profile_image);
        textView=findViewById(R.id.UserName);
        imageButton=findViewById(R.id.btn_send);
        editText=findViewById(R.id.txt_send);

        recyclerView=findViewById(R.id.rv_chat);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        intent=getIntent();
         uid=intent.getStringExtra("UsersID");
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify=true;
                String msg=editText.getText().toString();

                if (!msg.equals("")){
                    sendMessage(firebaseUser.getUid(),uid,msg);
                }else {
                    Toast.makeText(MessageActivity.this, "You Can't empty Message", Toast.LENGTH_SHORT).show();
                }
                editText.setText("");
            }
        });


        databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(uid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user= snapshot  .getValue(User.class);
                textView.setText(user.getUserName());

                if (user.getImgURL().equals("Default"))
                {
                    circleImageView.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImgURL()).into(circleImageView);
                }
                readMessage(firebaseUser.getUid(),uid,user.getImgURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        seenMessage(uid);
    }

    private void seenMessage(final String userid){
       databaseReference=FirebaseDatabase.getInstance().getReference("Chats");
       seenListener=databaseReference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot datasnapshot) {
               for (DataSnapshot Snapshot : datasnapshot.getChildren()) {
                   Chat chat=Snapshot.getValue(Chat.class);

                   if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)) {
                       HashMap<String,Object>map=new HashMap<>();
                       map.put("isseen",true);
                       Snapshot.getRef().updateChildren(map);
                   }

               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    }

    private void sendMessage(String sender, final String receiver, String message){

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);
        databaseReference.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatRef=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(uid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatRef.child("id").setValue(uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final  String msg=message;
        databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                if (notify) {
                    sentNotification(receiver, user.getUserName(), msg);
                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sentNotification(String receiver, final String userName, final String msg) {
        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot Snapshot : snapshot.getChildren()) {
                    Token token = Snapshot.getValue(Token.class);
                    Data data=new Data(R.mipmap.ic_launcher,firebaseUser.getUid(),userName+":"+msg,"New meaasge",uid);

                    Sender sender=new Sender(data,token.getToken());
                    apiService.sentNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code()==200){
                                        if (response.body().success!=1){
                                            Toast.makeText(MessageActivity.this, "Faild", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage(final String myid, final String userid, final String imgurl){
        mchat=new ArrayList<>();
        databaseReference=FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                mchat.clear();
                for (DataSnapshot Snapshot : datasnapshot.getChildren()) {
                    Chat chat=Snapshot.getValue(Chat.class);

                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid))
                    {
                        mchat.add(chat);
                    }
                    messageAdapter=new MessageAdapter(MessageActivity.this,mchat,imgurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void status(String status){
        databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object>map=new HashMap<>();
        map.put("Status",status);
        databaseReference.updateChildren(map);

    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(seenListener);
        status("offline");
    }
}