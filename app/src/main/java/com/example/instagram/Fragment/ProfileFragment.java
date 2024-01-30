package com.example.instagram.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.Adapter.PhoToAdapter;
import com.example.instagram.EditProfileActivity;
import com.example.instagram.FollowersActivity;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.User;
import com.example.instagram.OptionsActivity;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {
    ImageView image_profile , options;
    TextView posts, followers, following, fullname, bio , username;
    Button edit_profile;

    LinearLayout lvFollowing, lvFollower;

    RecyclerView recyclerView;
    PhoToAdapter phoToAdapter;
    List<Post> postList;

    private List<String> mySaves;
    RecyclerView recyclerView_saves;
    PhoToAdapter phoToAdapter_saves;
    List<Post> postList_saves;

    FirebaseUser firebaseUser;
    String profileid;

    ImageButton my_photo, saved_photo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile,container,false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //có liên quan đến dòng 166 bên PostAdapter
        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid =  prefs.getString("profileid", "none");

        image_profile = view.findViewById(R.id.image_profile);
        options = view.findViewById(R.id.options);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        lvFollower = view.findViewById(R.id.lvFollower);
        following = view.findViewById(R.id.following);
        lvFollowing = view.findViewById(R.id.lvFollowing);
        fullname = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);
        edit_profile = view.findViewById(R.id.edit_profile);
        my_photo = view.findViewById(R.id.my_photo);
        saved_photo = view.findViewById(R.id.saved_photo);
        //profile
        recyclerView = view.findViewById(R.id.recycle_view_pitcher);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        phoToAdapter = new PhoToAdapter(getContext(),postList);
        recyclerView.setAdapter(phoToAdapter);
        //save
        recyclerView_saves = view.findViewById(R.id.recycle_view_save);
        recyclerView_saves.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager_saves = new GridLayoutManager(getContext(), 3);
        recyclerView_saves.setLayoutManager(linearLayoutManager_saves);
        postList_saves = new ArrayList<>();
        phoToAdapter_saves = new PhoToAdapter(getContext(),postList_saves);
        recyclerView_saves.setAdapter(phoToAdapter_saves);

        recyclerView.setVisibility(View.VISIBLE);
        recyclerView_saves.setVisibility(View.GONE);

        userInfo();
        getFollower();
        getFollowing();
        getPosts();
        MyPhoto();
        mysaves();

        if (profileid.equals(firebaseUser.getUid())){
            edit_profile.setText("Chỉnh sửa trang cá nhân");
        }else {
            checkFollow();
            saved_photo.setVisibility(View.GONE);
        }

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String btn = edit_profile.getText().toString();
                if (btn.equals("Chỉnh sửa trang cá nhân")){
                   startActivity(new Intent(getContext(), EditProfileActivity.class));
                }else if (btn.equals("Theo dõi")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("Following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("Follower").child(firebaseUser.getUid()).setValue(true);

                }else if (btn.equals("Đang theo dõi")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("Following").child(profileid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("Follower").child(firebaseUser.getUid()).removeValue();
                }
            }
        });


        //hình ảnh
        my_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView_saves.setVisibility(View.GONE);
            }
        });
        //lưu hình ảnh
        saved_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.GONE);
                recyclerView_saves.setVisibility(View.VISIBLE);
            }
        });
        /*
         * Load followers , following từ FollowersActivity*/
        //followers
        lvFollower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //truyền dữ liệu qua FollowersActivity
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id",profileid);
                intent.putExtra("title", "Theo dõi"); //truyền title
                startActivity(intent);
            }
        });
        //following
        lvFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //truyền dữ liệu qua FollowersActivity
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id",profileid);
                intent.putExtra("title", "Đang theo dõi");//truyền title
                startActivity(intent);
            }
        });
        //logout từ OptionsActivity
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), OptionsActivity.class);
                startActivity(intent);
            }
        });


        return view;
    }

    private void checkFollow(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(firebaseUser.getUid()).child("Following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if (datasnapshot.child(profileid).exists()){
                    edit_profile.setText("Đang theo dõi");
                }else {
                    edit_profile.setText("Theo dõi");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void MyPhoto(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profileid)){
                        postList.add(post);
                    }
                }
                Collections.reverse(postList);
                phoToAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                int counter = 0;
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profileid))
                        counter++;
                }
                posts.setText(String.valueOf(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getFollower(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(profileid).child("Follower");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                followers.setText(""+datasnapshot.getChildrenCount()); // tăng/giảm số lượng theo dõi
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void getFollowing(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(profileid).child("Following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                following.setText(""+datasnapshot.getChildrenCount());// tăng/giảm số lượng đang theo dõi
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void userInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if (getContext() == null){
                    return ;
                }
                User user = datasnapshot.getValue(User.class);
                /* Picasso là thư viện hổ trợ hình ảnh
                //hình ảnh user bên profile */
                Picasso.get().load(user.getImageurl()).placeholder(R.drawable.userlogo).into(image_profile);

                username.setText("Tên: "+user.getUsername());
                fullname.setText("Họ và tên: "+user.getFullname());
                bio.setText("Tiểu sử: "+user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void mysaves(){
        mySaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    mySaves.add(snapshot.getKey());
                }
                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void readSaves(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                postList_saves.clear();
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);

                    for (String id : mySaves){
                        if (post.getPostid().equals(id)){
                            postList_saves.add(post);
                        }
                    }
                }
                phoToAdapter_saves.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}