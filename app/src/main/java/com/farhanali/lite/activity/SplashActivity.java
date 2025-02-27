package com.farhanali.lite.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.view.WindowManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.ImageView;

import com.farhanali.lite.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        
        RelativeLayout layout = new RelativeLayout(this);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(
                                   RelativeLayout.LayoutParams.MATCH_PARENT,
                                   RelativeLayout.LayoutParams.MATCH_PARENT));
        layout.setBackgroundColor(Color.WHITE);

        ImageView icon = new ImageView(this);
        icon.setId(R.id.icon);
        icon.setImageResource(R.drawable.ic_icon_s);
        RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(70, 70);
        iconParams.setMargins(15, 15, 15, 15);
        iconParams.addRule(RelativeLayout.CENTER_VERTICAL);
        iconParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        icon.setLayoutParams(iconParams);
        layout.addView(icon);

        TextView textView = new TextView(this);
        textView.setText(getString(R.string.about));
        textView.setTextAppearance(android.R.style.TextAppearance_Small);
        textView.setTextColor(Color.parseColor("#FFFF4081"));
        RelativeLayout.LayoutParams textViewParams = new RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.WRAP_CONTENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT);
        textViewParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.icon);
        textViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        textViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        textViewParams.setMargins(15, 15, 15, 15);
        textView.setLayoutParams(textViewParams);
        layout.addView(textView);
        
        setContentView(layout);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
            finish();
        }, 2000);
    }
}
