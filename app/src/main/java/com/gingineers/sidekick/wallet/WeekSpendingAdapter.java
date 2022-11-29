package com.gingineers.sidekick.wallet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gingineers.sidekick.R;

import java.util.List;

public class WeekSpendingAdapter extends RecyclerView.Adapter<WeekSpendingAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Data> myDataList;

    public WeekSpendingAdapter(Context mContext, List<Data> myDataList) {
        this.mContext = mContext;
        this.myDataList = myDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.retrivewallet_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Data data = myDataList.get(position);

        holder.item.setText("Item: "+data.getItem());
        holder.amount.setText("Amount: ₱ "+data.getAmount());
        holder.date.setText("On "+data.getDate());
        holder.notes.setText("Notes: "+data.getNotes());

        switch (data.getItem()){
            case "Transportation":
                holder.imageView.setImageResource(R.drawable.transportation);
                break;

            case "Food":
                holder.imageView.setImageResource(R.drawable.food);
                break;
            case "House":
                holder.imageView.setImageResource(R.drawable.house);
                break;

            case "Education":
                holder.imageView.setImageResource(R.drawable.education);
                break;

            case "Health":
                holder.imageView.setImageResource(R.drawable.health);
                break;

            case "Personal":
                holder.imageView.setImageResource(R.drawable.personal);
                break;

            case "Apparel":
                holder.imageView.setImageResource(R.drawable.apparel);
                break;

            case "Entertainment":
                holder.imageView.setImageResource(R.drawable.entertainment);
                break;

            case "Online Shopping":
                holder.imageView.setImageResource(R.drawable.shopping);
                break;

            case "Others":
                holder.imageView.setImageResource(R.drawable.others);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return myDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView item, amount, date, notes;
        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            item = itemView.findViewById(R.id.item);
            amount = itemView.findViewById(R.id.amount);
            date = itemView.findViewById(R.id.date);
            notes = itemView.findViewById(R.id.notes);
            imageView = itemView.findViewById(R.id.imageView);


        }
    }

}
