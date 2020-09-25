package com.kangkan.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kangkan.chatapp.Fragments.ChatsFragment;
import com.kangkan.chatapp.Fragments.ProfileFragment;
import com.kangkan.chatapp.Fragments.UsersFragment;
import com.kangkan.chatapp.Model.Chat;
import com.kangkan.chatapp.Model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView imageView;
    TextView textView;

    FirebaseUser firebaseUser;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar=findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        imageView=findViewById(R.id.profile_image);
        textView=findViewById(R.id.UserName);


        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user= snapshot  .getValue(User.class);
                textView.setText(user.getUserName());

                if (user.getImgURL().equals("Default"))
                {
                    imageView.setImageResource(R.drawable.ic_baseline_accessibility_new_24);
                } else {

                    Glide.with(getApplicationContext()).load(user.getImgURL()).into(imageView);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final TabLayout tabLayout=findViewById(R.id.tab_layout);
        final ViewPager viewPager =findViewById(R.id.view_pager);

        reference=FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                viewpagerAdapter viewpagerAdapter=new viewpagerAdapter(getSupportFragmentManager());
                int unread=0;

                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    Chat chat=snapshot1.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()){
                        unread++;
                    }
                }
                if (unread==0){
                    viewpagerAdapter.addFragment(new ChatsFragment(),"Chats");
                }else {
                    viewpagerAdapter.addFragment(new ChatsFragment(),"("+unread+")"+"Chats");
                }
                //viewpagerAdapter.addFragment(new ChatsFragment(),"Chats");
                viewpagerAdapter.addFragment(new UsersFragment(),"Users");
                viewpagerAdapter.addFragment(new ProfileFragment(),"Profile");

                viewPager.setAdapter(viewpagerAdapter);

                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void CurrentUser(String userid){
        SharedPreferences.Editor editor=getSharedPreferences("PREPS",MODE_PRIVATE).edit();
        editor.putString("currentUser",userid);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.logOut:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this,StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return  true;
        }

        return false;
    }


    class viewpagerAdapter extends FragmentPagerAdapter{


        private ArrayList<Fragment>fragments;
        private ArrayList<String>titles;

        viewpagerAdapter(FragmentManager fm){
            super(fm);
            this.fragments=new ArrayList<>();
            this.titles=new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment,String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void status(String status){
        reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object>map=new HashMap<>();
        map.put("status",status);
        reference.updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        //CurrentUser();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //reference.removeEventListener();
        status("offline");
        //CurrentUser("none");
    }
}