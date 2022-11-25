package com.example.reppmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //vistas
    EditText mEmailEt, mPasswordEt, mConfirmPasswordEt;
    TextView tienesCuenta;
    Button RegisterBtn;
    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;//variable para la autenticacion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //actionbars y su titulo
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Crear cuenta");
        //botones de regreso
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        //Inicializamos
        mAuth = FirebaseAuth.getInstance();

        mEmailEt = findViewById(R.id.correo);
        mPasswordEt = findViewById(R.id.contrasena);
        mConfirmPasswordEt = findViewById(R.id.confirmarContrasena);
        RegisterBtn = (Button) findViewById(R.id.registarseBtn);
        tienesCuenta = findViewById(R.id.textRegister);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando usuario");

        //manjeador del click del buton
        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //input de correo, contrasena y confirm
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                String confirmP = mConfirmPasswordEt.getText().toString().trim();
                //validando
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //fija el error y focus en el input
                    mEmailEt.setError("Introduce un correo valido");
                    mEmailEt.setFocusable(true);
                }else if(password.length()<6) {
                    mPasswordEt.setError("Introduce una contraseña de al menos 6 caracteres");
                    mPasswordEt.setFocusable(true);
                }else if (!confirmP.equals(password)){
                    mConfirmPasswordEt.setError("Introduce una contraseña que coincida");
                    mConfirmPasswordEt.setFocusable(true);
                }else{
                    registerUser(email, password);
                }
            }
        });

        tienesCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //verificamos si el user fue registrado
                        if(task.isSuccessful()){
                            //fue un exito, desecha el progressdialog e inicia registerActivity
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                            String email = user.getEmail();
                            String uid = user.getUid();
                            //cuando el usuario es registrado, su informacion tambien sera almacenada en la database realtime
                            //Usando HashMap
                            HashMap<Object, String> hashMap = new HashMap<>();
                            //poner info en hashmap
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", "");
                            hashMap.put("phone", "");
                            hashMap.put("image", "");
                            //firebase database instancia
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //camino para almacenar los user data
                            DatabaseReference reference = database.getReference("Users");
                            //poner data dentro del hashmap en database
                            reference.child(uid).setValue(hashMap);


                            Toast.makeText(RegisterActivity.this, "Registrado...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "El registro fallo! Prueba de nuevo ",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //error, disolver progressdialog
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//ir al anterior activity
        return super.onSupportNavigateUp();
    }
}
