package com.example.osamashanti.androidorderfoods;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.example.osamashanti.androidorderfoods.Common.Common;
import com.example.osamashanti.androidorderfoods.Database.Database;
import com.example.osamashanti.androidorderfoods.Interface.ItemClickListener;
import com.example.osamashanti.androidorderfoods.Model.Banner;
import com.example.osamashanti.androidorderfoods.Model.Category;
import com.example.osamashanti.androidorderfoods.Model.Food;
import com.example.osamashanti.androidorderfoods.Service.ListenOrder;
import com.example.osamashanti.androidorderfoods.ViewHolder.FoodViewHolder;
import com.example.osamashanti.androidorderfoods.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference category;
    private long backPressedTime;

    TextView txtFullName;
    RecyclerView recyclerViewMenu;
    RecyclerView.LayoutManager layoutManager;

    SwipeRefreshLayout swipeRefreshLayout;
    CounterFab fab;

    HashMap<String,String> image_list;
    SliderLayout slider;

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
        setContentView(R.layout.activity_home);

        Intent service = new Intent(Home.this, ListenOrder.class);
        startService(service);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);



        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadMenu();
                } else {
                    Toast.makeText(getBaseContext(), "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
            //Default , load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadMenu();
                } else {
                    Toast.makeText(getBaseContext(), "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


        database = FirebaseDatabase.getInstance();
        category = database.getReference("Restaurants").child(Common.restaurantSelected).child("detail").child("Category");
        //must direct down the reference ...


        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category,Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {
                viewHolder.txtMenuName.setText(model.getName());
                //Picasso.get().load(model.getImage()).into(viewHolder.imageView);
                Picasso.with(getApplicationContext()).load(model.getImage()).into(viewHolder.imageView);
                //Toast.makeText(Home.this, ""+model.getImage(), Toast.LENGTH_SHORT).show();

                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Get Category Id and Send To New Acticity
                        Intent foodList = new Intent(Home.this, FoodList.class);

                        foodList.putExtra("CategoryId", adapter.getRef(position).getKey());
                        startActivity(foodList);
                    }
                });
            }
            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item,parent,false);
                return new MenuViewHolder(itemview);
            }
        };
        adapter.startListening();


       /* adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class, R.layout.menu_item, MenuViewHolder.class, category) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {
                viewHolder.txtMenuName.setText(model.getName());
                //Picasso.get().load(model.getImage()).into(viewHolder.imageView);
                Picasso.get().load(model.getImage()).into(viewHolder.imageView);

                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Get Category Id and Send To New Acticity
                        Intent foodList = new Intent(Home.this, FoodList.class);

                        foodList.putExtra("CategoryId", adapter.getRef(position).getKey());
                        startActivity(foodList);

                    }
                });


            }
        };
*/

        Paper.init(this);


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, Cart.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        fab.setCount(new Database(this).getCountCart());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Set Name For User
        View headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView) headerView.findViewById(R.id.txtFullName);
        txtFullName.setText("Welcome User: " + Common.currentUser.getName());

        //Load menu

        recyclerViewMenu = findViewById(R.id.recycler_menu);
       // recyclerViewMenu.setHasFixedSize(true); for anim
        //layoutManager = new LinearLayoutManager(this); 2 box behind
        //recyclerViewMenu.setLayoutManager(layoutManager);
        recyclerViewMenu.setLayoutManager(new GridLayoutManager(this,2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerViewMenu.getContext(),
                R.anim.layout_fall_down);
        recyclerViewMenu.setLayoutAnimation(controller);

        if (Common.isConnectedToInternet(this)) {

            loadMenu();

        } else {
            Toast.makeText(this, "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
            return;
        }
        //REqister Service

        setupSlider();

    }

    private void setupSlider() {
        slider = findViewById(R.id.slider);
        image_list = new HashMap<>();

        final DatabaseReference banners = database.getReference("Restaurants").child(Common.restaurantSelected).child("detail").child("Banner");
        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot postSnapShot: dataSnapshot.getChildren()){
                    Banner banner = postSnapShot.getValue(Banner.class);
                    //PIZZA@@@01
                        image_list.put(banner.getName()+"@@@"+banner.getId(),banner.getImage());

                }
                for (String key:image_list.keySet()){

                    String[] keySplit = key.split("@@@");
                    String nameOfFood = keySplit[0];
                    String idOfFood = keySplit[1];

                    //Create Slider
                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView
                            .description(nameOfFood)
                            .image(image_list.get(key))//path
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent = new Intent(Home.this,OffersActivity.class);
                                    //intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);

                                }
                            });
                    //Add Extra bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("foodId",idOfFood);
                    slider.addSlider(textSliderView);

                    banners.removeEventListener(this);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        slider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        slider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        slider.setCustomAnimation(new DescriptionAnimation());
        slider.setDuration(4000);

    }

    private void loadMenu() {
       /* adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class, R.layout.menu_item, MenuViewHolder.class, category) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.get().load(model.getImage()).into(viewHolder.imageView);

                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Get Category Id and Send To New Acticity
                        Intent foodList = new Intent(Home.this, FoodList.class);

                        foodList.putExtra("CategoryId", adapter.getRef(position).getKey());
                        startActivity(foodList);

                    }
                });


            }
        };
*/
        recyclerViewMenu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

        recyclerViewMenu.getAdapter().notifyDataSetChanged();
        recyclerViewMenu.scheduleLayoutAnimation();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if (backPressedTime + 1000 > System.currentTimeMillis()) {

                super.onBackPressed();
                return;
            }

            backPressedTime = System.currentTimeMillis();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

         if (id == R.id.menu_search) {
            startActivity(new Intent(Home.this, searchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_cart) {

            Intent cartIntent = new Intent(Home.this, Cart.class);
            startActivity(cartIntent);

        } else if (id == R.id.nav_orders) {

            Intent orderIntent = new Intent(Home.this, OrderStatus.class);
            startActivity(orderIntent);

        }else if(id == R.id.nav_offers){
            startActivity(new Intent(Home.this,OffersActivity.class));
        }
        else if (id == R.id.nav_log_out) {
            //Delete Remember user and phone

            Paper.book().destroy();

            Intent signIn = new Intent(Home.this, SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);


        } else if (id == R.id.change_pass) {

            showChangePassword();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showChangePassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder.setTitle("CHANGE PASSWORD");
        builder.setMessage("Please enter all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.change_password_layout, null);

        final MaterialEditText editPassword = view.findViewById(R.id.edtPassword);
        final MaterialEditText editNewPassword = view.findViewById(R.id.edtNewPassword);
        final MaterialEditText editRepeatPassword = view.findViewById(R.id.edtRepeatPassword);

        builder.setView(view);
        builder.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                //Change Old Password
                if(editPassword.getText().toString().equals(Common.currentUser.getPassword())){

                    if(editNewPassword.getText().toString().equals(editRepeatPassword.getText().toString())){
                        Map<String,Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("password",editNewPassword.getText().toString()); //match fireBase
                        //Make update
                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                        user.child(Common.currentUser.getPhone())
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>(){
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task){
                                        waitingDialog.dismiss();
                                        Toast.makeText(Home.this, "Password Update", Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                    }else{
                        waitingDialog.dismiss();
                        Toast.makeText(Home.this, "Not Matches", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    waitingDialog.dismiss();
                    Toast.makeText(Home.this, "Wrong old password!", Toast.LENGTH_SHORT).show();
                }


            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        fab.setCount(new Database(this).getCountCart());
        if(adapter !=null){
            adapter.startListening();

        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //adapter.stopListening();
        adapter.stopListening();
        slider.stopAutoCycle();
    }
}
