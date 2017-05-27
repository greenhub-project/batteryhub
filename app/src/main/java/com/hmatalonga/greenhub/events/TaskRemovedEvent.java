package com.hmatalonga.greenhub.events;

import com.hmatalonga.greenhub.models.ui.Task;

/**
 * Created by hugo on 27-05-2017.
 */
public class TaskRemovedEvent {

    public final int position;

    public final Task task;

    public TaskRemovedEvent(int position, Task task) {
        this.position = position;
        this.task = task;
    }
}