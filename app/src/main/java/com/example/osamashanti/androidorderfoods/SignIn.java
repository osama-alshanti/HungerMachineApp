package com.example.osamashanti.androidorderfoods;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.osamashanti.androidorderfoods.Common.Common;
import com.example.osamashanti.androidorderfoods.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.zip.Inflater;

import info.hoang8f.widget.FButton;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignIn extends AppCompatActivity {

    FButton btnSignIn;
    MaterialEditText edtPhone,edtPassword;
    TextView forgot_pass;
    com.rey.material.widget.CheckBox chbRemember;
    FirebaseDatabase database;
    DatabaseReference table_user;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_sign_in);


        edtPhone = (MaterialEditText) findViewById(R.id.edtPhone);
        edtPassword = (MaterialEditText) findViewById(R.id.edtPassword);
        btnSignIn =  findViewById(R.id.btnSignIn);


        forgot_pass = findViewById(R.id.forgot_pass);
        chbRemember = findViewById(R.id.chbRemember);

        Paper.init(this);


         database = FirebaseDatabase.getInstance();
         table_user = database.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Common.isConnectedToInternet(getBaseContext())){

                    //Save User And Pass
                    if(chbRemember.isChecked()){
                        Paper.book().write(Common.USER_KEY,edtPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY,edtPassword.getText().toString());
                    }

                final ProgressDialog dialog = new ProgressDialog(SignIn.this);
                dialog.setMessage("Please Waiting ...");
                dialog.show();

                table_user.addListenerForSingleValueEvent(new ValueEventListener(){

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                        //if the user not exist in datebase

                        if(dataSnapshot.child(edtPhone.getText().toString()).exists()){//0592244405


                        //get user information

                        User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                        user.setPhone(edtPhone.getText().toString());

                        if(user.getPassword().equals(edtPassword.getText().toString()) && !Boolean.parseBoolean(user.getIsStaff())){
                            dialog.dismiss();
                            Intent homeIntent = new Intent(SignIn.this,RestaurantList.class);
                            Common.currentUser = user;
                            startActivity(homeIntent);
                            finish();

                            table_user.removeEventListener(this);


                        }else{
                            dialog.dismiss();
                            Toast.makeText(SignIn.this, "Sign In Failed , Please Try Again !!!", Toast.LENGTH_SHORT).show();
                        }

                        }else{
                            dialog.dismiss();
                            Toast.makeText(SignIn.this, "User not exist !!", Toast.LENGTH_SHORT).show();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }else{
                    Toast.makeText(SignIn.this, "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        forgot_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPassword();
            }
        });

    }

    private void showForgotPassword() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(SignIn.this);
        builder.setTitle("Forgot Password");
        builder.setMessage("Enter your secure code");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.forgot_password_layout,null);
        builder.setView(view);
        builder.setIcon(R.drawable.ic_security_black_24dp);

        final MaterialEditText edtPhone = view.findViewById(R.id.edtPhone);
        final MaterialEditText edtSecureCode = view.findViewById(R.id.edtSecureCode);



        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(edtPhone.getText().toString().equals("") ){
                    Toast.makeText(getBaseContext(), "Please enter your phone !", Toast.LENGTH_SHORT).show();
                    return;
                }else if(edtSecureCode.getText().toString().equals("")){
                    Toast.makeText(getBaseContext(), "Please enter your secure code !", Toast.LENGTH_SHORT).show();
                    return;
                }

                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);

                        if(user.getSecureCode().equals(edtSecureCode.getText().toString())){
                            Toast.makeText(SignIn.this, "Your Password : "+user.getPassword(), Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(SignIn.this, "Wrong secure code !", Toast.LENGTH_SHORT).show();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });
        builder.show();

    }

}
