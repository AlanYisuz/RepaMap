package com.example.reppmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    //vistas
    EditText mEmailEt, mPasswordEt;
    TextView notCuenta, mRecoverPasswordEt;
    Button mLoginBtn;
    ProgressDialog pd;

    private FirebaseAuth mAuth;//variable para la autenticacion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //actionbars y su titulo
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Inicio de Sesión");
        //botones de regreso
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //inicializamos
        mAuth = FirebaseAuth.getInstance();

        mEmailEt = findViewById(R.id.loginCorreo);
        mPasswordEt = findViewById(R.id.loginContrasena);
        mLoginBtn = findViewById(R.id.iniciarSesionBtn);
        notCuenta = findViewById(R.id.textRegister);
        mRecoverPasswordEt = findViewById(R.id.forgetPasswrod);

        //manejador de boton login
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //entrada de datos
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //email invalido
                    mEmailEt.setError("Introduce un correo valido");
                    mEmailEt.setFocusable(true);
                }else if(password == null) {
                    mPasswordEt.setError("El campo no puede estar vacio");
                    mPasswordEt.setFocusable(true);
                }else if(password.length()<6){
                    mPasswordEt.setError("La contraseña debe tener al menos 6 caracteres");
                    mPasswordEt.setFocusable(true);
                }else{
                    //patron de email validado
                    loginUser(email, password);
                }
            }
        });

        //no tiene cuenta manejador
        notCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        //manejador para recuperar cuenta
        mRecoverPasswordEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialog();
            }
        });

        //inicalizamos el progressdialog
        pd = new ProgressDialog(this);
    }

    private void showRecoverPasswordDialog() {
        //alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar contraseña");
        //poner un layout, linearlayout
        LinearLayout linearLayout = new LinearLayout(this);
        //vistas mostradas en el layout
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        //ponemos el min de width de un editText para encajar un texto de un n de letras
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //boton para recuperar
        builder.setPositiveButton("Recuperar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //input de email
                String email = emailEt.getText().toString().trim();
                beginRecovery (email);
            }
        });
        //boton para cancelar
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //deshacer dialog
                dialogInterface.dismiss();
            }
        });

        //mostrar dialog
        builder.create().show();
    }

    private void beginRecovery(String email) {
        //mostrar progreso
        pd.setMessage("Enviando email..");
        pd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Email Enviado", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(LoginActivity.this, "Fallo....", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Error
                pd.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        //mostrar progreso
        pd.setMessage("Iniciando Sesión");
        pd.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //deshacer progressdialog
                            pd.dismiss();
                            //si el inicio de sesion fue un exito
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        }else{
                            Toast.makeText(LoginActivity.this, "Autenticacion falló",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //mostrar el error
                        pd.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//ir al anterior activity
        return super.onSupportNavigateUp();
    }

}