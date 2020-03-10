package com.example.osamashanti.androidorderfoods;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.osamashanti.androidorderfoods.Common.Common;
import com.example.osamashanti.androidorderfoods.Interface.ItemClickListener;
import com.example.osamashanti.androidorderfoods.Model.Food;
import com.example.osamashanti.androidorderfoods.Model.Offers;
import com.example.osamashanti.androidorderfoods.ViewHolder.FoodViewHolder;
import com.example.osamashanti.androidorderfoods.ViewHolder.OffersViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OffersActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference offers;

    RecyclerView recyclerViewOffers;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Offers, OffersViewHolder> adapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_offers);


        database = FirebaseDatabase.getInstance();
        offers = database.getReference("Offers");

        recyclerViewOffers = findViewById(R.id.recycler_offers);
        recyclerViewOffers.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerViewOffers.setLayoutManager(layoutManager);

        if (Common.isConnectedToInternet(this)) {
            loadOffers();
        } else {
            Toast.makeText(this, "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void loadOffers() {

        FirebaseRecyclerOptions<Offers> options = new FirebaseRecyclerOptions.Builder<Offers>()
                .setQuery(offers,Offers.class)
                .build();

//Offers.class,R.layout.offers_item,OffersViewHolder.class,offers
        adapter = new FirebaseRecyclerAdapter<Offers,OffersViewHolder>(options) {
            @NonNull
            @Override
            public OffersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.offers_item,parent,false);
                return new OffersViewHolder(itemview);
            }

            @Override
            protected void onBindViewHolder(@NonNull OffersViewHolder viewHolder, int position, @NonNull Offers model) {
                Picasso.with(getApplicationContext()).load(model.getImage()).into(viewHolder.imageOffers);
                viewHolder.tv_title.setText(model.getTitle());
                viewHolder.tv_type.setText(model.getType());
                viewHolder.tv_min_buy.setText(model.getMin());
                viewHolder.tv_delivery_price.setText(model.getDelivery());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                    }
                });


            }


           /* @Override
            protected void populateViewHolder(OffersViewHolder viewHolder, Offers model, int position) {
                Picasso.get().load(model.getImage()).into(viewHolder.imageOffers);
                viewHolder.tv_title.setText(model.getTitle());
                viewHolder.tv_type.setText(model.getType());
                viewHolder.tv_min_buy.setText(model.getMin());
                viewHolder.tv_delivery_price.setText(model.getDelivery());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                    }
                });


            }*/
        };

        adapter.notifyDataSetChanged();
        recyclerViewOffers.setAdapter(adapter);
        adapter.startListening();

    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
