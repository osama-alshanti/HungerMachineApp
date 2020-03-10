package com.example.osamashanti.androidorderfoods;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.osamashanti.androidorderfoods.Common.Common;
import com.example.osamashanti.androidorderfoods.Database.Database;
import com.example.osamashanti.androidorderfoods.Interface.ItemClickListener;
import com.example.osamashanti.androidorderfoods.Model.Food;
import com.example.osamashanti.androidorderfoods.Model.Order;
import com.example.osamashanti.androidorderfoods.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database ;
    DatabaseReference foodList;
    String categoryId = "";


    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    //search Bar
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList=new ArrayList<>();
    MaterialSearchBar materialSearchBar;
    //Favorites
    Database localDB;

    //CallbackManager callbackManager;
    //ShareDialog shareDialog;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/of.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_food_list);

        localDB = new Database(this);

        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Restaurants").child(Common.restaurantSelected).child("detail").child("Foods");

        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");

                if(!categoryId.isEmpty() && categoryId !=null){

                    if(Common.isConnectedToInternet(getBaseContext())){

                        loadListFood(categoryId);
                    }else{
                        Toast.makeText(FoodList.this, "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }

            }
        });
        //Default , load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                if(getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");

                if(!categoryId.isEmpty() && categoryId !=null){

                    if(Common.isConnectedToInternet(getBaseContext())){

                        loadListFood(categoryId);
                    }else{
                        Toast.makeText(FoodList.this, "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }

            }
        });


        //Need category Id when User Click to Menu
        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");

        if(!categoryId.isEmpty() && categoryId !=null){

            if(Common.isConnectedToInternet(getBaseContext())){

                loadListFood(categoryId);
            }else{
                Toast.makeText(FoodList.this, "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        //Search Food

        materialSearchBar=(MaterialSearchBar)findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your food");
        // materialSearchBar.setSpeechMode(false);
        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                List<String>suggest=new ArrayList<String>();
                for (String search:suggestList) {

                        if (search.toLowerCase().contains( materialSearchBar.getText().toLowerCase() )) {
                            suggest.add(search);
                        }

                }

                materialSearchBar.setLastSuggestions(suggest);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        materialSearchBar.setOnSearchActionListener( new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //when Search Bar is close
                //Restore original adapter
                if (!enabled)
                    recyclerView.setAdapter(searchAdapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //when search finish
                //show ruselt of search adapter
                startSearch(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {
                //startActivity(new Intent(FoodList.this,FoodList.class));

            }
        } );



    }

    private void startSearch(CharSequence text) {

        Query listFoodCategoryId = foodList.orderByChild("name").equalTo(text.toString());

        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(listFoodCategoryId,Food.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position, @NonNull Food model) {

                viewHolder.food_name.setText(model.getName());
                Picasso.with(getApplicationContext()).load(model.getImage()).into(viewHolder.food_image);


                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {


                        Intent foodDetale = new Intent(FoodList.this,FoodDetail.class);

                        foodDetale.putExtra("foodId",searchAdapter.getRef(position).getKey());
                        startActivity(foodDetale);
                    }
                });

            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(itemview);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(searchAdapter);

/*
                searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,R.layout.food_item,FoodViewHolder.class
                ,foodList.orderByChild("name").equalTo(text.toString())) { //like > Select * from Foods where MenuId =
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {

                viewHolder.food_name.setText(model.getName());
                //Picasso.get().load(model.getImage()).into(viewHolder.food_image);
                Picasso.get().load(model.getImage()).into(viewHolder.food_image);


                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();


                        Intent foodDetale = new Intent(FoodList.this,FoodDetail.class);

                        foodDetale.putExtra("foodId",searchAdapter.getRef(position).getKey());


                        startActivity(foodDetale);

                    }
                });
            }
        };
        */
    }

    private void loadSuggest() {
        foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            Food item=postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName()); //add name of food to suggest list
                        }
                        materialSearchBar.setLastSuggestions(suggestList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                } );
    }

    private void loadListFood(String categoryId) {

        Query listFoodCategoryId = foodList.orderByChild("menuId").equalTo(categoryId);

        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(listFoodCategoryId,Food.class)
                .build();


        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(itemview);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder viewHolder, final int position, @NonNull final Food model) {

                viewHolder.food_name.setText(model.getName());
                Picasso.with(getApplicationContext()).load(model.getImage()).into(viewHolder.food_image);
                viewHolder.food_price.setText(String.format("$ %s",model.getPrice().toString()));
                viewHolder.btn_quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Database(getBaseContext()).addToCart(new Order(adapter.getRef(position).getKey(),
                                model.getName(),
                                "1",
                                model.getPrice(),
                                model.getDiscount(),
                                model.getImage()));
                        Toast.makeText(FoodList.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                    }
                });

                //Add Favorites
                if(localDB.isFavorites(adapter.getRef(position).getKey())){
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                }
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!localDB.isFavorites(adapter.getRef(position).getKey())){
                            localDB.addToFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+" Was added to Favorites", Toast.LENGTH_SHORT).show();
                        }else{
                            localDB.reomoveFromFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+" Was removed from Favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();


                        Intent foodDetale = new Intent(FoodList.this,FoodDetail.class);

                        foodDetale.putExtra("foodId",adapter.getRef(position).getKey());


                        startActivity(foodDetale);




                    }
                });



            }
        };
        adapter.startListening();

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);


/*
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,R.layout.food_item,FoodViewHolder.class
                ,foodList.orderByChild("menuId").equalTo(categoryId)) { //like > Select * from Foods where MenuId =
            @Override
            protected void populateViewHolder(final FoodViewHolder viewHolder, final Food model, final int position) {

                viewHolder.food_name.setText(model.getName());
                Picasso.get().load(model.getImage()).into(viewHolder.food_image);
                viewHolder.food_price.setText(String.format("$ %s",model.getPrice().toString()));
                viewHolder.btn_quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Database(getBaseContext()).addToCart(new Order(adapter.getRef(position).getKey(),
                                model.getName(),
                                "1",
                                model.getPrice(),
                                model.getDiscount(),
                                model.getImage()));
                        Toast.makeText(FoodList.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                    }
                });

                //Add Favorites
                if(localDB.isFavorites(adapter.getRef(position).getKey())){
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                }
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!localDB.isFavorites(adapter.getRef(position).getKey())){
                            localDB.addToFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+" Was added to Favorites", Toast.LENGTH_SHORT).show();
                        }else{
                            localDB.reomoveFromFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+" Was removed from Favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();


                        Intent foodDetale = new Intent(FoodList.this,FoodDetail.class);

                        foodDetale.putExtra("foodId",adapter.getRef(position).getKey());


                        startActivity(foodDetale);

                        
                       

                    }
                });



            }
        };
        */

//        Log.d("TAG",""+adapter.getItemCount());


    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //fix click back on Fooddetail and get no item in Food List
        if(adapter != null){
            adapter.startListening();

        }
    }
}
