package com.example.instagram;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.instagram.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    FirebaseAuth auth;
    DatabaseReference reference;
    ProgressDialog pd;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        auth = FirebaseAuth.getInstance();
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.noAccountTv.setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        binding.registerBtn.setOnClickListener(view -> {
            pd = new ProgressDialog(RegisterActivity.this);
            pd.setMessage("Vui lòng đợi...");
            pd.show();

            String str_username = binding.nameEt.getText().toString();
            String str_fullname = binding.fullnameEt.getText().toString();
            String str_email = binding.emailEt.getText().toString();
            String str_password = binding.passwordEt.getText().toString();

            if (TextUtils.isEmpty(str_username) || TextUtils.isEmpty(str_fullname) ||
                    TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)) {
                Toast.makeText(RegisterActivity.this, "Hồ sơ chưa được duyệt", Toast.LENGTH_SHORT).show();
            } else if (str_password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Mật khẩu phải có 6 số", Toast.LENGTH_SHORT).show();
            } else {
                register(str_username,str_fullname,str_email,str_password);

            }
        });
    }

    private void register (String username, String fullname, String email , String password){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, task -> {
                    if (task.isSuccessful()){
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        assert firebaseUser != null;
                        String userid = firebaseUser.getUid();

                        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("id", userid);
                        hashMap.put("username",username.toLowerCase());//to lower case : viết thường
                        hashMap.put("fullname",fullname);
                        hashMap.put("bio", "");
                        hashMap.put("imageurl","https://firebasestorage.googleapis.com/v0/b/instagram-305da" +
                                ".appspot.com/o/placeholder.jpg?alt=media&token=f64f2943-1fa5-4ffc-b757-1f029300ee36");
                        reference.setValue(hashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()){
                                pd.dismiss();
                                Toast.makeText(RegisterActivity.this,"Đăng kí thành công", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        });
                    } else{
                        pd.dismiss();
                        Toast.makeText(RegisterActivity.this,"Email đã đăng kí rồi , mật khẩu bạn nhập sai",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

