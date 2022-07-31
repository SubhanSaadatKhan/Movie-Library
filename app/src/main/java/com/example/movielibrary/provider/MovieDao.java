package com.example.movielibrary.provider;

import android.content.ClipData;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MovieDao {
    @Query("select * from movieTable")
    LiveData<List<Movie>> getAllMovie();

//    @Query("select * from movieTable where year=:cost")
//    List<Movie> getSearchM(int cost);

    @Insert
    void addMovie(Movie movie);

    @Query("delete from movieTable where year= :yearB")
    void deleteByYear(int yearB);

    @Query("delete FROM movieTable")
    void deleteAllMovies();
}
