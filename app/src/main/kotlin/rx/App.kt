package rx

import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import org.json.JSONArray

/**
 * Created by pgirardi on 3/21/15.
 */

class GitHubClient {
    val okHttpClient = OkHttpClient()

    fun gitHubUsers(since: String): Observable<GitHubUser> {
        return Observable.create {
            if (!it.isUnsubscribed()) {
                val url = "https://api.github.com/users?since$since"

                val request = Request.Builder()
                        .addHeader("User-Agent", "android-reactive-programming")
                        .url(url)
                        .build()

                val response = okHttpClient.newCall(request).execute()

                val jArray = JSONArray(response.body().toString())

                for (i in 0..jArray.length() - 1) {
                    val jObject = jArray.getJSONObject(i)

                    val gitHubUser = GitHubUser(id = jObject.getInt("id"),
                            login = jObject.getString("login"),
                            url = jObject.getString("url"))

                    it.onNext(gitHubUser)
                }

                it.onCompleted()

            }
        }
    }

}
