package bacy.game.snack;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class GameActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SnackView(this));
    }
}
