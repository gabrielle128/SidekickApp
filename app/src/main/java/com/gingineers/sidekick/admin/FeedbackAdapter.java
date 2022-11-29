package com.gingineers.sidekick.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.gingineers.sidekick.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {

    Context context;
    ArrayList<FeedbackModel> feedbackList;

    private String post_key = "";

    public FeedbackAdapter(Context context, ArrayList<FeedbackModel> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_retrievefeedback, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeedbackModel model = feedbackList.get(position);

        holder.email.setText(model.getEmail());
        holder.feedback.setText(model.getFeedback());
        holder.date.setText(model.getDate());


        // show delete dialog on click of cardview
        holder.feedbackCard.setOnClickListener(view -> {

            post_key = model.getId();

            AlertDialog.Builder altdel = new AlertDialog.Builder(context);
            altdel.setMessage("Do you want to delete this feedback?").setCancelable(false)
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("feedback");
                        reference.child(post_key).removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(context, "Feedback deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String err = Objects.requireNonNull(task.getException()).toString();
                                Toast.makeText(context, "Failed to delete: " + err, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialog, i) -> dialog.cancel());
            altdel.show();
        });
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView email, feedback, date;
        CardView feedbackCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            email = itemView.findViewById(R.id.emailTV);
            feedback = itemView.findViewById(R.id.feedbackTV);
            date = itemView.findViewById(R.id.dateTV);
            feedbackCard = itemView.findViewById(R.id.feedbackCard);

        }
    }

}
