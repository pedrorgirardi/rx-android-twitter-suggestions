package com.pedrogirardi.rxjavatutorial;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.GitHubUser;
import rx.Observable;
import rx.android.events.OnClickEvent;
import rx.android.observables.ViewObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends ActionBarActivity {

    static private final String TAG = MainActivity.class.getSimpleName();

    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    @InjectView(R.id.suggestion1IdText)
    TextView mSuggestion1Id;

    @InjectView(R.id.suggestion1LoginText)
    TextView mSuggestion1Login;

    @InjectView(R.id.suggestion1UrlText)
    TextView mSuggestion1Url;

    @InjectView(R.id.suggestion2IdText)
    TextView mSuggestion2Id;

    @InjectView(R.id.suggestion2LoginText)
    TextView mSuggestion2Login;

    @InjectView(R.id.suggestion2UrlText)
    TextView mSuggestion2Url;

    @InjectView(R.id.suggestion3IdText)
    TextView mSuggestion3Id;

    @InjectView(R.id.suggestion3LoginText)
    TextView mSuggestion3Login;

    @InjectView(R.id.suggestion3UrlText)
    TextView mSuggestion3Url;

    @InjectView(R.id.closeSuggestion1Button)
    Button mClose1;

    @InjectView(R.id.closeSuggestion2Button)
    Button mClose2;

    @InjectView(R.id.closeSuggestion3Button)
    Button mClose3;

    @InjectView(R.id.refreshButton)
    Button mRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        final Observable<OnClickEvent> close1ClickStream = ViewObservable.clicks(mClose1);

        final Observable<OnClickEvent> close2ClickStream = ViewObservable.clicks(mClose2);

        final Observable<OnClickEvent> close3ClickStream = ViewObservable.clicks(mClose3);

        final Observable<OnClickEvent> refreshClickStream = ViewObservable.clicks(mRefresh);

        final Observable<String> requestStream = refreshClickStream
                .startWith(new OnClickEvent(null)) // emulate a click when the activity starts
                .map(event -> {
                    double randomOffset = Math.floor(Math.random() * 500);
                    return "https://api.github.com/users?since" + randomOffset;
                });

        final Observable<List<GitHubUser>> responseStream = requestStream
                .observeOn(Schedulers.io())
                .flatMap(url -> Observable.create(subscriber -> {

                    Request request = new Request.Builder()
                            .addHeader("User-Agent", "rx-java-tutorial")
                            .url(url)
                            .build();

                    try {
                        Response response = mOkHttpClient.newCall(request).execute();

                        List<GitHubUser> list = new ArrayList<>();

                        JSONArray users = new JSONArray(response.body().string());

                        for (int i = 0; i < users.length(); i++) {
                            JSONObject user = users.getJSONObject(i);

                            list.add(new GitHubUser(user.getInt("id"),
                                    user.getString("login"),
                                    user.getString("url")));
                        }

                        subscriber.onNext(Collections.unmodifiableList(list));

                        subscriber.onCompleted();

                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }));

        final Observable<GitHubUser> suggestion1Stream =
                createSuggestionStream(close1ClickStream, responseStream, refreshClickStream);

        final Observable<GitHubUser> suggestion2Stream =
                createSuggestionStream(close2ClickStream, responseStream, refreshClickStream);

        final Observable<GitHubUser> suggestion3Stream =
                createSuggestionStream(close3ClickStream, responseStream, refreshClickStream);

        suggestion1Stream.subscribe(githubUser -> {
            Log.d(TAG, "Suggestion 1:[" + githubUser + "]");

            showSuggestion(githubUser, SuggestionNumber.SUGGESTION_1);

        }, error -> Log.e(TAG, "Error on suggestion 1", error));

        suggestion2Stream.subscribe(githubUser -> {
            Log.d(TAG, "Suggestion 2:[" + githubUser + "]");

            showSuggestion(githubUser, SuggestionNumber.SUGGESTION_2);

        }, error -> Log.e(TAG, "Error on suggestion 2", error));

        suggestion3Stream.subscribe(githubUser -> {
            Log.d(TAG, "Suggestion 3:[" + githubUser + "]");

            showSuggestion(githubUser, SuggestionNumber.SUGGESTION_3);

        }, error -> Log.e(TAG, "Error on suggestion 3", error));
    }

    private Observable<GitHubUser> createSuggestionStream(Observable<OnClickEvent> closeClickStream,
                                                          Observable<List<GitHubUser>> responseStream,
                                                          Observable<OnClickEvent> refreshClickStream) {
        return Observable.combineLatest(
                closeClickStream.startWith(new OnClickEvent(null)),
                responseStream,
                (event, list) -> {
                    int index = (int) Math.floor(Math.random() * list.size());
                    return list.get(index);
                })
                .mergeWith(refreshClickStream.map(event -> null))
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void showSuggestion(GitHubUser githubUser, SuggestionNumber suggestionNumber) {
        String id;
        String login;
        String url;

        if (githubUser == null) {
            id = null;
            login = null;
            url = null;
        } else {
            id = String.valueOf(githubUser.getId());
            login = githubUser.getLogin();
            url = githubUser.getUrl();
        }


        switch (suggestionNumber) {
            case SUGGESTION_1:
                mSuggestion1Id.setText(id);
                mSuggestion1Login.setText(login);
                mSuggestion1Url.setText(url);
                break;
            case SUGGESTION_2:
                mSuggestion2Id.setText(id);
                mSuggestion2Login.setText(login);
                mSuggestion2Url.setText(url);
                break;
            case SUGGESTION_3:
                mSuggestion3Id.setText(id);
                mSuggestion3Login.setText(login);
                mSuggestion3Url.setText(url);
                break;
        }

    }

    enum SuggestionNumber {
        SUGGESTION_1,
        SUGGESTION_2,
        SUGGESTION_3,
    }

}
