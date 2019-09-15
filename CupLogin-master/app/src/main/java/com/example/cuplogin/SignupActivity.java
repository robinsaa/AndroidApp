package com.example.cuplogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    EditText emailET,passwordET,usernameET;
    Button registerBtn;
    FirebaseAuth firebaseAuth;
    DatabaseReference firebaseRef,firebaseUserRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_signup);

        emailET = (EditText) findViewById(R.id.email);
        passwordET = (EditText) findViewById(R.id.password);
        usernameET = (EditText) findViewById(R.id.username);
        registerBtn = (Button) findViewById(R.id.signup_btn);


        firebaseRef = FirebaseDatabase.getInstance().getReference();
        firebaseUserRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailET.getText().toString();
                String password = passwordET.getText().toString();
                final String username  = usernameET.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Please fill in the required fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Please fill in the required fields", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(getApplicationContext(), "Please fill in the required fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                }


                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    firebaseRef.child("user_ids").child(usernameET.getText().toString()).setValue(emailET.getText().toString());
                                    HashMap userData = new HashMap();
                                    userData.put("email", email);
                                    userData.put("cafe-id", username);
                                    userData.put("user-role", "user");

                                    if (firebaseAuth.getCurrentUser() != null) {

                                        firebaseUserRef.child(firebaseAuth.getCurrentUser().getUid()).updateChildren(userData).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {

                                                if (task.isSuccessful()) {

                                                    Toast.makeText(SignupActivity.this, "Saved details", Toast.LENGTH_LONG).show();

                                                } else {
                                                    Toast.makeText(SignupActivity.this, "Error sending data" , Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });

                                    }
                                    Toast.makeText(getBaseContext(),"You are successfully registered ",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "E-mail or password is wrong", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }



}
