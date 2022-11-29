package com.gingineers.sidekick.journal;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gingineers.sidekick.Journal;
import com.gingineers.sidekick.R;


public class journaldetails extends AppCompatActivity {

    RelativeLayout journalLayout;
    LinearLayout header;

    ImageButton btnBack, btnToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journaldetails);

        TextView title = findViewById(R.id.titleTV);
        TextView content = findViewById(R.id.contentTV);
        TextView dateTime = findViewById(R.id.dateTime);
        TextView timestamp = findViewById(R.id.timestamp);
        TextView color = findViewById(R.id.colorTV);

        journalLayout = findViewById(R.id.journalLayout);
        header = findViewById(R.id.header);

        btnBack = findViewById(R.id.btnBack);
        btnToEdit = findViewById(R.id.btnToEdit);

        btnBack.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Journal.class)));

        Intent data = getIntent();

        btnToEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), journaledit.class);
            intent.putExtra("title", data.getStringExtra("title"));
            intent.putExtra("content", data.getStringExtra("content"));
            intent.putExtra("dateTime", data.getStringExtra("dateTime"));
            intent.putExtra("timestamp", data.getStringExtra("timestamp"));
            intent.putExtra("color", data.getStringExtra("color"));

            intent.putExtra("journalId", data.getStringExtra("journalId"));
            v.getContext().startActivity(intent);
        });

        title.setText(data.getStringExtra("title"));
        content.setText(data.getStringExtra("content"));
        dateTime.setText(data.getStringExtra("dateTime"));
        timestamp.setText(data.getStringExtra("timestamp"));
        color.setText(data.getStringExtra("color"));

        header.setBackgroundColor(Color.parseColor(data.getStringExtra("color")));
        journalLayout.setBackgroundColor(Color.parseColor(data.getStringExtra("color")));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(journaldetails.this, Journal.class));
    }

}