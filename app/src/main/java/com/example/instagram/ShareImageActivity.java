package com.example.instagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.ProgressDialog;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.example.instagram.databinding.ActivityShareImageBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;

public class ShareImageActivity extends AppCompatActivity {
    //view binding
    private ActivityShareImageBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;
    private String postid,postimage, postDescription, postPublisher, postTimestamp;

    //arraylist pdf categories


    //uri of picked
    private Uri pdfUri = null;


    //tag for debugging
    private static final String TAG = "ShareImage";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShareImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        postid = getIntent().getStringExtra("postid");
        postimage = getIntent().getStringExtra("postimage");
        postDescription = getIntent().getStringExtra("description");
        postPublisher = getIntent().getStringExtra("publisher");
        Log.d(TAG, "onCreate: "+postid);
        Log.d(TAG, "onCreate: "+postimage);
        Log.d(TAG, "onCreate: "+postDescription);
        Log.d(TAG, "onCreate: "+postPublisher);

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi trong giây lát");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.imageTv.setText(postimage);

        //handle click, go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareTextOnly();
                sharePDFAndText();
            }
        });


    }

    private void shareTextOnly() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Instagram Clone");
        intent.putExtra(Intent.EXTRA_TEXT,""+postimage);
        startActivity(Intent.createChooser(intent,"Chia sẻ qua"));
    }


    private Uri sharePDFAndText() {

        File imageFolder = new File(this.getCacheDir(),"InstagramClone/image/");
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, ""+postimage);

            FileOutputStream stream = new FileOutputStream(file);
            stream.flush();
            stream.close();
            pdfUri = FileProvider.getUriForFile(ShareImageActivity.this,"com.example.instagram.fileprovider",file);
        }catch (Exception e){
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return pdfUri;
    }
}
