package com.example.reppmap.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reppmap.R;
import com.example.reppmap.models.ModelComment;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {

    Context context;
    List<ModelComment> commentList;

    public AdapterComments(Context context, List<ModelComment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind el row_comments layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);


        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //obtener  the data
        String iud = commentList.get(position).getUid();
        String name = commentList.get(position).getuName();
        String email = commentList.get(position).getuEmail();
        String image = commentList.get(position).getuDp();
        String cid = commentList.get(position).getcId();
        String comment = commentList.get(position).getComment();
        String timestamp = commentList.get(position).getTimestamp();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = format1.format(calendar.getTime());

        //set la data
        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);
        //set up user dp
        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_default_image).into(holder.avatarIv);
        }catch (Exception e){

        }

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class  MyHolder extends RecyclerView.ViewHolder{

        //declarar view de row_comments
        ImageView avatarIv;
        TextView nameTv, commentTv, timeTv;


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv= itemView.findViewById(R.id.avatarIv);
            nameTv= itemView.findViewById(R.id.nameTv);
            commentTv= itemView.findViewById(R.id.commentTv);
            timeTv= itemView.findViewById(R.id.timeTV);
        }
    }
}
