package com.example.reppmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    //verificar si el usuario esta en linea, si no lo enviamos al activity main
    //firebase auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //actionbar y su titulo
        actionBar = getSupportActionBar();
        actionBar.setTitle("Perfil");

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //bottom navigation
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //home fragment transicion (default, en star)
        actionBar.setTitle("Inicio");
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();

        checkUserStatus();


    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new
            BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //manejador de item click
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            //home fragment transicion
                            actionBar.setTitle("Inicio");
                            HomeFragment fragment1 = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, fragment1, "");
                            ft1.commit();
                            return true;
                        case R.id.nav_profile:
                            //perfil fragment transicion
                            actionBar.setTitle("Perfil");
                            ProfileFragment fragment2 = new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, fragment2, "");
                            ft2.commit();
                            return true;
                    }
                    return false;
                }
            };

    private void checkUserStatus(){
        //obtener usuario actual
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //el usuario sigue signed
            //mostrar correo del user
            //mProfile.setText(user.getEmail());

        }else{
            //el usuario no esta signed, dirigir al mainactivity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //check en el inicio de la app
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    //inflate menu options

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //llenando menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //manjeador del menu options

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //obtener id del item seleccionado
        int id = item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
    */
}


