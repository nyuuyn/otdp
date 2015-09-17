package org.opentosca.otdp.resources;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;

import org.opentosca.otdp.data.Tasks;
import org.opentosca.otdp.model.TaskState;
import org.opentosca.ui.vinothek.CallbackEndpointServlet;
import org.opentosca.ui.vinothek.CallbackManager;
import org.opentosca.ui.vinothek.model.ApplicationInstance;

public class CallbackResource {

	private static final Logger LOG = Logger.getLogger(CallbackResource.class
			.getName());

	@GET
	public void getCallback(@Context HttpServletRequest req) {
		LOG.info("Received CallbackRequest GET");
		this.handleRequest(req);
	}

	@POST
	public void postCallback(@Context HttpServletRequest req) {
		LOG.info("Received CallbackRequest POST");
		this.handleRequest(req);
	}

	private void handleRequest(HttpServletRequest req) {
		CallbackEndpointServlet callbackServlet = new CallbackEndpointServlet();
		callbackServlet.processMessage(req);

		ApplicationInstance instance = CallbackManager.getInstance(req.getParameter("callbackId"));
		
		String callbackId = instance.getCallbackId();
		
		for(TaskState task : Tasks.getInstance().tasks){
			if(task.getCallbackId().equals(callbackId)){
				task.setApplicationEndpoint(instance.getEndpointUrl());				
				task.setCurrentState(TaskState.State.CSARINSTANTIATED);
				task.setCurrentMessage("Deployment and Provisioning finished, see endpoint for application");
				break;
			}
		}
	}

}
