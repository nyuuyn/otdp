package org.opentosca.otdp.resources;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.mvc.Viewable;
import org.opentosca.otdp.data.Configuration;
import org.opentosca.otdp.data.MainResourceDAO;
import org.opentosca.otdp.data.Tasks;
import org.opentosca.otdp.model.TaskState;
import org.opentosca.otdp.task.DeployProvisionTask;
import org.opentosca.ui.vinothek.CallbackEndpointServlet;

/**
 * @author Kálmán Képes - kepeskn@studi.informatik.uni-stuttgart.de
 *
 */
@Path("/")
public class MainResource {

	private static final Logger LOG = Logger.getLogger(MainResource.class
			.getName());

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response root() {
		LOG.info("Mainpage is requested");

		return Response.ok(
				new Viewable("index", new MainResourceDAO(Configuration
						.getInstance()))).build();
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadCSAR(
			@FormDataParam("csarAddress") String csarAddress,
			@FormDataParam("sshPrivateKey") String sshPrivateKey,
			@FormDataParam("keyPairName") String keyPairName,
			@FormDataParam("accessKey") String accesKey,
			@FormDataParam("regionEndpoint") String regionEndpoint,
			@FormDataParam("secretKey") String secretKey, @Context HttpServletRequest req) {

		LOG.info("Handling upload request");

		String otdpUrl = req.getRequestURL().toString();
		
		if(otdpUrl.endsWith("/")){
			otdpUrl = otdpUrl.substring(0, otdpUrl.length() - 1);
		}
		try {
			URL csarURL = new URL(csarAddress);
			TaskState newTask = new TaskState(csarURL, otdpUrl, sshPrivateKey.trim(), keyPairName, accesKey, regionEndpoint, secretKey);
			Tasks.getInstance().tasks.add(newTask);

			new Thread(new DeployProvisionTask(newTask)).start();
			UriBuilder builder = UriBuilder.fromResource(MainResource.class);

			builder.path("./otdp/tasks/" + newTask.getId());
			return Response.seeOther(builder.build()).build();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(400).entity("CSARUrl is not a valid URL")
					.build();
		}

	}

	@Path("/configuration")
	public ConfigurationResource getConfigurationResource() {
		return new ConfigurationResource();
	}

	@Path("/tasks")
	public TasksResource getTasksResource() {
		return new TasksResource();
	}
	
	@Path("/CallbackEndpoint")	
	public CallbackResource getCallbackResource(){
		return new CallbackResource();
	}

}
