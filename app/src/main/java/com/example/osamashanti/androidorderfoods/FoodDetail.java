package com.example.osamashanti.androidorderfoods;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.osamashanti.androidorderfoods.Common.Common;
import com.example.osamashanti.androidorderfoods.Database.Database;
import com.example.osamashanti.androidorderfoods.Model.Category;
import com.example.osamashanti.androidorderfoods.Model.Food;
import com.example.osamashanti.androidorderfoods.Model.Order;
import com.example.osamashanti.androidorderfoods.Model.Rating;
import com.example.osamashanti.androidorderfoods.ViewHolder.MenuViewHolder;
import com.example.osamashanti.androidorderfoods.ViewHolder.ShowCommentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    TextView food_name , food_price , food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    CounterFab btnCart;
    ElegantNumberButton numberButton;
    FloatingActionButton btn_rating;
    RatingBar ratingBar;
    FButton btnShowComment;

    String foodId ="";

    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratings;

    Food currentFood ;

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
        setContentView(R.layout.activity_food_detail);

        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Restaurants").child(Common.restaurantSelected).child("detail").child("Foods");
        ratings =database.getReference("Rating");

        numberButton = findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btnCart);
        btn_rating = findViewById(R.id.btn_rating);
        ratingBar = findViewById(R.id.ratingBar);
        btnShowComment = findViewById(R.id.btnShowComment);


        btn_rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRating();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database(getBaseContext()).addToCart(new Order(foodId,currentFood.getName(),numberButton.getNumber(),
                        currentFood.getPrice(),currentFood.getDiscount(),currentFood.getImage()));
                Toast.makeText(FoodDetail.this, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });
        btnCart.setCount(new Database(this).getCountCart());

        food_name = findViewById(R.id.food_name);
        food_image = findViewById(R.id.img_food);
        food_price = findViewById(R.id.food_price);
        food_description = findViewById(R.id.food_description);
       // collapsingToolbarLayout = findViewById(R.id.collapsing);
      //  collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
      //  collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);



        if(getIntent() != null )
            foodId = getIntent().getStringExtra("foodId");

        if (foodId != null && !foodId.isEmpty()){
            if(Common.isConnectedToInternet(getBaseContext())){
                getDetailFood(foodId);
                getRatingFood(foodId);
            }else{
                Toast.makeText(FoodDetail.this, "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        btnShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FoodDetail.this,ShowComment.class);
                intent.putExtra(Common.INTENT_FOOD_ID,foodId);
                startActivity(intent);
            }
        });
    }


    private void getRatingFood(String foodId) {//upload our rating to fireBase
        Query foodRating = ratings.orderByChild("foodId").equalTo(foodId); //When use orderByChild must add indexOn in firebase
        foodRating.addValueEventListener(new ValueEventListener() {
            int count=0,sum=0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()){

                    Rating item = postSnapShot.getValue(Rating.class);
                    sum+= Integer.parseInt(item.getRateValue());
                    count++;

                }
                if(count !=0){
                    float average = sum/count;
                    ratingBar.setRating(average);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRating() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here ...")
                .setHintTextColor(R.color.colorPrimary)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnimation)
                .create(FoodDetail.this)
                .show();
    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);


                //Picasso.get().load(currentFood.getImage()).into(food_image);
                Picasso.with(getApplicationContext()).load(currentFood.getImage()).into(food_image);


                food_price.setText(currentFood.getPrice());

                food_name.setText(currentFood.getName());

                food_description.setText(currentFood.getDescription());





            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, String comments) {

        final Rating rating = new Rating(Common.currentUser.getPhone(),foodId,String.valueOf(value),comments);
        ratings.push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FoodDetail.this, "Thank you for submit rating!", Toast.LENGTH_SHORT).show();
                    }
                });

        /*ratings.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(Common.currentUser.getPhone()).exists()){
                    //Remove old value
                    ratings.child(Common.currentUser.getPhone()).removeValue();
                    //Update new value
                    ratings.child(Common.currentUser.getPhone()).setValue(rating);

                }else{
                    ratings.child(Common.currentUser.getPhone()).setValue(rating);

                }
                Toast.makeText(FoodDetail.this, "Thank you for submit rating!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
*/

    }
}
