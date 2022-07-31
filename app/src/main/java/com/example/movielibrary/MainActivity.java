package com.example.movielibrary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movielibrary.provider.Movie;
import com.example.movielibrary.provider.MovieViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {
    DatabaseReference myRef;
    DrawerLayout drawer;
    String title;
    String genre;
    ArrayList<String> myList = new ArrayList<String>();
//    ArrayList<Movie> data = new ArrayList<>();

    //new code
    private MovieViewModel mMovieViewModel;
    MyRecyclerViewAdapter adapter;

    ArrayAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Items/movie");

//        myRef.setValue("Hello, World!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer);

        adapter = new MyRecyclerViewAdapter();

        //new code
        mMovieViewModel=new ViewModelProvider(this).get(MovieViewModel.class);
        mMovieViewModel.getAllMovies().observe(this, newData -> {
            adapter.setData(newData);
            adapter.notifyDataSetChanged();
//            TextView myText = findViewById(R.id.textViewCount);
//            myText.setText("Data count: " + newData.size());
        });

        //setting a Toolbar to act as an Actionbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setting up a listview
        ListView listView = findViewById(R.id.lv);
        //adapter will help store text inside the list by using containers
        myAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,myList);
        listView.setAdapter(myAdapter);

        //creating a HamBurger symbol to display navigation menu
        drawer = findViewById(R.id.dl);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //setting up a navigation menu and listening to the events on it
        NavigationView navigationView = findViewById(R.id.nv);
        navigationView.setNavigationItemSelectedListener(new MyNavigationListener());

        //setting up a navigation menu and creating an onClick listener that will save the movie when clicked
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMovie();
                add();
            }

        });

        /* Request permissions to access SMS */
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, 0);

        IntentFilter intentFilter = new IntentFilter("SMS_FILTER");
        registerReceiver(myReceiver,intentFilter);
//        registerReceiver(myReceiver,intentFilter);
    }
    //setting up a options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    //when user presses an item inside option menu it will reset fields.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.clearfields) {
            resetText();
        }else if (id == R.id.totalmovies) {
            int val = myList.size(); //returns the size of the list
            Toast.makeText(getApplicationContext(), "Total Movies in the list are " + val, Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
    //implementing navigation menu listener that will react differently on every item clicked
    class MyNavigationListener implements NavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // get the id of the selected item
            int id = item.getItemId();

            if (id == R.id.addMovie) {
                saveMovie();
                add();
            } else if (id == R.id.removeLasrMovie) {
                myList.remove(myList.size()-1); //removing last item from the array list
                myAdapter.notifyDataSetChanged(); //updating the adaptor
            } else if (id == R.id.removeAllMovies) {
                deleteAll();
                myList.clear();//removing all items from the array list
                adapter.notifyDataSetChanged(); //updating the adaptor
                myAdapter.notifyDataSetChanged();

            } else if (id == R.id.closeapp) {
                finish(); //closes the app
            } else if (id == R.id.listallmovies) {
                goToNextActivity();
            }
                // close the drawer
                drawer.closeDrawers();
                // tell the OS
                return true;


        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences myData = getSharedPreferences("f2",0);

        //getting the respective value using its key and storing in a variable.
        String title = myData.getString("titleKey"," ");
        String year = myData.getString("yearKey"," ");
        String country = myData.getString("countryKey"," ");
        String genre = myData.getString("genreKey"," ");
        String cost = myData.getString("costKey", " ");
        String keyword = myData.getString("keywordKey"," ");

//        //grabbing the plain texts using their ids
        EditText inputVal1 = findViewById(R.id.planeText);
        EditText inputVal2 = findViewById(R.id.planeYear);
        EditText inputVal3 = findViewById(R.id.planeCountry);
        EditText inputVal4 = findViewById(R.id.planeGenre);
        EditText inputVal5 = findViewById(R.id.costTxt);
        EditText inputVal6 = findViewById(R.id.planeKeyword);

//        //setting the saved value back into their respective plain texts.
        inputVal1.setText(title);
        inputVal2.setText(year);
        inputVal3.setText(country);
        inputVal4.setText(genre);
        inputVal5.setText(cost);
        inputVal6.setText(keyword);
    }

    public void saveMovie(View view){

        //grabbing the plain texts using their ids
        EditText inputVal1 = findViewById(R.id.planeText);
        EditText inputVal2 = findViewById(R.id.planeYear);
        EditText inputVal3 = findViewById(R.id.planeCountry);
        EditText inputVal4 = findViewById(R.id.planeGenre);
        EditText inputVal5 = findViewById(R.id.costTxt);
        EditText inputVal6 = findViewById(R.id.planeKeyword);

        //storing the input values into variable
        String sTitle = inputVal1.getText().toString();
        String sYear = inputVal2.getText().toString();
        String sCountry = inputVal3.getText().toString();
        String sGenre = inputVal4.getText().toString();
        String sCost = inputVal5.getText().toString();
        String sKeyword = inputVal6.getText().toString();

        SharedPreferences myData = getSharedPreferences("f2",0); //creating SharedPrefrence instance and setting file name
        SharedPreferences.Editor myEditor = myData.edit();

        //saving the key/value pairs into SharedPrefrences
        myEditor.putString("titleKey",sTitle);
        myEditor.putString("yearKey",sYear);
        myEditor.putString("countryKey",sCountry);
        myEditor.putString("genreKey",sGenre);
        myEditor.putString("costKey",sCost);
        myEditor.putString("keywordKey",sKeyword);

        // Save the changes in SharedPreferences
        myEditor.commit(); // commit changes

        //Toast message
        Toast myMessage = Toast.makeText(this,"Movie" + " - " + sTitle + " - " + " has been added",Toast.LENGTH_SHORT );
        myMessage.show();
    }

    public void doubleCost(View view){
        SharedPreferences myData = getSharedPreferences("f2",0);
        SharedPreferences.Editor myEditor = myData.edit();

        String cost = myData.getString("costKey", " ");
        String costX2 = Integer.toString((Integer.parseInt(cost))*2);

        EditText inputVal5 = findViewById(R.id.costTxt);
        inputVal5.setText(costX2);

        myEditor.putString("costKey",costX2);

        myEditor.commit();

    }

    public void clearAll(View view){
        SharedPreferences myData = getSharedPreferences("f2",0);
        SharedPreferences.Editor myEditor = myData.edit();

        myEditor.clear();
        myEditor.commit(); // commit changes
    }

    public void resetText(View view){

        //grabbing the plain texts using their ids
        EditText inputVal1 = findViewById(R.id.planeText);
        EditText inputVal2 = findViewById(R.id.planeYear);
        EditText inputVal3 = findViewById(R.id.planeCountry);
        EditText inputVal4 = findViewById(R.id.planeGenre);
        EditText inputVal5 = findViewById(R.id.costTxt);
        EditText inputVal6 = findViewById(R.id.planeKeyword);

        //setting empty string into the field.
        inputVal1.setText("");
        inputVal2.setText("");
        inputVal3.setText("");
        inputVal4.setText("");
        inputVal5.setText("");
        inputVal6.setText("");
    }

    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
        return super.registerReceiver(receiver, filter);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //storing the input tile and genre in a instance variable
        EditText inputVal1 = findViewById(R.id.planeText);
        title = inputVal1.getText().toString();

        EditText inputVal4 = findViewById(R.id.planeGenre);
        genre = inputVal4.getText().toString();

        //saving the title and genre into the Bundle
        outState.putString("title1",title);
        outState.putString("genre1",genre.toLowerCase()); //genre changed to all lower cases when saved

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        EditText inputVal1 = findViewById(R.id.planeText);
        title = savedInstanceState.getString("title1").toUpperCase(); //title changed to all upper cases when restored
        inputVal1.setText(title);//setting updated title back into the field

        EditText inputVal4 = findViewById(R.id.planeGenre);
        genre = savedInstanceState.getString("genre1");
        inputVal4.setText(genre);//setting updated genre back into the field
    }

    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EditText inputVal1 = findViewById(R.id.planeText);
            EditText inputVal2 = findViewById(R.id.planeYear);
            EditText inputVal3 = findViewById(R.id.planeCountry);
            EditText inputVal4 = findViewById(R.id.planeGenre);
            EditText inputVal5 = findViewById(R.id.costTxt);
            EditText inputVal6 = findViewById(R.id.planeKeyword);

            StringTokenizer st = new StringTokenizer(intent.getStringExtra("SMS_MSG_KEY"),";");
            inputVal1.setText(st.nextToken());
            inputVal2.setText(st.nextToken());
            inputVal3.setText(st.nextToken());
            inputVal4.setText(st.nextToken());
            String val = st.nextToken();
            inputVal6.setText(st.nextToken());
            String val2 = st.nextToken();
            int finalVal = Integer.parseInt(val) + Integer.parseInt(val2);
            inputVal5.setText(Integer.toString(finalVal));

        }
    };
    //Overloading the saveMovie method
    public void saveMovie(){

        //grabbing the plain texts using their ids
        EditText inputVal1 = findViewById(R.id.planeText);
        EditText inputVal2 = findViewById(R.id.planeYear);
        EditText inputVal3 = findViewById(R.id.planeCountry);
        EditText inputVal4 = findViewById(R.id.planeGenre);
        EditText inputVal5 = findViewById(R.id.costTxt);
        EditText inputVal6 = findViewById(R.id.planeKeyword);

        //storing the input values into variable
        String sTitle = inputVal1.getText().toString();
        String sYear = inputVal2.getText().toString();
        String sCountry = inputVal3.getText().toString();
        String sGenre = inputVal4.getText().toString();
        String sCost = inputVal5.getText().toString();
        String sKeyword = inputVal6.getText().toString();

        SharedPreferences myData = getSharedPreferences("f2",0); //creating SharedPrefrence instance and setting file name
        SharedPreferences.Editor myEditor = myData.edit();

        //saving the key/value pairs into SharedPrefrences
        myEditor.putString("titleKey",sTitle);
        myEditor.putString("yearKey",sYear);
        myEditor.putString("countryKey",sCountry);
        myEditor.putString("genreKey",sGenre);
        myEditor.putString("costKey",sCost);
        myEditor.putString("keywordKey",sKeyword);

        // Save the changes in SharedPreferences
        myEditor.commit(); // commit changes

        //Toast message
        Toast myMessage = Toast.makeText(this,"Movie" + " - " + sTitle + " - " + " has been added",Toast.LENGTH_SHORT );
        myMessage.show();

        myList.add(sTitle + " | " + sYear); //adding title and year to the array list
        myAdapter.notifyDataSetChanged(); //updating the adapter
    }
//    Overloading the resetText method
    public void resetText(){

        //grabbing the plain texts using their ids
        EditText inputVal1 = findViewById(R.id.planeText);
        EditText inputVal2 = findViewById(R.id.planeYear);
        EditText inputVal3 = findViewById(R.id.planeCountry);
        EditText inputVal4 = findViewById(R.id.planeGenre);
        EditText inputVal5 = findViewById(R.id.costTxt);
        EditText inputVal6 = findViewById(R.id.planeKeyword);

        //setting empty string into the field.
        inputVal1.setText("");
        inputVal2.setText("");
        inputVal3.setText("");
        inputVal4.setText("");
        inputVal5.setText("");
        inputVal6.setText("");
    }

    public void add(){
        //grabbing the plain texts using their ids
        EditText inputVal1 = findViewById(R.id.planeText);
        EditText inputVal2 = findViewById(R.id.planeYear);
        EditText inputVal3 = findViewById(R.id.planeCountry);
        EditText inputVal4 = findViewById(R.id.planeGenre);
        EditText inputVal5 = findViewById(R.id.costTxt);
        EditText inputVal6 = findViewById(R.id.planeKeyword);

        //storing the input values into variable
        String sTitle = inputVal1.getText().toString();
        String sYear = inputVal2.getText().toString();
        String sCountry = inputVal3.getText().toString();
        String sGenre = inputVal4.getText().toString();
        String sCost = inputVal5.getText().toString();
        String sKeyword = inputVal6.getText().toString();

        int reYear = Integer.parseInt(sYear); //changed
        int reCost = Integer.parseInt(sCost); //changed

        Movie movie = new Movie(sTitle,reYear,sCountry,sGenre,reCost,sKeyword); //changed

        mMovieViewModel.insert(movie);

        //storing data to firebase
        myRef.push().setValue(movie);
//        data.add(movie);

    }
    public void deleteAll(){

        mMovieViewModel.deleteAll();
        myRef.removeValue();
    }
    public void goToNextActivity(View v){
//        Gson gson = new Gson();
//        String dbStr = gson.toJson(data);
//        SharedPreferences sP = getSharedPreferences("db1",0);
//        SharedPreferences.Editor edit = sP.edit();
//        edit.putString("MOV_LIST",dbStr);
//        edit.apply();

        Intent intent = new Intent(this,MainActivity2.class);
        startActivity(intent);
    }

    public void goToNextActivity(){
//        Gson gson = new Gson();
//        String dbStr = gson.toJson(data);
//        SharedPreferences sP = getSharedPreferences("db1",0);
//        SharedPreferences.Editor edit = sP.edit();
//        edit.putString("MOV_LIST",dbStr);
//        edit.apply();
        Intent intent = new Intent(this,MainActivity2.class);
        startActivity(intent);
    }
}