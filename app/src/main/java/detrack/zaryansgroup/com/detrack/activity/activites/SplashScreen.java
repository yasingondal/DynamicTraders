package detrack.zaryansgroup.com.detrack.activity.activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import detrack.zaryansgroup.com.detrack.R;
import pl.droidsonroids.gif.GifImageButton;

public class SplashScreen extends AppCompatActivity {
    GifImageButton gib;
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        
        Animation animation= AnimationUtils.loadAnimation(this,R.anim.moveup);
        ImageView imageView= (ImageView) findViewById(R.id.imageView);
        imageView.setAnimation(animation);
        new Timer().schedule(new TimerTask() {
            public void run() {
                SplashScreen.this.runOnUiThread(new Runnable() {
                    public void run() {
                        startActivity(new Intent(SplashScreen.this, RegisterActivity.class));
                       // startActivity(new Intent(SplashScreen.this, WelcomeActivity.class));
                        finish();
                    }
                });
            }
        }, 2000);

    }

}
