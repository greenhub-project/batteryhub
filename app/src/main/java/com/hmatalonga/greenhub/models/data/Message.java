package com.hmatalonga.greenhub.models.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Message data definition.
 */
public class Message extends RealmObject {

    @PrimaryKey
    public int id;

    public String type;

    public String title;

    public String body;

    public String date;

    public boolean read;

    public Message() {
        this.type = "info";
        this.read = false;
    }

    public Message(int id, String title, String body, String date) {
        this.id = id;
        this.type = "info";
        this.title = title;
        this.body = body;
        this.date = date;
        this.read = false;
    }

    public Message(int id, String title, String body, String date, boolean read) {
        this.id = id;
        this.type = "info";
        this.title = title;
        this.body = body;
        this.date = date;
        this.read = read;
    }
}
