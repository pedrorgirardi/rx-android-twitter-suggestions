package com.pedrogirardi.rxjavatutorial;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.android.events.OnClickEvent;
import rx.android.observables.ViewObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends ActionBarActivity {

    static private final String TAG = MainActivity.class.getSimpleName();

    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Observable<OnClickEvent> refreshClickStream = ViewObservable.clicks(findViewById(R.id.button_execute));

        final Observable<String> requestStream = refreshClickStream
                .startWith(new OnClickEvent(null)) // emulate a click when the activity starts
                .map(event -> {
                    double randomOffset = Math.floor(Math.random() * 500);
                    return "https://api.github.com/users?since" + randomOffset;
                });

        final Observable<List<GithubUser>> responseStream = requestStream
                .observeOn(Schedulers.io())
                .flatMap(url -> Observable.create(subscriber -> {

                    Request request = new Request.Builder()
                            .addHeader("User-Agent", "rx-java-tutorial")
                            .url(url)
                            .build();

                    try {
                        Response response = mOkHttpClient.newCall(request).execute();

                        List<GithubUser> list = new ArrayList<GithubUser>();

                        JSONArray users = new JSONArray(response.body().string());

                        for (int i = 0; i < users.length(); i++) {
                            JSONObject user = users.getJSONObject(i);

                            list.add(new GithubUser(user.getInt("id"),
                                    user.getString("login"),
                                    user.getString("url")));
                        }

                        subscriber.onNext(Collections.unmodifiableList(list));

                        subscriber.onCompleted();

                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }));

        responseStream
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            Log.d(TAG, "Response:[" + response + "]");

                            Toast.makeText(this, "Done(#" + response.size() + ")", Toast.LENGTH_SHORT).show();
                        },
                        error -> Log.e(TAG, "Error", error)
                );
    }

}
