package org.opentosca.otdp.data;

import java.util.ArrayList;
import java.util.List;

import org.opentosca.otdp.model.TaskState;

/**
 * @author Kálmán Képes - kepeskn@studi.informatik.uni-stuttgart.de
 *
 */
public class Tasks {
	
	public List<TaskState> tasks = new ArrayList<TaskState>();
	
	private Tasks() {
	}

	private static class SingletonHolder {
		private static final Tasks INSTANCE = new Tasks();
	}

	public static Tasks getInstance() {
		return SingletonHolder.INSTANCE;
	}

}
