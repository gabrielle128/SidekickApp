package com.gingineers.sidekick.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.format.DateFormat;
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

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder>{

    private final Context mContext;
    private final List<EventModel> myEventList;

    private String post_key = "";
    private String name = "";
    private String content = "";
    private String date = "";
    private String time = "";
    private String timestamp = "";

    public EventAdapter(Context mContext, List<EventModel> myEventList) {
        this.mContext = mContext;
        this.myEventList = myEventList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.retrieveevent_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final EventModel eventModel = myEventList.get(position);

        holder.name.setText(eventModel.getName());
        holder.content.setText(eventModel.getContent());
        holder.date.setText(eventModel.getDate());
        holder.time.setText(eventModel.getTime());
        holder.timestamp.setText(eventModel.getTimestamp());

        holder.itemView.setOnClickListener(view -> {
            post_key = eventModel.getId();
            name = eventModel.getName();
            content = eventModel.getContent();
            date = eventModel.getDate();
            time = eventModel.getTime();

            updateEvent();
        });

    }

    private void updateEvent() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View myView = inflater.inflate(R.layout.updateevent_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();

        // initialize widgets from addevent_layout
        final EditText Name = myView.findViewById(R.id.eventNameET);
        final EditText Content = myView.findViewById(R.id.eventContentET);
        final ImageView btnDate = myView.findViewById(R.id.btnDate);
        final ImageView btnTime = myView.findViewById(R.id.btnTime);
        final TextView Date = myView.findViewById(R.id.dateTV);
        final TextView Time = myView.findViewById(R.id.timeTV);
        final TextView Timestamp = myView.findViewById(R.id.timestampTV);

        final Button delete = myView.findViewById(R.id.btnDelete);
        final Button update = myView.findViewById(R.id.btnUpdate);

        Name.setText(name);
        Name.setSelection(name.length());

        Content.setText(content);
        Content.setSelection(content.length());

        Date.setText(date);
        Time.setText(time);
        Timestamp.setText(timestamp);

        btnDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, (view, year1, month1, dayOfMonth) -> {

                int months = month1 + 1;
                String dateTxt = dayOfMonth + "-" + months + "-" + year1;
                Date.setText(dateTxt);

                String timestampTxt = year1 + "-" + months + "-" + dayOfMonth;
                Timestamp.setText(timestampTxt);

            }, year, month, day);
            datePickerDialog.show();
        });

        btnTime.setOnClickListener(new View.OnClickListener() {

            int t1Hour, t1Minute;

            @Override
            public void onClick(View view) {

                TimePickerDialog timePickerDialog = new TimePickerDialog(mContext, (view1, hourOfDay, minute) -> {
                    // initialize hour and minute
                    t1Hour = hourOfDay;
                    t1Minute = minute;
                    // initialize calendar
                    Calendar calendar = Calendar.getInstance();
                    // set hour and minute
                    calendar.set(0, 0, 0, t1Hour, t1Minute);
                    // set selected time on text view
                    Time.setText(DateFormat.format("hh:mm aa", calendar));
                }, 12,0,false);
                // display previous selected time
                timePickerDialog.updateTime(t1Hour, t1Minute);
                // show dialog
                timePickerDialog.show();
            }
        });

        update.setOnClickListener(view -> {
            // get datas from layout
            name = Name.getText().toString().trim();
            content = Content.getText().toString().trim();
            date = Date.getText().toString().trim();
            time = Time.getText().toString().trim();
            timestamp = Timestamp.getText().toString().trim();

            EventModel model = new EventModel(post_key, name, content, date, time, timestamp);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("event").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            reference.child(post_key).setValue(model).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Toast.makeText(mContext, "Event has been updated successfully", Toast.LENGTH_SHORT).show();
                }
                else {
                    String error = Objects.requireNonNull(task.getException()).toString();
                    Toast.makeText(mContext, "Failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
            dialog.dismiss();
        });

        delete.setOnClickListener(view -> {
            AlertDialog.Builder altdel = new AlertDialog.Builder(mContext);
            altdel.setMessage("Do you want to delete this event?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("event").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                        reference.child(post_key).removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(mContext, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String err = Objects.requireNonNull(task.getException()).toString();
                                Toast.makeText(mContext, "Failed to delete: " + err, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialogInterface, i) -> dialog.cancel());
            altdel.show();
            dialog.dismiss();
        });

        dialog.show();


    }

    @Override
    public int getItemCount() {
        return myEventList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView name, content, date, time, timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            content = itemView.findViewById(R.id.content);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            timestamp = itemView.findViewById(R.id.timestamp);

        }
    }

}
