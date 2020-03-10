package com.example.osamashanti.androidorderfoods;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.osamashanti.androidorderfoods.Common.Common;
import com.example.osamashanti.androidorderfoods.Model.Food;
import com.example.osamashanti.androidorderfoods.Model.Rating;
import com.example.osamashanti.androidorderfoods.ViewHolder.FoodViewHolder;
import com.example.osamashanti.androidorderfoods.ViewHolder.ShowCommentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ShowComment extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference ratingTbl;
    SwipeRefreshLayout mSwipeRefreshLayout;
    FirebaseRecyclerAdapter<Rating,ShowCommentViewHolder> adapter;

    String foodId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_show_comment);

        database=FirebaseDatabase.getInstance();
        ratingTbl=database.getReference("Rating");
        recyclerView = findViewById(R.id.recyclerComment);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mSwipeRefreshLayout=findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (getIntent()!=null)
                    foodId=getIntent().getStringExtra( Common.INTENT_FOOD_ID);
                if (!foodId.isEmpty() && foodId!=null) {

                    Query listFoodCategoryId = ratingTbl.orderByChild("foodId").equalTo(foodId);

                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(listFoodCategoryId,Rating.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowCommentViewHolder viewHolder, int position, @NonNull Rating model) {
                            viewHolder.tv_phone.setText(model.getUserPhone());
                            viewHolder.tv_comment.setText(model.getComment());
                            viewHolder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                        }

                        @NonNull
                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                            return new ShowCommentViewHolder(itemview);
                        }
                    };

                   /* adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(Rating.class,R.layout.comment_layout,ShowCommentViewHolder.class
                            ,ratingTbl.orderByChild("foodId").equalTo(foodId)) {

                        @Override
                        protected void populateViewHolder(ShowCommentViewHolder viewHolder, Rating model, int position) {
                            viewHolder.tv_phone.setText(model.getUserPhone());
                            viewHolder.tv_comment.setText(model.getComment());
                            viewHolder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                        }
                    };*/
                    loadComment(foodId);
                    adapter.startListening();
                }
            }
        });
        mSwipeRefreshLayout.post( new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                if (getIntent()!=null)
                    foodId=getIntent().getStringExtra( Common.INTENT_FOOD_ID);
                if (!foodId.isEmpty() && foodId!=null) {

                    Query listFoodCategoryId = ratingTbl.orderByChild("foodId").equalTo(foodId);

                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(listFoodCategoryId,Rating.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowCommentViewHolder viewHolder, int position, @NonNull Rating model) {
                            viewHolder.tv_phone.setText(model.getUserPhone());
                            viewHolder.tv_comment.setText(model.getComment());
                            viewHolder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                        }

                        @NonNull
                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                            return new ShowCommentViewHolder(itemview);
                        }
                    };
                    loadComment(foodId);
                    adapter.startListening();
                }

            }
        } );







    }

    private void loadComment(String foodId) {
        adapter.startListening();
        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
