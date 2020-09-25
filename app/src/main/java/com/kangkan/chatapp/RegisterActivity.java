package com.kangkan.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText name,email,pass;
    Button buttonReg;

    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        Toolbar toolbar=findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reg");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name=findViewById(R.id.userName);
        email=findViewById(R.id.email);
        pass=findViewById(R.id.password);
        buttonReg=findViewById(R.id.btn_reg);

        auth=FirebaseAuth.getInstance();

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName=name.getText().toString();
                String userEmail=email.getText().toString();
                String userPass=pass.getText().toString();

                if (TextUtils.isEmpty(userName)||TextUtils.isEmpty(userEmail)||TextUtils.isEmpty(userPass))
                {
                    Toast.makeText(RegisterActivity.this, "FAKA", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    register(userName,userEmail,userPass);
                }

            }
        });
    }

    public void register(final String userName,  String email, String pass){
        auth.createUserWithEmailAndPassword(email,pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser firebaseUser=auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userID=firebaseUser.getUid();
                            reference= FirebaseDatabase.getInstance().getReference("Users").child(userID);

                            HashMap<String ,String> hashMap=new HashMap<>();
                            hashMap.put("id",userID);
                            hashMap.put("userName",userName);
                            hashMap.put("imgURL","Default");
                            hashMap.put("status","offline");
                            hashMap.put("search",userName.toLowerCase());

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                        }else {
                            Toast.makeText(RegisterActivity.this, "Invalid Email address", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
