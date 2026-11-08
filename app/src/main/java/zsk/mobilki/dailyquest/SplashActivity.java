package zsk.mobilki.dailyquest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1200L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        animateSplash();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DELAY_MS);
    }

    private void animateSplash() {
        View card = findViewById(R.id.layout_splash_card);
        View status = findViewById(R.id.layout_splash_status);

        card.setAlpha(0f);
        card.setScaleX(0.96f);
        card.setScaleY(0.96f);
        status.setAlpha(0f);
        status.setTranslationY(18f);

        card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(520)
                .start();

        status.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(180)
                .setDuration(420)
                .start();
    }
}
