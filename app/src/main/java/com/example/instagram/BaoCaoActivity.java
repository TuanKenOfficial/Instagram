package com.example.instagram;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class BaoCaoActivity extends AppCompatActivity {

    CheckBox chk1, chk2, chk3, chk4, chk5;
    Button btn_huy, btn_baocao;
    FirebaseAuth mAuth;
    DatabaseReference reference;
    private String currentUserID;

    private String saveCurrentDate, saveCurrentTime, postName; // ngày giờ
//    List<Report> mReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bao_cao);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference("Reports");

        chk1 = (CheckBox) findViewById(R.id.chk1);
        chk2 = (CheckBox) findViewById(R.id.chk2);
        chk3 = (CheckBox) findViewById(R.id.chk3);
        chk4 = (CheckBox) findViewById(R.id.chk4);
        chk5 = (CheckBox) findViewById(R.id.chk5);
        btn_huy = (Button) findViewById(R.id.btn_huy);
        btn_baocao = (Button) findViewById(R.id.btn_baocao);



        //huỷ
        btn_huy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //báo cáo
        btn_baocao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResult();
                String msg = chk1.getText().toString();
                finish();
            }
        });

    }

    private void showResult() {

            String message = null;
            if(this.chk1.isChecked()) {
                message =  this.chk1.getText().toString();
            }
            if(this.chk2.isChecked()) {
                if(message== null)  {
                    message =  this.chk2.getText().toString();
                } else {
                    message += ", " + this.chk2.getText().toString();
                }
            }
            if(this.chk3.isChecked()) {
                if(message== null)  {
                    message =  this.chk3.getText().toString();
                } else {
                    message += ", " + this.chk3.getText().toString();
                }
            }
            if(this.chk4.isChecked()) {
                if(message== null)  {
                    message =  this.chk4.getText().toString();
                } else {
                    message += ", " + this.chk4.getText().toString();
                }
            }
            if(this.chk5.isChecked()) {
                if(message== null)  {
                    message =  this.chk5.getText().toString();
                } else {
                    message += ", " + this.chk5.getText().toString();
                }
            }

            message = message == null? "You select nothing": "" + message;
            Toast.makeText(this, "Nội dung báo cáo: "+message, Toast.LENGTH_LONG).show();

            Intent intent = getIntent();
            String postid = intent.getStringExtra("postid");
            sendReport(currentUserID,postid,message);

    }
    private void sendReport(String currentUserID,String postid,String reason){
        //currentDate
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentData = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentData.format(calendar.getTime());
        //currentTime
        Calendar calendar1 = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());
        // phần name bằng 2 cái current cộng lại
        postName = saveCurrentDate+saveCurrentTime;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Reports");
        String baocaoId = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("baocaoId",baocaoId);
        hashMap.put("uid",currentUserID);
        hashMap.put("postid",postid);
        hashMap.put("reason",reason);
        hashMap.put("time" , saveCurrentTime);
        hashMap.put("date",saveCurrentDate);

        reference.child(baocaoId).setValue(hashMap);
    }
}
