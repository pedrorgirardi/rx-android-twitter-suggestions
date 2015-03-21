package rx

import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.util.Log
import com.pedrogirardi.rxjavatutorial.R
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.android.synthetic.activity_main.closeSuggestion1Button
import kotlinx.android.synthetic.activity_main.closeSuggestion2Button
import kotlinx.android.synthetic.activity_main.closeSuggestion3Button
import kotlinx.android.synthetic.activity_main.refreshButton
import org.json.JSONArray
import rx.android.schedulers.AndroidSchedulers
import rx.android.view.ViewObservable
import rx.schedulers.Schedulers

/**
 * Created by pgirardi on 3/21/15.
 */

data class GitHubUser(val id: Int, val login: String, val url: String)

class GitHubClient {
    val okHttpClient = OkHttpClient()

    fun gitHubUsers(): Observable<GitHubUser> {
        val randomOffset = Math.floor(Math.random() * 500)

        return gitHubUsers(since = "$randomOffset")
    }

    fun gitHubUsers(since: String): Observable<GitHubUser> {
        return Observable.create {
            if (!it.isUnsubscribed()) {
                try {
                    val url = "https://api.github.com/users?since$since"

                    val request = Request.Builder()
                            .addHeader("User-Agent", "android-reactive-programming")
                            .url(url)
                            .build()

                    val response = okHttpClient.newCall(request).execute()

                    val jArray = JSONArray(response.body().string())

                    for (i in 0..jArray.length() - 1) {
                        val jObject = jArray.getJSONObject(i)

                        val gitHubUser = GitHubUser(id = jObject.getInt("id"),
                                login = jObject.getString("login"),
                                url = jObject.getString("url"))

                        it.onNext(gitHubUser)
                    }

                    it.onCompleted()
                } catch(e: Throwable) {
                    it.onError(e)
                }
            }
        }
    }

}

class MainActivity : ActionBarActivity() {

    val tag = "RxApp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val gitHubClient = GitHubClient()

        val closeSuggestion1ClickStream = ViewObservable.clicks(closeSuggestion1Button)

        val closeSuggestion2ClickStream = ViewObservable.clicks(closeSuggestion2Button)

        val closeSuggestion3ClickStream = ViewObservable.clicks(closeSuggestion3Button)

        val refreshClickStream = ViewObservable.clicks(refreshButton, true)

        val gitHubUsersStream = refreshClickStream
                .observeOn(Schedulers.io())
                .flatMap { gitHubClient.gitHubUsers() }

        gitHubUsersStream
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d(tag, "$it")
                }, {
                    Log.e(tag, "Oh no something went wrong :(", it)
                })
    }
}
