package com.pedrogirardi.rxjavatutorial;

/**
 * Created by pedro on 18/10/14.
 */
public class GithubUser {

    public final int id;
    public final String login;
    public final String url;

    public GithubUser(int id, String login, String url) {
        this.id = id;
        this.login = login;
        this.url = url;
    }

}
