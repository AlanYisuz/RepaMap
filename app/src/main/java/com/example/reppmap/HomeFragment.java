

package com.example.reppmap;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.reppmap.adapters.AdapterPosts;
import com.example.reppmap.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    //firebase
    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);//para mostrar menu option en fragment
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        //recycler view y su propiedades
        recyclerView = view.findViewById(R.id.postsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //mostrar los nuevos posts primero.
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout para recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //mostrar
        postList = new ArrayList<>();

        loadPosts();

        return view;
    }

    private void loadPosts() {
        //camino de todos los posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //obtener toda la data de la ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
             postList.clear();
             for (DataSnapshot ds: snapshot.getChildren()){
                 ModelPost modelPost = ds.getValue(ModelPost.class);

                 postList.add(modelPost);

                 //adaptador
                 adapterPosts = new AdapterPosts(getActivity(), postList);
                 //set adapter para recyclerview
                 recyclerView.setAdapter(adapterPosts);

             }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //en caso de error
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
    private void searchPosts(String searchQuery){
        //camino de todos los posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //obtener toda la data de la ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    String id = modelPost.getUid();
                    String titulo = modelPost.getpTitle();
                    String descr = modelPost.getpDesrc();

                    if (titulo.toLowerCase().contains(searchQuery.toLowerCase()) ||
                            descr.toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(modelPost);
                    }

                    //adaptador
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set adapter para recyclerview
                    recyclerView.setAdapter(adapterPosts);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //en caso de error
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    */


    private void checkUserStatus(){
        //obtener usuario actual
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //el usuario sigue signed
            //mostrar correo del user
            //mProfile.setText(user.getEmail());

        }else{
            //el usuario no esta signed, dirigir al mainactivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

//inflate menu options

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //llenando menu
        inflater.inflate(R.menu.menu_main, menu);
        /*
        //searchview para buscar posts
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //llamado cuando el usuario presiona search button
                if (!TextUtils.isEmpty(query)){
                    searchPosts(query);
                }
                else{
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //llamado cuando el usuario introduce cualquier tecla
                if (!TextUtils.isEmpty(newText)){
                    searchPosts(newText);
                }
                else{
                    loadPosts();
                }
                return false;
            }
        });
*/
        super.onCreateOptionsMenu(menu, inflater);
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
        if(id == R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


}