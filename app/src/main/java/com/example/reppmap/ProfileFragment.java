package com.example.reppmap;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reppmap.adapters.AdapterPosts;
import com.example.reppmap.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.*;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseStorage storage;

    //Almacenamiento
    StorageReference storageReference;

    //ruta donde las imagenes seran almacenadas
    String storagePath = "Users_Profile_Imgs/";

    //views del xml
    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton fab;
    RecyclerView postsRecyclerViews;

    //progress dialog
    ProgressDialog pd;

    //permisos constantes
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    //arreglo de permisos que seran requeridos
    String cameraPermissions[];
    String storagePermissions[];

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    //uri  de la imagen seleccionada
    Uri image_uri;

    //para verificar photo de perfil
    String profileOrCoverPhoto;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //inicializando fire
        firebaseAuth = FirebaseAuth.getInstance();
        user  = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");//pasamos el nombre de la coleccion
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        //storageReference = getInstance();

        //inicializando arrays de permisos
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //inicializamos las views
        avatarIv = view.findViewById(R.id.avatarIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        fab = view.findViewById(R.id.fab);
        postsRecyclerViews = view.findViewById(R.id.recyclerview_posts);

        pd = new ProgressDialog(getActivity());

        //Tenemos que obtener la info del usuario actualmente sign in. Lo podemos hacer por medio del correo o el uid
        //usando orderByChild query podremos mostrar los detalles de un nodo donde la key llamada correo tenga un valor igual al correo
        //del usuario actualmente sign in
        //Buscaremos en todos los nodos y donde la key haga match ahi obtendremos la infor de se nodo

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //check hasta que la data requerida sea obtenida
                for (DataSnapshot ds: snapshot.getChildren()){
                    //obteniendo datos
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String phone = ""+ ds.child("phone").getValue();
                    String image = ""+ ds.child("image").getValue();

                    //set los datos
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        //si la imagen es recibida se la asignamos
                        Picasso.get().load(image).into(avatarIv);
                    }catch (Exception e){
                        //si ocurre cualquier exepcion, asigna la por defecto
                        Picasso.get().load(R.drawable.ic_default_image_white).into(avatarIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });

        postList = new ArrayList<>();
        checkUserStatus();
        loadMyPosts();


        return view;
    }

    private void loadMyPosts() {
        //Linearlayout para recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show nuevos posts primero, por esta cargada hasta el ultimo
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set este layout para recyclerview
        postsRecyclerViews.setLayoutManager(layoutManager);

        //init lista posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query para cargar posts
        Query query = ref.orderByChild("uid").equalTo(uid);
        //obten toda la data de la ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //agregar a lista
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //poner este adaptator al recyclerviws
                    postsRecyclerViews.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
    private void searchMyPosts(final String searchQuery) {
        //Linearlayout para recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show nuevos posts primero, por esta cargada hasta el ultimo
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set este layout para recyclerview
        postsRecyclerViews.setLayoutManager(layoutManager);

        //init lista posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query para cargar posts
        Query query = ref.orderByChild("uid").equalTo(uid);
        //obten toda la data de la ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //agregar a lista
                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDesrc().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(myPosts);
                    }
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //poner este adaptator al recyclerviws
                    postsRecyclerViews.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
     */

    private boolean checkStoragePermission(){
        //check si el storage permission esta habilitado
        //regresa true si esta habilitado y si no, false
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        //request runtime storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        //regresa true si esta habilitado y si no, false
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        //solicitar en tiempo de ejecucion
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        //opciones para mostrar en dialog
        String options[] = {"Editar foto de perfil", "Editar nombre", "Editar telefono"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set titulo
        builder.setTitle("Elige una Acción");
        //set items para el dialog
        builder.setItems(options, (dialog, i) -> {
                //manejador de dialog item clicks
                if( i == 0){
                    //Edit perfil click
                    pd.setMessage("Actualizando foto de perfil");
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();
                }
                else if(i == 1){
                    //Edit nombre
                    pd.setMessage("Actualizando nombre");
                    //llamando al metodo y pasando la llave "name" como parametro par actualizar su valor en la database
                    showNamePhoneUpdateDialog("name");
                }
                else if(i == 2){
                    //Edit telefono
                    pd.setMessage("Actualizando telefono");
                    //llamando al metodo y pasando la llave "phone" como parametro par actualizar su valor en la database
                    showNamePhoneUpdateDialog("phone");
                }
        });
        //crear y mostrar dialog
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(final String key) {
        //el parametro key puede tener como valor tanto name como phone
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Actualizando "+key);
        //ponemos layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //agregar edit text
        EditText editText = new EditText(getActivity());
        editText.setHint("Introduce "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //agregar botones en el dialog
        builder.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //input text de edit text
                String value = editText.getText().toString().trim();
                //validar si el usuario ha introducido algo o no
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //actualizar, dismiss
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Actualizado..", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    //si user edit su nombre, tambien cambia en sus posts
                    if (key.equals("name")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds: snapshot.getChildren()){
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }else{
                    Toast.makeText(getActivity(), "Porfavor introduce "+key, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                pd.dismiss();
            }
        });
        //crear y mostrar dialog
        builder.create().show();
    }

    private void showImagePicDialog() {
        //mostramos dialog containing options camara y galeria para eligir una foto

        //opciones para mostrar en dialog
        String options[] = {"Camara", "Galeria"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set titulo
        builder.setTitle("Elige una imagen de");
        //set items para el dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //manejador de dialog item clicks
                if( i == 0){
                    //camara click
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else{
                        pickFromCamera();
                    }
                }
                else if(i == 1){
                    //galeria click
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }else{
                        pickFromGallery();
                    }
                }
            }
        });
        //crear y mostrar dialog
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*este metodo es llamado cuando el usuario presiona permitir o denegar de la peticion del dialog request
        aqui se manejaran los casos de permisos (permitidos y denegados)
         */
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                //eligiendo de la camera, primero verificamos si la camara tiene los permisos habilitados o no
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        //permisos habilitados
                        pickFromCamera();
                    }
                    else {
                        //permisos denegados 
                        Toast.makeText(getActivity(), "Porfavor habilita los permisos de Camara y Almacenamiento", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                //eligiendo de la galeria, primero verificamos si el almacenamiento tiene los permisos habilitados o no
                if (grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        //permisos habilitados
                        pickFromGallery();
                    }
                    else {
                        //permisos denegados
                        Toast.makeText(getActivity(), "Porfavor habilita los permisos de Almacenamiento", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //este metodo sera llamado despues que se haya seleccionado imagen de la camara o galeria
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //imagen is elegida de la galeria, obtener uri de imagen
                image_uri = data.getData();

                uploadProfilePhoto(image_uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //imagen is elegida de la camera, obtener uri de imagen
                uploadProfilePhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfilePhoto(final Uri uri) {
        //mostrar progress
        pd.show();
        //el parametro "image_uri" contiene el uri de la imagen elegida tanto de galeria como de camera
        //usaremos uid del actual signed usuario como nombre de la imagen asi habra una sola imagen
        //ruta y nombre de imagen para ser almacena en la storage de firebase
        //Ejem Users_Profile_Cover_Imgs/image_e12f3456f789.jpg
        String filePathAndName = storagePath+""+profileOrCoverPhoto+"_"+user.getUid();

        StorageReference storageReference2 = storageReference.child(filePathAndName);
        storageReference2.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //imagen es subida al almacenamiento, ahora obten su url y guarda en user's database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //vefica si la imagen se subio o no y url es recibido
                        if(uriTask.isSuccessful()){
                            //imagen subida
                            //agregar/actualizar url en user's data
                            HashMap<String, Object> results = new HashMap<>();
                            //primer parametro es profileOrCoverPhoto que tiene el valor de "image"
                            //las cuales son llaves en user's database donde url de una imagen sera salvada en una
                            //--el segundo parametro contiene la url de la imagen guardada en firebase storage,
                            //esta url podra ser guardada como llave
                            results.put(profileOrCoverPhoto,downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //url en la database de usuarios es agregado con exito
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Imagen Actualizada..", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error actualizando imagen..", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            if (profileOrCoverPhoto.equals("image")){
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ds: snapshot.getChildren()){
                                            String child = ds.getKey();
                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                        }else{
                            //error en la imagen
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Ocurrio algun error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //habra algunos errores, obtenemos y mostramos el error
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickFromGallery() {
        //seleccionar de la galeria
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);

    }

    private void pickFromCamera(){
        //intento de seleccionar imagen de la camara del dispositivo
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Foto");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descripción");
        //poner imagen uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent para iniciar camara
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void checkUserStatus(){
        //obtener usuario actual
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //el usuario sigue signed
            //mostrar correo del user
            //mProfile.setText(user.getEmail());
            uid = user.getUid();

        }else{
            //el usuario no esta signed, dirigir al mainactivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //llenando menu
        inflater.inflate(R.menu.menu_main, menu);

        /*
        MenuItem item = menu.findItem(R.id.action_search);
        //searchview of search user specific posts
        SearchView searchview  = (SearchView) MenuItemCompat.getActionView(item);

        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //llamado cuandoe el usuario presiona el search button
                if(!TextUtils.isEmpty(query)){
                    //SEARCH
                    searchMyPosts(query);
                }
                else{
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    //SEARCH
                    searchMyPosts(newText);
                }
                else{
                    loadMyPosts();
                }
                return false;
            }
        });
        */

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //obtener id del item seleccionado
        int id = item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        if(id == R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}