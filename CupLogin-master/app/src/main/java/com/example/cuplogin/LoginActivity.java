package com.example.cuplogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button loginBtn;
    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;
    DatabaseReference firebaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = (EditText) findViewById(R.id.loginEmail);
        loginEmail.setInputType(InputType.TYPE_CLASS_TEXT);

        loginPassword = (EditText) findViewById(R.id.loginPassword);
        loginBtn = (Button) findViewById(R.id.login_btn);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        firebaseRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }


//       signupBtnLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
//                finish();
//            }
//        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNetworkAvailable()) {
                    final String username = loginEmail.getText().toString();
                    final String password = loginPassword.getText().toString();

                    if (TextUtils.isEmpty(username)) {
                        Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    progressBar.setVisibility(View.VISIBLE);

                    //get the emailId associated with the username
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    firebaseRef.child("user_ids").child(username)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String userEmail = dataSnapshot.getValue(String.class);
                                        Log.d("Login", "Login with username");
                                        performLogin(userEmail, password, username);
                                    } else {

                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(LoginActivity.this, "Invalid Username or password!", Toast.LENGTH_LONG).show();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                } else {
                    Toast.makeText(LoginActivity.this, "Check Internet connection before logging in!", Toast.LENGTH_LONG).show();
                }
            }
    });

}


    private void performLogin(String emailId, final String password, final String username) {

        firebaseAuth.signInWithEmailAndPassword(emailId,password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);

                if(!task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Invalid Username or password!", Toast.LENGTH_LONG).show();
                }else{
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username",username);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}