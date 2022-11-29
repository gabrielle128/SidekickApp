package com.gingineers.sidekick;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.gingineers.sidekick.journal.createjournal;
import com.gingineers.sidekick.journal.journaldetails;
import com.gingineers.sidekick.journal.journaledit;
import com.gingineers.sidekick.journal.journalmodel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class Journal extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FloatingActionButton journalFAB;

    RecyclerView journalRV;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    FirebaseUser mUser;
    FirebaseFirestore mFirestore;

    FirestoreRecyclerAdapter<journalmodel, JournalViewHolder> journalAdapter;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        bottomNavigationView = findViewById(R.id.botNavView);
        bottomNavigationView.setSelectedItemId(R.id.journal);


        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()){

                case R.id.journal:
                    return true;

                case R.id.todo:
                    startActivity(new Intent(getApplicationContext(), ToDo.class));
                    overridePendingTransition(0,0);
                    return true;

                case R.id.home:
                    startActivity(new Intent(getApplicationContext(), Homescreen.class));
                    overridePendingTransition(0,0);
                    return true;

                case R.id.wallet:
                    startActivity(new Intent(getApplicationContext(), Wallet.class));
                    overridePendingTransition(0,0);
                    return true;
            }

            return false;
        });

        // activity will go to createjournal
        journalFAB = findViewById(R.id.journalFAB);
        journalFAB.setOnClickListener(v -> startActivity(new Intent(Journal.this, createjournal.class)));

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirestore = FirebaseFirestore.getInstance();

        // display journal entry on journal activity
        Query query = mFirestore.collection("journal").document(mUser.getUid()).collection("myJournal").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<journalmodel> alluserjournal = new FirestoreRecyclerOptions.Builder<journalmodel>().setQuery(query, journalmodel.class).build();

        journalAdapter = new FirestoreRecyclerAdapter<journalmodel, JournalViewHolder>(alluserjournal) {
            @Override
            protected void onBindViewHolder(@NonNull JournalViewHolder journalViewHolder, int i, @NonNull journalmodel journalmodel) {

                ImageView menupopbtn = journalViewHolder.itemView.findViewById(R.id.menupopbtn);

                journalViewHolder.journaltitle.setText(journalmodel.getTitle());
                journalViewHolder.journalcontent.setText(journalmodel.getContent());
                journalViewHolder.journaldateTime.setText(journalmodel.getDateTime());
                journalViewHolder.relativeLayout.setBackgroundColor(Color.parseColor(journalmodel.getColor()));

                String docId = journalAdapter.getSnapshots().getSnapshot(i).getId();

                // journal entry is clicked
                journalViewHolder.itemView.setOnClickListener(v -> {

                    Intent intent = new Intent(v.getContext(), journaldetails.class);
                    intent.putExtra("title", journalmodel.getTitle());
                    intent.putExtra("content", journalmodel.getContent());
                    intent.putExtra("dateTime", journalmodel.getDateTime());
                    intent.putExtra("timestamp", journalmodel.getTimestamp());
                    intent.putExtra("color", journalmodel.getColor());

                    intent.putExtra("journalId", docId);

                    v.getContext().startActivity(intent);
                    //Toast.makeText(getApplicationContext(), "This is clicked", Toast.LENGTH_SHORT).show();
                });

                // menu for edit and delete journal entry
                menupopbtn.setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.setGravity(Gravity.END);

                    // edit button in pop up menu and will go to journaldetails
                    popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(item -> {
                        Intent intent = new Intent(v.getContext(), journaledit.class);
                        intent.putExtra("title", journalmodel.getTitle());
                        intent.putExtra("content", journalmodel.getContent());
                        intent.putExtra("dateTime", journalmodel.getDateTime());
                        intent.putExtra("timestamp", journalmodel.getTimestamp());
                        intent.putExtra("color", journalmodel.getColor());
                        intent.putExtra("journalId", docId);
                        v.getContext().startActivity(intent);
                        return false;
                    });

                    // delete button in pop up menu
                    popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {

                        // delete journal entry with yes or no
                        AlertDialog.Builder altdial = new AlertDialog.Builder(Journal.this);
                        altdial.setMessage("Do you want to delete this journal?").setCancelable(false)
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    DocumentReference documentReference = mFirestore.collection("journal").document(mUser.getUid()).collection("myJournal").document(docId);
                                    documentReference.delete().addOnSuccessListener(aVoid -> Toast.makeText(v.getContext(), "Journal is deleted.", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(v.getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());
                                })
                                .setNegativeButton("No", (dialog, which) -> dialog.cancel());

                        altdial.show();

                        return false;
                    });

                    popupMenu.show();

                });

            }

            @NonNull
            @Override
            public JournalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_layout, parent, false);
                return new JournalViewHolder(view);
            }
        };

        // display entries in journal
        journalRV = findViewById(R.id.journalRV);
        journalRV.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        journalRV.setLayoutManager(staggeredGridLayoutManager);
        journalRV.setAdapter(journalAdapter);

    }

    public static class JournalViewHolder extends RecyclerView.ViewHolder {
        private final TextView journaltitle;
        private final TextView journalcontent;
        private final TextView journaldateTime;
        CardView journalCard;
        RelativeLayout relativeLayout;
        LinearLayout mjournal;

        public JournalViewHolder(@NonNull View itemView) {
            super(itemView);
            journaltitle = itemView.findViewById(R.id.journaltitle);
            journalcontent = itemView.findViewById(R.id.journalcontent);
            journaldateTime = itemView.findViewById(R.id.journaldateTime);
            journalCard = itemView.findViewById(R.id.journalcard);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);

            mjournal = itemView.findViewById(R.id.journalLL);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        journalAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (journalAdapter != null){
            journalAdapter.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), Homescreen.class));
    }

}