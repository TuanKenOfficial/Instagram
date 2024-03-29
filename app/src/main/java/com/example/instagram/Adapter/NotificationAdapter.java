package com.example.instagram.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.Fragment.PostDetailFragment;
import com.example.instagram.Fragment.ProfileFragment;
import com.example.instagram.Model.Notification;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private List<Notification> mNotification;
    private FirebaseAuth firebaseAuth;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.mContext = context;
        this.mNotification = notificationList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent ,false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final Notification notification = mNotification.get(position);
        viewHolder.text.setText(notification.getText());
        getUser(viewHolder.image_profile, viewHolder.username,notification.getUserid());

        if (notification.isIspost()){
            viewHolder.post_image.setVisibility(View.VISIBLE);
            getPostImage(viewHolder.post_image, notification.getPostid());
        }else {
            viewHolder.post_image.setVisibility(View.GONE);
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notification.isIspost()){
                    mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit().putString("postid", notification.getPostid()).apply();
                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new PostDetailFragment()).commit();
                }else {
                    mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit().putString("profileid", notification.getUserid()).apply();
                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit();
                }
            }
        });

        //nhấn that lâu để xoá thông báo
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alers = new AlertDialog.Builder(mContext);
                //thiết lập tiêu đề:
                alers.setTitle("Xoá thông");
                //thiết lập icon:
                alers.setIcon(R.drawable.story_delete);
                //thiết lập nội dung cho dialog:
                alers.setMessage("Bạn có chắc chắn muốn xoá thông báo này?");
                //thiết lập các nút lệnh để người dùng tương tác:
                alers.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications");
                        reference.child(firebaseAuth.getUid()).child(notification.getIdNotification()).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(mContext, "Xoá", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                alers.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                //tạo cửa sổ Dialog:
                AlertDialog dialog=alers.create();
                dialog.setCanceledOnTouchOutside(false);
                //hiển thị cửa sổ này lên:
                dialog.show();

               return false;
            }

        });
    }

    public int getItemCount() {
        return mNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile , post_image;
        public TextView username , text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            username= itemView.findViewById(R.id.username);
            text = itemView.findViewById(R.id.comment);

        }
    }
    //load user hình, tên
    private void getUser(final ImageView imageView ,final TextView username, String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                User user = datasnapshot.getValue(User.class);
                Picasso.get().load(user.getImageurl()).placeholder(R.drawable.userlogo).into(imageView);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //load hình ảnh
    private void getPostImage(ImageView imageView , String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                Post post = datasnapshot.getValue(Post.class);
                Picasso.get().load(post.getPostimage()).placeholder(R.drawable.image).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
