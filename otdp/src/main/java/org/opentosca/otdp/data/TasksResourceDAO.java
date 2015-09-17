package org.opentosca.otdp.data;

import java.util.List;

import org.opentosca.otdp.model.TaskState;

public class TasksResourceDAO {

	public final List<TaskState> tasks;

	public TasksResourceDAO() {
		this.tasks = Tasks.getInstance().tasks;
	}

	/**
	 * @return the tasks
	 */
	public List<TaskState> getTasks() {
		return tasks;
	}
}
