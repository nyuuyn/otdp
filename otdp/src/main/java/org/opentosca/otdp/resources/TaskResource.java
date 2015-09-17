package org.opentosca.otdp.resources;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Viewable;
import org.opentosca.otdp.model.TaskState;
import org.opentosca.otdp.model.TaskState.State;

public class TaskResource {

	private TaskState state;

	public TaskResource(TaskState state) {
		this.state = state;
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response getTask() {
		return Response.ok(new Viewable("index", this.state)).build();
	}

	@Path("/state")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getState() {
		return Response.ok(this.state.getCurrentState().toString()).build();
	}
	
	@Path("/endpoint")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getApplicationEndpoint(){
		if(this.state.getCurrentState() == TaskState.State.CSARINSTANTIATED){			
			return Response.ok(this.state.getApplicationEndpoint()).build();
		} else {
			return Response.noContent().build();
		}
	}


}
