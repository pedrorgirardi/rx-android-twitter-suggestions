package com.pedrogirardi.rxjavatutorial;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;


public class MainActivity extends ActionBarActivity {

    static private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_execute).setOnClickListener(
                view -> Log.d(TAG, "Event:[View.OnClickListener] View:[" + view + "]")
        );

    }

}
