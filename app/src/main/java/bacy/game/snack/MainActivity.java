package bacy.game.snack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button start;
    Intent next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        next = new Intent();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        CommonData.screenWidth = metric.widthPixels;
        CommonData.screenHeight = metric.heightPixels;

        setContentView(R.layout.activity_main);
        start = (Button)findViewById(R.id.button);
        start.setOnClickListener(startClick);
    }

    private View.OnClickListener startClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            next.setClass(MainActivity.this, GameActivity.class);
            startActivity(next);
        }
    };
}