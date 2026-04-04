package com.swaas.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.swaas.app.R;

/**
 * SplashActivity — shown on launch, then routes to Login or Main based on auth state.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Fluid and bouncy entrance animations
        android.widget.ImageView ivLogo = findViewById(R.id.iv_logo);
        android.widget.TextView tvTitle = findViewById(R.id.tv_app_name);

        ivLogo.setScaleX(0.5f);
        ivLogo.setScaleY(0.5f);
        ivLogo.setAlpha(0f);
        ivLogo.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(1000)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                .start();

        tvTitle.setTranslationY(50f);
        tvTitle.setAlpha(0f);
        tvTitle.animate()
                .translationY(0f).alpha(1f)
                .setDuration(800)
                .setStartDelay(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
            Intent intent = isLoggedIn
                    ? new Intent(SplashActivity.this, MainActivity.class)
                    : new Intent(SplashActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION_MS);
    }
}
