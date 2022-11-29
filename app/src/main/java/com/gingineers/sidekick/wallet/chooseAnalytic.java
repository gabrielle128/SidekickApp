package com.gingineers.sidekick.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.gingineers.sidekick.R;
import com.gingineers.sidekick.Wallet;

public class chooseAnalytic extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_analytics);

        ImageButton btnBackanalytics = findViewById(R.id.btnBackanalytics);
        CardView todayCV = findViewById(R.id.todayCV);
        CardView weekCV = findViewById(R.id.weekCV);
        CardView monthCV = findViewById(R.id.monthCV);

        btnBackanalytics.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Wallet.class)));
        todayCV.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), dailyAnalytics.class)));
        weekCV.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), weeklyAnalytics.class)));
        monthCV.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), monthlyAnalytics.class)));
    }
}