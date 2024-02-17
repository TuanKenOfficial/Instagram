package com.example.instagram;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.instagram.Adapter.AdapterChat;
import com.example.instagram.Model.Chats;
import com.example.instagram.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;

    private String receiptUid ="";
    private String receiptFcmToken ="";
    private String myUid ="";
    private String myName =""; //liên quan loadMyInfo bên dưới

    private  static final String TAG = "CHAT";

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    private String chatPath = "";

    private Uri imageUri = null;

    private Intent intent;


    TextView txt1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi trong giây lát");
        progressDialog.setCanceledOnTouchOutside(false);

//        txt1.setGravity(1);

        //get intent
        intent = getIntent();
        receiptUid = intent.getStringExtra("receiptUid"); //uid người nhận tin nhắn chuyển qua Chats Activity
        //uid người dùng đăng nhập - gửi tin nhắn
        myUid = firebaseAuth.getUid();


        //chat path
        chatPath = Utils.chatPath(receiptUid,myUid); // xem lại cái này

        loadMyInfo();//load thông báo chat
        loadMessage(); // load tin nhắn
        loadToUidDetails(); // load thông tin tên và hình profile người nhận tin nhắn


        Log.d(TAG, "onCreate: receiptUid: "+receiptUid);
        Log.d(TAG, "onCreate: myUid: "+myUid);
        Log.d(TAG, "onCreate: chatPath: "+chatPath);




        //quay lại
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //upload ảnh chat
        binding.attachFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: bấm vào thay đổi ảnh");
                imageDialog();
            }
        });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: gửi tin nhắn");
                validate();
            }
        });


    }

    private void loadMyInfo() {
        Log.d(TAG, "loadMyInfo: ");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.child(""+firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myName = ""+snapshot.child("username").getValue();
                        Log.d(TAG, "onDataChange: myName: "+myName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    //load thông tin người nhận tin nhắn
    // coi lại chỗ này
    private void loadToUidDetails() {
        Log.d(TAG, "loadToUidDetails: ");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(receiptUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String name = "" +snapshot.child("username").getValue();
                            String profileImageUrl =""+snapshot.child("imageurl").getValue();
                            receiptFcmToken =""+snapshot.child("fcmToken").getValue();
                            Log.d(TAG, "onDataChange: name: "+name);
                            Log.d(TAG, "onDataChange: profileImageUrl: "+profileImageUrl);
                            Log.d(TAG, "onDataChange: receiptFcmToken: "+receiptFcmToken);

                            binding.toolbarTitleTv.setText(name); //load tên người nhận tin nhắn


                            try {
                                /*
                                 * Ở đây có 2 cách dùng hình ảnh
                                 * cách 1: dùng Glide
                                 * cách 2: dùng Picasso
                                 * Các bạn có thể dùng 1 trong 2 cách trên nhá*/
                                Glide.with(ChatActivity.this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.image)
                                        .into(binding.profileIv);
//                                Picasso.get().load(profileImageUrl).placeholder(R.drawable.image).error(R.drawable.ic_image_broken_gray).into(binding.profileIv);
                                Log.d(TAG, "onBindViewHolder: Load thành công hình ảnh");
                            }
                            catch (Exception e){
                                Log.e(TAG, "onDataChange: Lỗi ko load được hình ảnh", e);
                            }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    //load tin nhắn
    // coi lại chỗ này
    private void loadMessage() {
        Log.d(TAG, "loadMessage: ");
        //danh sách tin nhắn
        ArrayList<Chats> chatsArrayList = new ArrayList<>();

        DatabaseReference refChats = FirebaseDatabase.getInstance().getReference("Chats");
        refChats.child(chatPath)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatsArrayList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()){
                            try {
                                //lấy dữ liêệu model từ cơ sở dữ liệu
                                Chats modelChats = ds.getValue(Chats.class);
                                //add vào danh sách chats
                                chatsArrayList.add(modelChats);
                                Log.d(TAG, "onDataChange: Load thành công tin nhắn");
                            }
                            catch (Exception e){
                                Log.d(TAG, "onDataChange: Lỗi ko load được tin nhắn" , e);
                            }
                        }
                        // đưa lên adapter
                        AdapterChat adapterChats = new AdapterChat(ChatActivity.this, chatsArrayList);
                        binding.chatRv.setAdapter(adapterChats);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void imageDialog() {
        PopupMenu popupMenu = new PopupMenu(ChatActivity.this,binding.attachFab);
        popupMenu.getMenu().add(Menu.NONE,1,1,"Camera");
        popupMenu.getMenu().add(Menu.NONE,2,2,"Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId ==1){
                    Log.d(TAG, "onMenuItemClick: Mở camera, check camera");
                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
                        requestCameraPemissions.launch(new String[]{Manifest.permission.CAMERA});
                    }else {
                        requestCameraPemissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                }
                else if (itemId==2){
                    Log.d(TAG, "onMenuItemClick: Mở storage, check storage");
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                        pickFromGallery();
                    }else {
                        requestStoragePemissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                return false;
            }
        });
    }
    private ActivityResultLauncher<String[]> requestCameraPemissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String,Boolean>>(){

                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: "+result.toString());
                    boolean areAllGranted = true;
                    for (Boolean isGranted: result.values()){
                        areAllGranted = areAllGranted && isGranted;
                    }
                    if (areAllGranted){
                        Log.d(TAG, "onActivityResult: Tất cả quyền camera & storage");
                        pickFromCamera();
                    }
                    else {
                        Log.d(TAG, "onActivityResult: Tất cả hoặc chỉ có một quyền");
                        Toast.makeText(ChatActivity.this, "Quyền camera hoặc storage", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    private ActivityResultLauncher<String> requestStoragePemissions = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    if (isGranted){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Quyền Storage chưa cấp quyền", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLaucher.launch(intent);
    }
    private ActivityResultLauncher<Intent> galleryActivityResultLaucher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: Hình ảnh thư viện: "+imageUri);
                        Intent data = result.getData();
                        imageUri = data.getData();

                        uploadChatImageStorageDb(); //đưa lên csdl
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Hủy", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Chat_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Chat_Image_Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLaucher.launch(intent);

    }
    private ActivityResultLauncher<Intent> cameraActivityResultLaucher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: Hình ảnh: "+imageUri);
                        uploadChatImageStorageDb(); //đưa lên csdl
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Hủy", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    //upload hình ảnh chat lên csdl
    private void uploadChatImageStorageDb() {
        progressDialog.setMessage("Upload Image...");
        progressDialog.show();
        Log.d(TAG, "updateAnh: Upload ảnh chats");
        //name and path of image
        long timestamp = Utils.getTimestamp();
        String filePathAndName = "chats_images/"+"chats_"+ timestamp;
        Log.d(TAG, "updateAnh: "+filePathAndName);
        //upload image
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        storageReference.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: "+progress);
                        progressDialog.setMessage("Upload hình ảnh. Tiến triển: "+(int)progress + "%");

                    }
                })
                .addOnSuccessListener(taskSnapshot -> {
                    //get url of uploaded image
                    Log.d(TAG, "uploadProfileImageStorageDb: Upload thành công...");
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String imageUrl = uriTask.getResult().toString();
                    if(uriTask.isSuccessful()){
                        sendMessage(Utils.MESSAGE_TYPE_IMAGE,imageUrl,timestamp);
                        Log.d(TAG, "uploadChatImageStorageDb: ");
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Lỗi: "+e);

                    }
                });
    }

    //xử lý gửi message

    private void validate(){
        Log.d(TAG, "validate: ");

        String message = binding.edtChats.getText().toString().trim();

        long timestamp = Utils.getTimestamp();

        //xét điều kiện người dùng đã nhập chat chưa
        if (message.isEmpty()){
            Toast.makeText(this, "Nhập nội dung chat vào đây, không được bỏ trống!!", Toast.LENGTH_SHORT).show();

        }
        else {
            sendMessage(Utils.MESSAGE_TYPE_TEXT, message, timestamp);
        }
    }
    private void sendMessage(String messageType, String message, long timestamp){
        Log.d(TAG, "sendMessage: messageType: "+messageType);
        Log.d(TAG, "sendMessage: message: "+message);
        Log.d(TAG, "sendMessage: timestamp: "+timestamp);

        progressDialog.setMessage("Gửi tin nhắn chats...");
        progressDialog.show();

        DatabaseReference refChat = FirebaseDatabase.getInstance().getReference("Chats");
        String idChat = "" + refChat.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("messageId",idChat);
        hashMap.put("messageType",messageType);
        hashMap.put("message",message);
        hashMap.put("fromUid",myUid); // uid người gửi
        hashMap.put("toUid",receiptUid); // uid người nhận
        hashMap.put("timestamp",timestamp);
        hashMap.put("call","");
        hashMap.put("callVideos","");

        refChat.child(chatPath)
                .child(idChat)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        binding.edtChats.setText("");

                        if (messageType.equals(Utils.MESSAGE_TYPE_TEXT)){
                            Toast.makeText(ChatActivity.this, "Bạn đã gửi một tin nhắn: "+message, Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(ChatActivity.this, "Bạn Đã gửi một tệp hình ảnh", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Lỗi "+e);
                    }
                });

    }

}
