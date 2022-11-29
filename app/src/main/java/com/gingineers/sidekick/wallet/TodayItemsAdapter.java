package com.gingineers.sidekick.wallet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.gingineers.sidekick.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class TodayItemsAdapter extends RecyclerView.Adapter<TodayItemsAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Data> myDataList;
    private String post_key = "";
    private String item = "";
    private String note = "";
    private int amount = 0;

    public TodayItemsAdapter(Context mContext, List<Data> myDataList) {
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

        holder.itemView.setOnClickListener(v -> {
            post_key = data.getId();
            item = data.getItem();
            amount = data.getAmount();
            note = data.getNotes();
            updateToday();
        });

    }

    private void updateToday() {

        AlertDialog.Builder updateDialog = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View mView = inflater.inflate(R.layout.updatewallet_layout, null);

        updateDialog.setView(mView);
        final AlertDialog dialog = updateDialog.create();

        final TextView mItem = mView.findViewById(R.id.itemNameTV);
        final EditText mAmount = mView.findViewById(R.id.itemAmountET);
        final EditText mNotes = mView.findViewById(R.id.itemNoteET);

        mItem.setText(item);

        mAmount.setText(String.valueOf(amount));
        mAmount.setSelection(String.valueOf(amount).length());

        mNotes.setText(note);
        mNotes.setSelection(note.length());

        Button delBtn = mView.findViewById(R.id.btnDeleteitem);
        Button updateBtn = mView.findViewById(R.id.btnUpdateitem);

        updateBtn.setOnClickListener(v -> {

            amount = Integer.parseInt(mAmount.getText().toString());
            amount = Integer.parseInt(mAmount.getText().toString());
            note = mNotes.getText().toString();

            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Calendar calendar = Calendar.getInstance();
            String date =dateFormat.format(calendar.getTime());

            MutableDateTime epoch = new MutableDateTime();
            epoch.setDate(0);
            DateTime now = new DateTime();
            Weeks weeks = Weeks.weeksBetween(epoch, now);
            Months months = Months.monthsBetween(epoch, now);

            String itemNday = item+date;
            String itemNweek = item+weeks.getWeeks();
            String itemNmonth = item+months.getMonths();

            Data data = new Data(item, date, post_key, itemNday, itemNweek, itemNmonth, amount, months.getMonths(), weeks.getWeeks(), note);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            reference.child(post_key).setValue(data).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(mContext, "Updated succesfully", Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(mContext, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                }
            });

            dialog.dismiss();
        });

        delBtn.setOnClickListener(v -> {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            reference.child(post_key).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(mContext, "Deleted succesfully", Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(mContext, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                }
            });

            dialog.dismiss();

        });

        dialog.show();

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
