package com.example.instagram;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.example.instagram.databinding.ActivityAddStoryBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {
    private Uri mImageUri;
    String myUrl = "";
    StorageReference storageReference;

    private String imageUrl = "";
    private StorageTask uploadTask;

    private static final String TAG="Story";

    private FirebaseAuth firebaseAuth;

    private ActivityAddStoryBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        binding.storyPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickOption();

            }
        });


    }
    private void showImagePickOption() {
        Log.d(TAG, "showImagePickOption: ");
        PopupMenu popupMenu = new PopupMenu(AddStoryActivity.this,binding.storyPhoto );
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == 1) {
                    Log.d(TAG, "onMenuItemClick: Mở camera, check camera");
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        requestCameraPemissions.launch(new String[]{android.Manifest.permission.CAMERA});
//                    } else {
//                        requestCameraPemissions.launch(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE});
//                    }
                } else if (itemId == 2) {
                    Log.d(TAG, "onMenuItemClick: Mở storage, check storage");
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        pickFromGallery();
//                    } else {
//                        requestStoragePemissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                    }
                }
                return false;
            }
        });
//        publishStory();
    }

    private void publishStory(){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Tải lên");
        progressDialog.show();
        Log.d(TAG, "uploadImageUrl: ");
        if (mImageUri != null) {
            String fileNamePath = "Story/";
            storageReference = FirebaseStorage.getInstance().getReference(fileNamePath)
                    .child(firebaseAuth.getUid());
            uploadTask = storageReference.putFile(mImageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Log.d(TAG, "onSuccess: ");

                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    myUrl = uriTask.getResult().toString();
                    Log.d(TAG, "onSuccess: url: " + myUrl);

                    if (uriTask.isSuccessful()) {
                        progressDialog.dismiss();
                        String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(myid);
                        String storyid = reference.push().getKey();

                        long timeend = System.currentTimeMillis()+86400000;// 1 days
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageurl",myUrl);
                        hashMap.put("timestart", ServerValue.TIMESTAMP);
                        hashMap.put("timeend", timeend);
                        hashMap.put("storyid", storyid);
                        hashMap.put("userid", myid);
                        reference.child(storyid).setValue(hashMap);

                        Toast.makeText(AddStoryActivity.this, "Upload Story thành công", Toast.LENGTH_SHORT).show();


                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddStoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Upload không thành công!! Bạn chưa chọn ảnh", Toast.LENGTH_LONG).show();
        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Log.d(TAG, "onActivityResult: 111");
            mImageUri= UCrop.getOutput(data);
            publishStory();
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Log.d(TAG, "onActivityResult: 222");
            final Throwable cropError = UCrop.getError(data);
        }
    }
}