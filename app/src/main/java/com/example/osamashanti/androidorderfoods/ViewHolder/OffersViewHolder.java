package com.example.osamashanti.androidorderfoods.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.osamashanti.androidorderfoods.Interface.ItemClickListener;
import com.example.osamashanti.androidorderfoods.R;

public class OffersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView tv_min_buy,tv_delivery_price,tv_title,tv_type;
    public ImageView imageOffers;

    private ItemClickListener itemClickListener;

    public OffersViewHolder(View itemView) {
        super(itemView);

        tv_min_buy = itemView.findViewById(R.id.tv_min_buy);
        tv_delivery_price = itemView.findViewById(R.id.tv_delivery_price);
        tv_title = itemView.findViewById(R.id.tv_title);
        tv_type = itemView.findViewById(R.id.tv_type);

        imageOffers = itemView.findViewById(R.id.offers_image);

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
