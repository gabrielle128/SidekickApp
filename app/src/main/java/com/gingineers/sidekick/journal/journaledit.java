package com.gingineers.sidekick.journal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gingineers.sidekick.Journal;
import com.gingineers.sidekick.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class journaledit extends AppCompatActivity {

    Intent data;
    EditText title, content;
    TextView dateTime, timestamp, colorTV;

    ImageButton btnBackedit, btnEdit, btnColor;

    RelativeLayout journalLayout;
    LinearLayout header;

    FirebaseFirestore mFirestore;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journaledit);

        title = findViewById(R.id.titleET);
        content = findViewById(R.id.contentET);
        dateTime = findViewById(R.id.dateTime);
        timestamp = findViewById(R.id.timestamp);
        colorTV = findViewById(R.id.colorTV);

        journalLayout = findViewById(R.id.journalLayout);
        header = findViewById(R.id.header);

        data = getIntent();
        mFirestore = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        btnBackedit = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);

        btnColor = findViewById(R.id.btnColor);
        btnColor.setOnClickListener(view -> addColor());

        btnBackedit.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Journal.class)));

        btnEdit.setOnClickListener(v -> {
            //Toast.makeText(getApplicationContext(), "savebutton clicked", Toast.LENGTH_SHORT).show();

            String newtitle = title.getText().toString();
            String newcontent = content.getText().toString();
            String newdateTime = dateTime.getText().toString();
            String newtimestamp = timestamp.getText().toString();
            String newcolor = colorTV.getText().toString();

            if (newtitle.isEmpty() || newcontent.isEmpty()){
                Toast.makeText(getApplicationContext(), "Something is empty", Toast.LENGTH_SHORT).show();
            }
            else{
                DocumentReference documentReference = mFirestore.collection("journal").document(mUser.getUid()).collection("myJournal").document(data.getStringExtra("journalId"));
                Map<String, Object> journal = new HashMap<>();
                journal.put("title", newtitle);
                journal.put("content", newcontent);
                journal.put("dateTime", newdateTime);
                journal.put("timestamp", newtimestamp);
                journal.put("color", newcolor);

                documentReference.set(journal).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "Journal is updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(journaledit.this, Journal.class));
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to update", Toast.LENGTH_SHORT).show());
                startActivity(new Intent(journaledit.this, Journal.class));
            }

        });

        String journaltitle = data.getStringExtra("title");
        String journalcontent = data.getStringExtra("content");
        String journaldateTime = data.getStringExtra("dateTime");
        String journaltimestamp = data.getStringExtra("timestamp");
        String journalcolor = data.getStringExtra("color");

        title.setText(journaltitle);
        content.setText(journalcontent);
        dateTime.setText(journaldateTime);
        timestamp.setText(journaltimestamp);
        colorTV.setText(journalcolor);

        header.setBackgroundColor(Color.parseColor(data.getStringExtra("color")));
        journalLayout.setBackgroundColor(Color.parseColor(data.getStringExtra("color")));

    }

    @SuppressLint("SetTextI18n")
    private void addColor() {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.palette, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();

        final View color1 = myView.findViewById(R.id.color_1);
        final View color2 = myView.findViewById(R.id.color_2);
        final View color3 = myView.findViewById(R.id.color_3);
        final View color4 = myView.findViewById(R.id.color_4);
        final View color5 = myView.findViewById(R.id.color_5);
        final View color6 = myView.findViewById(R.id.color_6);
        final View color7 = myView.findViewById(R.id.color_7);
        final View color8 = myView.findViewById(R.id.color_8);
        final View color9 = myView.findViewById(R.id.color_9);
        final View color10 = myView.findViewById(R.id.color_10);
        final View color11 = myView.findViewById(R.id.color_11);
        final View color12 = myView.findViewById(R.id.color_12);
        final View color13 = myView.findViewById(R.id.color_13);
        final View color14 = myView.findViewById(R.id.color_14);
        final View color15 = myView.findViewById(R.id.color_15);
        final View color16 = myView.findViewById(R.id.color_16);

        // initialize imageview checks per color
        final ImageView img1 = myView.findViewById(R.id.img1);
        final ImageView img2 = myView.findViewById(R.id.img2);
        final ImageView img3 = myView.findViewById(R.id.img3);
        final ImageView img4 = myView.findViewById(R.id.img4);
        final ImageView img5 = myView.findViewById(R.id.img5);
        final ImageView img6 = myView.findViewById(R.id.img6);
        final ImageView img7 = myView.findViewById(R.id.img7);
        final ImageView img8 = myView.findViewById(R.id.img8);
        final ImageView img9 = myView.findViewById(R.id.img9);
        final ImageView img10 = myView.findViewById(R.id.img10);
        final ImageView img11 = myView.findViewById(R.id.img11);
        final ImageView img12 = myView.findViewById(R.id.img12);
        final ImageView img13 = myView.findViewById(R.id.img13);
        final ImageView img14 = myView.findViewById(R.id.img14);
        final ImageView img15 = myView.findViewById(R.id.img15);
        final ImageView img16 = myView.findViewById(R.id.img16);

        final TextView color = myView.findViewById(R.id.color);

        // initialize save button
        final ImageButton save = myView.findViewById(R.id.save);

        color1.setOnClickListener(view -> {

            color.setText("#FFFFFF");
            colorTV.setText("#FFFFFF");

            journalLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            header.setBackgroundColor(Color.parseColor("#FFFFFF"));

            img1.setImageResource(R.drawable.check);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view1 -> {
                dialog.dismiss();
            });

        });

        color2.setOnClickListener(view -> {

            color.setText("#F0F0F2");
            colorTV.setText("#F0F0F2");

            journalLayout.setBackgroundColor(Color.parseColor("#F0F0F2"));
            header.setBackgroundColor(Color.parseColor("#F0F0F2"));

            img1.setImageResource(0);
            img2.setImageResource(R.drawable.check);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view12 -> {
                dialog.dismiss();
            });

        });

        color3.setOnClickListener(view -> {

            color.setText("#FDFD96");
            colorTV.setText("#FDFD96");

            journalLayout.setBackgroundColor(Color.parseColor("#FDFD96"));
            header.setBackgroundColor(Color.parseColor("#FDFD96"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(R.drawable.check);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view13 -> {
                dialog.dismiss();
            });

        });

        color4.setOnClickListener(view -> {

            color.setText("#F7C289");
            colorTV.setText("#F7C289");

            journalLayout.setBackgroundColor(Color.parseColor("#F7C289"));
            header.setBackgroundColor(Color.parseColor("#F7C289"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(R.drawable.check);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view14 -> {
                dialog.dismiss();
            });

        });

        color5.setOnClickListener(view -> {

            color.setText("#D9B29C");
            colorTV.setText("#D9B29C");

            journalLayout.setBackgroundColor(Color.parseColor("#D9B29C"));
            header.setBackgroundColor(Color.parseColor("#D9B29C"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(R.drawable.check);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view15 -> {
                dialog.dismiss();
            });

        });

        color6.setOnClickListener(view -> {

            color.setText("#FFB6B3");
            colorTV.setText("#FFB6B3");


            journalLayout.setBackgroundColor(Color.parseColor("#FFB6B3"));
            header.setBackgroundColor(Color.parseColor("#FFB6B3"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(R.drawable.check);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view16 -> {
                dialog.dismiss();
            });

        });

        color7.setOnClickListener(view -> {

            color.setText("#F7DCEA");
            colorTV.setText("#F7DCEA");

            journalLayout.setBackgroundColor(Color.parseColor("#F7DCEA"));
            header.setBackgroundColor(Color.parseColor("#F7DCEA"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(R.drawable.check);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view17 -> {
                dialog.dismiss();
            });

        });

        color8.setOnClickListener(view -> {

            color.setText("#D5B9B1");
            colorTV.setText("#D5B9B1");

            journalLayout.setBackgroundColor(Color.parseColor("#D5B9B1"));
            header.setBackgroundColor(Color.parseColor("#D5B9B1"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(R.drawable.check);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view18 -> {
                dialog.dismiss();
            });

        });

        color9.setOnClickListener(view -> {

            color.setText("#C784AF");
            colorTV.setText("#C784AF");

            journalLayout.setBackgroundColor(Color.parseColor("#C784AF"));
            header.setBackgroundColor(Color.parseColor("#C784AF"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(R.drawable.check);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view19 -> {
                dialog.dismiss();
            });

        });

        color10.setOnClickListener(view -> {

            color.setText("#BCAFBD");
            colorTV.setText("#BCAFBD");

            journalLayout.setBackgroundColor(Color.parseColor("#BCAFBD"));
            header.setBackgroundColor(Color.parseColor("#BCAFBD"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(R.drawable.check);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view110 -> {
                dialog.dismiss();
            });

        });

        color11.setOnClickListener(view -> {

            color.setText("#7393C2");
            colorTV.setText("#7393C2");

            journalLayout.setBackgroundColor(Color.parseColor("#7393C2"));
            header.setBackgroundColor(Color.parseColor("#7393C2"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(R.drawable.check);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view111 -> {
                dialog.dismiss();
            });

        });

        color12.setOnClickListener(view -> {

            color.setText("#B6C7DD");
            colorTV.setText("#B6C7DD");

            journalLayout.setBackgroundColor(Color.parseColor("#B6C7DD"));
            header.setBackgroundColor(Color.parseColor("#B6C7DD"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(R.drawable.check);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view112 -> {
                dialog.dismiss();
            });

        });

        color13.setOnClickListener(view -> {

            color.setText("#B4CBD9");
            colorTV.setText("#B4CBD9");

            journalLayout.setBackgroundColor(Color.parseColor("#B4CBD9"));
            header.setBackgroundColor(Color.parseColor("#B4CBD9"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(R.drawable.check);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view113 -> {
                dialog.dismiss();
            });

        });

        color14.setOnClickListener(view -> {

            color.setText("#8D9365");
            colorTV.setText("#8D9365");

            journalLayout.setBackgroundColor(Color.parseColor("#8D9365"));
            header.setBackgroundColor(Color.parseColor("#8D9365"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(R.drawable.check);
            img15.setImageResource(0);
            img16.setImageResource(0);

            save.setOnClickListener(view114 -> {
                dialog.dismiss();
            });

        });

        color15.setOnClickListener(view -> {

            color.setText("#C1B985");
            colorTV.setText("#C1B985");

            journalLayout.setBackgroundColor(Color.parseColor("#C1B985"));
            header.setBackgroundColor(Color.parseColor("#C1B985"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(R.drawable.check);
            img16.setImageResource(0);

            save.setOnClickListener(view115 -> {
                dialog.dismiss();
            });

        });

        color16.setOnClickListener(view -> {

            color.setText("#A7DBC8");
            colorTV.setText("#A7DBC8");

            journalLayout.setBackgroundColor(Color.parseColor("#A7DBC8"));
            header.setBackgroundColor(Color.parseColor("#A7DBC8"));

            img1.setImageResource(0);
            img2.setImageResource(0);
            img3.setImageResource(0);
            img4.setImageResource(0);
            img5.setImageResource(0);
            img6.setImageResource(0);
            img7.setImageResource(0);
            img8.setImageResource(0);
            img9.setImageResource(0);
            img10.setImageResource(0);
            img11.setImageResource(0);
            img12.setImageResource(0);
            img13.setImageResource(0);
            img14.setImageResource(0);
            img15.setImageResource(0);
            img16.setImageResource(R.drawable.check);

            save.setOnClickListener(view116 -> {
                dialog.dismiss();
            });

        });


        dialog.show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(journaledit.this, Journal.class));
    }

}