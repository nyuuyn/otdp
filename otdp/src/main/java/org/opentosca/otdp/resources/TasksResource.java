package org.opentosca.otdp.resources;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Viewable;
import org.opentosca.otdp.data.Tasks;
import org.opentosca.otdp.data.TasksResourceDAO;
import org.opentosca.otdp.model.TaskState;

/**
 * @author Kálmán Képes - kepeskn@studi.informatik.uni-stuttgart.de
 *
 */
public class TasksResource {

	private static final Logger LOG = Logger.getLogger(TasksResource.class
			.getName());

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response getTasks() {
		LOG.info("TasksResource is requested");

		return Response.ok(new Viewable("index", new TasksResourceDAO()))
				.build();
	}
	
	@Path("/{id}")
	public TaskResource getTask(@PathParam("id")long id){
		for(TaskState state :Tasks.getInstance().tasks){
			if(state.getId() == id){
				return new TaskResource(state);
			}
		}
		return null;
	}

}
