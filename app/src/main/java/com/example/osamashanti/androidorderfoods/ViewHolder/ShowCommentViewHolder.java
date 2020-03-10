package com.example.osamashanti.androidorderfoods.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import com.example.osamashanti.androidorderfoods.R;

public class ShowCommentViewHolder extends  RecyclerView.ViewHolder {

    public TextView tv_phone,tv_comment;
    public RatingBar ratingBar;

    public ShowCommentViewHolder(View itemView) {
        super( itemView );

        tv_comment= itemView.findViewById( R.id.tv_comment);
        tv_phone= itemView.findViewById( R.id.tv_phone);
        ratingBar=itemView.findViewById( R.id.ratingBar);
    }
}
