package com.example.instagram.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.example.instagram.BaoCaoActivity;
import com.example.instagram.ChatActivity;
import com.example.instagram.CommentsActivity;
import com.example.instagram.FollowersActivity;
import com.example.instagram.Fragment.PostDetailFragment;
import com.example.instagram.Fragment.ProfileFragment;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.example.instagram.ShareImageActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    public Context mContext;
    public List<Post> mPost;


    private FirebaseUser firebaseUser;
    private static final String TAG ="Post";

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Post post = mPost.get(position);
        /*hiện username , publisher , decription(tên ảnh ) lên trang home */
        if (post.getDescription().equals("")) {
            viewHolder.description.setVisibility(View.GONE);
        } else {
            viewHolder.description.setVisibility(View.VISIBLE);
            viewHolder.description.setText(post.getDescription());
        }
        /*thư viện load ảnh Picasso ,load hình ảnh mục home */
        Picasso.get().load(post.getPostimage()).placeholder(R.mipmap.ic_launcher).into(viewHolder.post_image);
        viewHolder.description.setText(post.getDescription());
        FirebaseDatabase.getInstance().getReference().child("Users").child(post.getPublisher()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                User user = datasnapshot.getValue(User.class);
                //load hình ảnh user ở mục home
                Picasso.get().load(user.getImageurl()).placeholder(R.drawable.userlogo).into(viewHolder.image_profile);
                viewHolder.username.setText(user.getUsername());
                viewHolder.publisher.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Liked(post.getPostid(), viewHolder.like);
        notlikes(viewHolder.likes, post.getPostid());
        getComments(post.getPostid(), viewHolder.comments);
        isSaved(post.getPostid(), viewHolder.save);

        //save , like , comment ,share, publisher, username
        viewHolder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.save.getTag().equals("save")) {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).removeValue();
                }
            }
        });

        viewHolder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Like").child(post.getPostid())
                            .child(firebaseUser.getUid()).setValue(true);
                    //tạo thông báo
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notification");
                    reference.orderByChild("userid").equalTo(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("postid", post.getPostid()).apply();
                            mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("userid", firebaseUser.getUid()).apply();
                            addNotification(post.getPostid(), post.getPublisher());
                            Log.d(TAG, "onClick: publisher: "+post.getPublisher());
                            Log.d(TAG, "onClick: postid: "+post.getPostid());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else {
                    FirebaseDatabase.getInstance().getReference().child("Like").child(post.getPostid())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        viewHolder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisher", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        viewHolder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisher", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        viewHolder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: share");
                moreOptionsDialog(post, viewHolder);
            }
        });

        viewHolder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: image_profile");
                //lưu ý tên PREFS bên ProfileFragment
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("profileid", post.getPublisher()).apply();
                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            }
        });

        viewHolder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("profileid", post.getPublisher()).apply();
                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            }
        });

        viewHolder.publisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("profileid", post.getPublisher()).apply();
                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            }
        });

        viewHolder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("postid", post.getPostid()).apply();
                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PostDetailFragment()).commit();
            }
        });
        /* xem likes , likes này bên FollowersActivity */
        viewHolder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FollowersActivity.class);
                intent.putExtra("id", post.getPostid());
                intent.putExtra("title", "Lượt thích");
                mContext.startActivity(intent);
            }
        });
        /* edit post */
        viewHolder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.edit:
                                editPost(post.getPostid());
                                return true;
                            case R.id.delete:
                                FirebaseDatabase.getInstance().getReference("Posts")
                                        .child(post.getPostid()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(mContext, "Xoá", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                return true;
                            case R.id.report:
                                Intent intent = new Intent(mContext, BaoCaoActivity.class);
                                mContext.startActivity(intent);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                if (!post.getPublisher().equals(firebaseUser.getUid())){
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show();
            }
        });
        viewHolder.chats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("receiptUid", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

    }

    private void moreOptionsDialog(Post post, ViewHolder viewHolder) {
        Log.d(TAG, "moreOptionsDialog: ");
        String postid = post.getPostid();
        /*Những thứ ở đây chưa dùng đến
         * Và sẽ dùng trong những chức năng sau*/
        String postimage = post.getPostimage();
        String description = post.getDescription();
        String publisher = post.getPublisher();
        long timestamp = post.getTimestamp();

        Intent intent = new Intent(mContext, ShareImageActivity.class);
        intent.putExtra("postid", postid);
        intent.putExtra("postimage", postimage);
        intent.putExtra("description", description);
        intent.putExtra("publisher", publisher);
        mContext.startActivity(intent);


    }

    //Comment
    private void getComments(String postid, TextView comments) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                comments.setText("Xem tất cả "+datasnapshot.getChildrenCount()+ " bình luận");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //like
    private void Liked(String postid, ImageView imageView) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Like").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if (datasnapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("notlike");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //tạo thông báo
    private void addNotification(String postid, String publisher) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications");
        String idNotification = reference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("idNotification",idNotification);
        hashMap.put("postUserid",publisher); // uid người đăng ảnh có nghĩa là chủ bức ảnh đăng lên
        hashMap.put("userid", firebaseUser.getUid()); // uid người dùng khác thích ảnh
        hashMap.put("text", "thích ảnh của bạn");
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);


        reference.child(publisher).child(idNotification).setValue(hashMap);


    }

    //notlike
    private void notlikes(TextView likes, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Like").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                likes.setText(datasnapshot.getChildrenCount() + " lượt thích");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //Save
    private void isSaved(final String postid, ImageView imageView) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if (datasnapshot.child(postid).exists()) {
                    imageView.setImageResource(R.drawable.ic_save_black);
                    imageView.setTag("saved");
                } else {
                    imageView.setImageResource(R.drawable.ic_save_color);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //edit post
    private void editPost(String postid) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Sửa tên ảnh");

        EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editText.setLayoutParams(layoutParams);
        alertDialog.setView(editText);

        getText(postid,editText);
        alertDialog.setPositiveButton("Chỉnh sửa",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("description",editText.getText().toString());

                        FirebaseDatabase.getInstance().getReference("Posts")
                                .child(postid).updateChildren(hashMap);
                    }
                });
        alertDialog.setNegativeButton("Huỷ bỏ",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
        //getText
    private void getText(String postid , final EditText editText){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(Post.class).getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //Báo cáo người dùng
    private void editBaoCao(String baocao, String postid, String description) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Báo cáo");

        EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editText.setLayoutParams(layoutParams);
        alertDialog.setView(editText);

        alertDialog.setPositiveButton("Xác nhận",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("baocao",editText.getText().toString());
                        hashMap.put("postid",postid);
                        hashMap.put("description",description);

                        FirebaseDatabase.getInstance().getReference("BaoCao")
                                .child(baocao).updateChildren(hashMap);
                        Toast.makeText(mContext, "Báo cáo thành công" , Toast.LENGTH_SHORT).show();
                    }
                });
        alertDialog.setNegativeButton("Huỷ bỏ",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile, post_image , like, comment, save, more,share,chats;
        public TextView username, likes, publisher, description,comments;
        public ViewHolder(@NonNull View itemView){
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            like =itemView.findViewById(R.id.like);
            comment =itemView.findViewById(R.id.comment);
            save =itemView.findViewById(R.id.save);
            likes =itemView.findViewById(R.id.likes);
            publisher=itemView.findViewById(R.id.publisher);
            description =itemView.findViewById(R.id.description);
            comments =itemView.findViewById(R.id.comments);
            username =itemView.findViewById(R.id.username);
            more = itemView.findViewById(R.id.more);
            share = itemView.findViewById(R.id.share);
            chats = itemView.findViewById(R.id.chats);
        }
    }
}
