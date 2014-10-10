package com.pedrogirardi.rxjavatutorial;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;


public class MainActivity extends ActionBarActivity {

    static private final String TAG = MainActivity.class.getSimpleName();

    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    private final List<String> mUrls = Arrays.asList("https://api.github.com/users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_execute).setOnClickListener(view -> {
            Observable<String> requestStream = Observable.from(mUrls).observeOn(Schedulers.io());

            Observable<String> responseStream = requestStream
                    .flatMap(url -> Observable.create(subscriber -> {

                        Request request = new Request.Builder()
                                .addHeader("User-Agent", "rx-java-tutorial")
                                .url(url)
                                .build();

                        try {
                            Response response = mOkHttpClient.newCall(request).execute();

                            subscriber.onNext(response.body().string());

                            subscriber.onCompleted();

                        } catch (IOException e) {
                            subscriber.onError(e);
                        }
                    }));

            responseStream.subscribe(
                    response -> Log.d(TAG, "Response:[" + response + "]")
                    , error -> Log.e(TAG, "Error", error)
            );

        });

    }

}
