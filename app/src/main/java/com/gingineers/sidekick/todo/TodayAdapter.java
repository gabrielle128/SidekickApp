package com.gingineers.sidekick.todo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.gingineers.sidekick.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class TodayAdapter extends RecyclerView.Adapter<TodayAdapter.ViewHolder>{

    private final Context mContext;
    private final List<Model> myTodayList;
    private String post_key = "";
    private String title = "";
    private String description = "";
    private String date = "";
    private String timestamp = "";

    public TodayAdapter(Context mContext, List<Model> myTodayList) {
        this.mContext = mContext;
        this.myTodayList = myTodayList;
    }

    @NonNull
    @Override
    public TodayAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.retrievetodo_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodayAdapter.ViewHolder holder, int position) {

        final Model model = myTodayList.get(position);

        holder.title.setText(model.getTitle());
        holder.description.setText(model.getDescription());
        holder.date.setText(model.getDate());
        holder.timestamp.setText(model.getTimestamp());


        holder.circle.setOnClickListener(view -> {
            post_key = model.getId();
            title = model.getTitle();
            description = model.getDescription();
            date = model.getDate();
            timestamp = model.getTimestamp();

            holder.circle.setImageResource(R.drawable.circle_check);

            AlertDialog.Builder altdel = new AlertDialog.Builder(mContext);
            altdel.setMessage("Mark as done? If yes, task will be deleted").setCancelable(false)
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("todo").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                        reference.child(post_key).removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(mContext, "Task deleted sucessfully", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String err = Objects.requireNonNull(task.getException()).toString();
                                Toast.makeText(mContext, "Failed to delete: " + err, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.cancel();
                        holder.circle.setImageResource(R.drawable.circle);
                    });
            altdel.show();
        });

        holder.circle.setImageResource(R.drawable.circle);

        String onlineUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy-M-d");
        Calendar calendar = Calendar.getInstance();
        String now = dateFormat.format(calendar.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("todo").child(onlineUserId);
        Query today = reference.orderByChild("timestamp").equalTo(now);
        today.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.todoCard.setCardBackgroundColor(Color.parseColor("#f0d89a"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        holder.itemView.setOnClickListener(view -> {
            post_key = model.getId();
            title = model.getTitle();
            description = model.getDescription();
            date = model.getDate();
            updateTask();
        });

    }

    private void updateTask() {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.updatetodo_layout, null);
        myDialog.setView(view);

        final AlertDialog dialog = myDialog.create();

        final EditText Title = view.findViewById(R.id.titleET);
        final EditText Description = view.findViewById(R.id.descET);
        final Button btnDate = view.findViewById(R.id.btnDate);
        final TextView Date = view.findViewById(R.id.dateTV);
        final TextView Timestamp = view.findViewById(R.id.timestampTV);

        Title.setText(title);
        Title.setSelection(title.length());

        Description.setText(description);
        Description.setSelection(description.length());

        Date.setText(date);
        Timestamp.setText(timestamp);

        Button delete = view.findViewById(R.id.btnDelete);
        Button update = view.findViewById(R.id.btnUpdate);

        btnDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, (view1, year1, month1, dayOfMonth) -> {
                int months = month1 + 1;
                String dateTxt = dayOfMonth + "-" + months + "-" + year1;
                Date.setText(dateTxt);

                String timestampTxt = year1 + "-" + months + "-" + dayOfMonth;
                Timestamp.setText(timestampTxt);
            }, year, month, day);
            datePickerDialog.show();
        });


        update.setOnClickListener(v -> {
            title = Title.getText().toString().trim();
            description = Description.getText().toString().trim();
            date = Date.getText().toString().trim();
            timestamp = Timestamp.getText().toString().trim();

            Model model = new Model(title, description, post_key, date, timestamp);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("todo").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            reference.child(post_key).setValue(model).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Toast.makeText(mContext, "Task has been updated sucessfully", Toast.LENGTH_SHORT).show();
                }
                else {
                    String err = Objects.requireNonNull(task.getException()).toString();
                    Toast.makeText(mContext, "Update failed: " + err, Toast.LENGTH_SHORT).show();
                }
            });
            dialog.dismiss();
        });

        delete.setOnClickListener(v -> {

            AlertDialog.Builder altdel = new AlertDialog.Builder(mContext);
            altdel.setMessage("Do you want to delete this task?").setCancelable(false)
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("todo").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                        reference.child(post_key).removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(mContext, "Task deleted sucessfully", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String err = Objects.requireNonNull(task.getException()).toString();
                                Toast.makeText(mContext, "Failed to delete: " + err, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialog1, which) -> dialog1.cancel());
            altdel.show();

            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return myTodayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView title, description, date, timestamp;
        public CardView todoCard;
        public ImageView circle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.todoTitle);
            description = itemView.findViewById(R.id.todoDesc);
            date = itemView.findViewById(R.id.todoDate);
            timestamp = itemView.findViewById(R.id.todoTimestamp);
            circle = itemView.findViewById(R.id.circle);
            todoCard = itemView.findViewById(R.id.todoCard);


        }
    }
}
