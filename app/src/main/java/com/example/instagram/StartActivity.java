package com.example.instagram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {
    Button login , register;
    FirebaseUser firebaseUser;


    @Override
    protected void onStart(){
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //redirect if user is not null
        if (firebaseUser != null){
            startActivity(new Intent(StartActivity.this , MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);

        login.setOnClickListener(view -> startActivity(new Intent(StartActivity.this, LoginActivity.class)));

        register.setOnClickListener(view -> startActivity(new Intent(StartActivity.this, RegisterActivity.class)));
    }
}