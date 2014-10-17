package com.pedrogirardi.rxjavatutorial;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import rx.Observable;
import rx.android.events.OnClickEvent;
import rx.android.observables.ViewObservable;
import rx.schedulers.Schedulers;


public class MainActivity extends ActionBarActivity {

    static private final String TAG = MainActivity.class.getSimpleName();

    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Observable<OnClickEvent> refreshClickStream = ViewObservable.clicks(findViewById(R.id.button_execute));

        final Observable<String> requestStream = refreshClickStream.map(event -> {
            double randomOffset = Math.floor(Math.random() * 500);
            return "https://api.github.com/users?since" + randomOffset;
        });

        final Observable<String> responseStream = requestStream
                .observeOn(Schedulers.io())
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
                response -> Log.d(TAG, "Response:[" + response + "]"),
                error -> Log.e(TAG, "Error", error)
        );
    }

}
