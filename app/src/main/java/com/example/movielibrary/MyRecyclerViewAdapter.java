package com.example.movielibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movielibrary.provider.Movie;
import com.example.movielibrary.provider.MovieViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

//    ArrayList<Movie> data;
    List<Movie> data = new ArrayList<Movie>();
//    static MovieViewModel mMovieViewModel;

    public void setData(List<Movie> data) {
        this.data = data;
    }

    public MyRecyclerViewAdapter(){
    }

    @NonNull
    @Override
    public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false); //CardView inflated as RecyclerView list item
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.itemTitle.setText(data.get(position).getTitle());
        holder.itemYear.setText(Integer.toString(data.get(position).getYear()));
        holder.itemCountry.setText(data.get(position).getCountry());
        holder.itemGenre.setText(data.get(position).getGenre());
        holder.itemCost.setText(Integer.toString(data.get(position).getCost()));
        holder.itemKeyword.setText(data.get(position).getKeywords());

        final int yearDelete = data.get(position).getYear();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                  MainActivity2.mMovieViewModel.deleteByYear(yearDelete);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public TextView itemTitle;
        public TextView itemYear;
        public TextView itemCountry;
        public TextView itemGenre;
        public TextView itemCost;
        public TextView itemKeyword;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemTitle = itemView.findViewById(R.id.edittextView15);
            itemYear = itemView.findViewById(R.id.edittextView16);
            itemCountry = itemView.findViewById(R.id.edittextView17);
            itemGenre = itemView.findViewById(R.id.edittextView18);
            itemCost = itemView.findViewById(R.id.edittextView19);
            itemKeyword = itemView.findViewById(R.id.edittextView20);
        }
    }
}
