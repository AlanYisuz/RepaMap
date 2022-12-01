package com.example.reppmap.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reppmap.AddPostActivity;
import com.example.reppmap.R;
import com.example.reppmap.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.zip.DataFormatException;
import java.lang.String;
import java.text.DateFormat;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {

    Context context;
    List<ModelPost> postList;

    String myUid;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //obtener data
        final String uid  = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        String pId = postList.get(position).getpId();
        String pTitle = postList.get(position).getpTitle();
        String pDescription = postList.get(position).getpDesrc();
        String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();

        //convertir timestamp a dd/mm/yy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = format1.format(calendar.getTime());
        //String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar);

        //set data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);

        //set user dp
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_image).into(holder.uPictureIv);
        }
        catch(Exception e){

        }

        //set post
        //si no hay imagen i.e pImage.equals("noImage).
        if(pImage.equals("noImage")){
            //esconde imageview
            holder.pImageIv.setVisibility(View.GONE);
        }else {
            //muestra imageview
            holder.pImageIv.setVisibility(View.VISIBLE);
            try{
                Picasso.get().load(pImage).into(holder.pImageIv);
            }
            catch(Exception e){

            }
        }


        //handle button click

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, uid, myUid, pId, pImage);
            }
        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will
                Toast.makeText(context, "Like", Toast.LENGTH_SHORT).show();
            }
        });
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will
                Toast.makeText(context, "Comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        //creando un popuo menu que tiene la opcion de eliminar
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //mostrar opcion de eliminar en solo los posts del usuario actual
        if(uid.equals(myUid)){
            //agregar items al menu
            popupMenu.getMenu().add(Menu.NONE, 0,0,"Eliminar");
            popupMenu.getMenu().add(Menu.NONE, 1,0,"Editar");
        }

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id==0){
                    //eliminar si hizo click
                    beginDelete(pId, pImage);
                }
                else if(id==1){
                    //editar si hizo click
                    //empieza AddpostActity con una llave "editpost" y la id del post clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        //post pueden ser con o sin imagen
        if(pImage.equals("noImage")){
            //post es sin imagen
            deleteWithoutImage(pId);
        }else{
            //post es con imagen
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithoutImage(String pId) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Eliminando...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue(); // remueve valores de firebase donde pid coincida
                }
                //eliminado
                Toast.makeText(context, "Eliminado exitosamente", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteWithImage(String pId, String pImage) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Eliminando...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds: snapshot.getChildren()){
                                    ds.getRef().removeValue(); // remueve valores de firebase donde pid coincida
                                }
                                //eliminado
                                Toast.makeText(context, "Eliminado exitosamente", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //views de row_post_xml
        ImageView uPictureIv, pImageIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv;
        ImageButton moreBtn;
        Button likeBtn, commentBtn;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //views init
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
        }
    }
}
