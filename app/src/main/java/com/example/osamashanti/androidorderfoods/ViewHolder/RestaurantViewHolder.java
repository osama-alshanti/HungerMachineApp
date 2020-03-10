package com.example.osamashanti.androidorderfoods.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.osamashanti.androidorderfoods.Interface.ItemClickListener;
import com.example.osamashanti.androidorderfoods.R;

public class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtRestaurantName;
    public ImageView imageView;
    private ItemClickListener itemClickListener;


    public RestaurantViewHolder(@NonNull View itemView){
        super(itemView);

        txtRestaurantName = itemView.findViewById(R.id.restaurant_name);
        imageView = itemView.findViewById(R.id.restaurant_image);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view,getAdapterPosition(),false);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


}
