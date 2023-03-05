package com.example.instagram;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText emailIT, passwordIT;
    Button login;
    TextView dangnhap, passwordFG;
    ProgressDialog pd;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        emailIT = findViewById(R.id.emailIT);
        passwordIT = findViewById(R.id.passwordIT);
        login = findViewById(R.id.login);
        dangnhap = findViewById(R.id.dangnhap);
        passwordFG = findViewById(R.id.passwordFG);


        mAuth = FirebaseAuth.getInstance();
        dangnhap.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this,
                RegisterActivity.class)));
        login.setOnClickListener(view -> {

            String str_email = emailIT.getText().toString();
            String str_password = passwordIT.getText().toString();

            if (TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)) {
                Toast.makeText(LoginActivity.this, "Hồ sơ chưa được duyệt", Toast.LENGTH_SHORT).show();
            } else {
                login(str_email, str_password);
            }
        });

        //quên mật khẩu
        passwordFG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });
    }
    private void login (String str_email, String str_password){
        ProgressDialog pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage("Vui lòng đợi...");
        pd.show();
        mAuth.signInWithEmailAndPassword(str_email, str_password)
                .addOnCompleteListener(LoginActivity.this, task -> {
                    if (task.isSuccessful()) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                pd.dismiss();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                pd.dismiss();
                            }
                        });
                    } else {
                        pd.dismiss();
                        Toast.makeText(LoginActivity.this, "Đăng nhập không thành công", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showRecoverPasswordDialog () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quên mật khẩu");

        LinearLayout linearLayout = new LinearLayout(this);
        EditText emailIT = new EditText(this);
        emailIT.setHint("Email");
        emailIT.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        emailIT.setMinEms(16);

        linearLayout.addView(emailIT);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);
        //Confirm : xác nhận
        builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailIT.getText().toString().trim();
                beginRecovery(email);
            }
        });
        //cancel : huỷ bỏ
        builder.setNegativeButton("Huỷ bỏ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }

        private void beginRecovery (String email){
            ProgressDialog pd = new ProgressDialog(LoginActivity.this);
            pd.setMessage("Đang gửi email...");
            pd.show();
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    pd.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Gửi email thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Lỗi ! Kiểm tra lại", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
}

