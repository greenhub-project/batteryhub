package hmatalonga.greenhub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Entry Activity with a splash screen logo.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Starts Main Activity and loads its contents
        startActivity(new Intent(this, MainActivity.class));

        // Finishes the current Splash Activity
        finish();
    }

}
