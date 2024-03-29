package com.example.instagram;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.instagram.Adapter.CommentAdapter;
import com.example.instagram.Model.Comment;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CommentsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    EditText addcomment;
    ImageView image_profile;
    TextView post;

    String postid;
    String publisher;
    FirebaseUser firebaseUser;

    private static final String TAG = "Comments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bình luận");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");
        Log.d(TAG, "onCreate: postid"+postid);

        recyclerView = findViewById(R.id.recycle_view_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this,commentList,postid);
        recyclerView.setAdapter(commentAdapter);
        addcomment = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.image_profile);
        post = findViewById(R.id.post);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.child(postid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                publisher = ""+dataSnapshot.child("publisher").getValue();
                Log.d(TAG, "onDataChange: publisher: "+publisher);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addcomment.getText().toString().equals("")) {
                    Toast.makeText(CommentsActivity.this, "Bạn không thể để trống bình luận!", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
            }
        });
        getUserImage();
        getComment();

    }

    // tạo comment
    private void addComment(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);
        String id = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", addcomment.getText().toString());
        hashMap.put("publisher", firebaseUser.getUid());
        hashMap.put("id",id);
        reference.child(id).setValue(hashMap);
        addNotification(postid,publisher);
        addcomment.setText("");
        Toast.makeText(CommentsActivity.this, "Bình luận thành công", Toast.LENGTH_SHORT).show();

    }
    //tạo thông báo
    private void addNotification(String postid, String publisher) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications");
        String idNotification = reference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("idNotification",idNotification);
        hashMap.put("postUserid",publisher);
        hashMap.put("userid", firebaseUser.getUid());// uid người dùng khác bình luận ảnh bạn
        hashMap.put("text", "đã bình luận về bài viết");// uid người đăng ảnh có nghĩa là chủ bức ảnh đăng lên
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);

        reference.child(publisher).child(idNotification).setValue(hashMap);
    }
    //tạo ảnh user comment
    private void getUserImage(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                User user = datasnapshot.getValue(User.class);
                if (user.getImageurl().equals("default")){
                    //load hình ảnh trong comment chỗ đăng
                    Picasso.get().load(user.getImageurl()).placeholder(R.drawable.userlogo).into(image_profile);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getComment(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                commentList.clear();
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
