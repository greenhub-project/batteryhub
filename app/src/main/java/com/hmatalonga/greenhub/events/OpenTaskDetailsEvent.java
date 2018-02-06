package com.hmatalonga.greenhub.events;

import com.hmatalonga.greenhub.models.ui.Task;

/**
 * Created by hugo on 27-05-2017.
 */
public class OpenTaskDetailsEvent {
    public final Task task;

    public OpenTaskDetailsEvent(Task task) {
        this.task = task;
    }
}
