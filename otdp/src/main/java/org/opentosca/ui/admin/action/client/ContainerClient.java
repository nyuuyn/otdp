package org.opentosca.ui.admin.action.client;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.namespace.QName;

import org.apache.struts2.ServletActionContext;
import org.opentosca.model.consolidatedtosca.PublicPlan;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * Customized version of original ContainerClient by Markus to be used in a WAR
 * Frontend for openTosca.
 * 
 * @author Markus Fischer - fischema@studi.informatik.uni-stuttgart.de
 * @author Nedim Karaoguz - karaognm@studi.informatik.uni-stuttgart.de
 * @author Christian Endres - endrescn@studi.informatik.uni-stuttgart.de
 * 
 */
public class ContainerClient {

	public static URI BASEURI;
	private Client client;

	// Singleton Pattern
	private static final ContainerClient INSTANCE = new ContainerClient();

	public static ContainerClient getInstance() {
		return ContainerClient.INSTANCE;
	}

	/**
	 * To decode a encoded URL back to original
	 * 
	 * @param encodedURL
	 *            as String
	 * @return decoded URL
	 */
	public static String URLdecode(String encodedURL) {

		try {
			return URLDecoder.decode(encodedURL, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException();
		}
	}

	/**
	 * To Encode a URL
	 * 
	 * @param url
	 *            as String
	 * @return encoded URL
	 */
	public static String URLencode(String url) {

		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Constructor
	 */
	private ContainerClient() {

		ClientConfig config = new DefaultClientConfig();
		this.client = Client.create(config);
		this.client.setChunkedEncodingSize(1024);

		// We assume that the OpenTOSC container is running on port 1337 on the
		// same machine as the GUI Backend.
		// TODO Move Container API port and path information to e.g. external
		// properties file, should not be fixed in the code.
		String host = ServletActionContext.getRequest().getServerName();
		ContainerClient.BASEURI = UriBuilder.fromUri(
				"http://" + host + ":1337/containerapi").build();

	}

	public List<String> deleteCSAR(String csarId) {

		List<String> result = new ArrayList<String>();
		ClientResponse resp = null;
		try {
			resp = this.getBaseService().path("CSARs").path(csarId)
					.delete(ClientResponse.class);
			if (!resp.getClientResponseStatus().equals(Status.OK)) {
				System.out.println("Error occured while deleting Csar "
						+ csarId + ". Server returned: "
						+ resp.getClientResponseStatus());
				result.add("Delete Error, Server returned"
						+ resp.getClientResponseStatus());
			}
		} catch (ClientHandlerException e) {
			System.out
					.println("An Error occurred while deleting! Maybe the Container is not running or cannot be accessed.");
			System.out.println(e);
			result.add("An Error occurred while deleting! Maybe the Container is not running or cannot be accessed.");
		}
		return result;
	}

	public void destroy() {

		this.client.destroy();
	}

	public File downloadFile(String relativeFileLocation) {

		File ret = this.getGenericService(relativeFileLocation)
				.accept(MediaType.APPLICATION_OCTET_STREAM).get(File.class);
		System.out.println(ret.getName());
		System.out.println(ret.getTotalSpace());

		return ret;
	}

	/**
	 * @param input
	 * @param withSelf
	 * @return String
	 */
	private List<String[]> filterXlinkReferences(String input, Boolean withSelf) {

		// System.out.println(input);
		List<String[]> result = new ArrayList<String[]>();
		// get the References
		String[] subStrings = input.split("Reference");

		for (String sub : subStrings) {
			// it is only a Reference if it contains the xmlns
			if (sub.contains("xlink:")) {
				// get the xlink elements
				String[] subsubStrings = sub.split("xlink:");

				Boolean hadTitle = false;
				Boolean hadHref = false;

				String[] element = new String[2];

				for (String subsub : subsubStrings) {
					// get the title
					if (subsub.startsWith("title=")) {
						element[0] = this.getBetweenQuotes(subsub);
						hadTitle = true;
						// do only return selflinks if requested
						if (element[0].equals("Self") && !withSelf) {
							hadTitle = false;
						}
					}
					// get the link
					if (subsub.startsWith("href=")) {
						element[1] = this.getBetweenQuotes(subsub);
						hadHref = true;
					}
				}
				// return element only if it had the fields title and href
				if (hadTitle && hadHref) {
					// System.out.println("Adding: ");
					// this.printStringArray(element);
					result.add(element);
				}
			}
		}

		return result;
	}

	/**
	 * Provides a WebResource to send requests to the ContainerBaseUri
	 * 
	 * @return WebResource
	 */
	private WebResource getBaseService() {

		return this.client.resource(ContainerClient.BASEURI);
	}

	/**
	 * Returns the String between to Quotes. Input: "anything", Output:anything
	 * 
	 * @param s
	 * @return String
	 */
	public String getBetweenQuotes(String s) {

		int first = s.indexOf("\"") + 1;
		int last = s.lastIndexOf("\"");
		return s.substring(first, last);
	}

	/**
	 * Provides a WebResource to send requests to anyUri
	 * 
	 * @param URI
	 *            as String
	 * @return WebResource
	 */
	private WebResource getGenericService(String uri) {

		return this.client.resource(uri);
	}

	public List<String[]> getLinksFromUri(String uri, Boolean withSelf) {

		return this.filterXlinkReferences(
				this.getGenericService(uri).accept(MediaType.TEXT_XML)
						.get(String.class), withSelf);
	}

	public List<String[]> getLinksWithExtension(String pathExtension,
			Boolean withSelf) {

		List<String[]> list = null;
		try {
			list = this.filterXlinkReferences(
					this.getBaseService().path(pathExtension)
							.accept(MediaType.TEXT_XML).get(String.class),
					withSelf);
		} catch (ClientHandlerException e) {
			System.out
					.println("An Error occurred! Maybe the Container is not running or cannot be accessed!");
			System.out.println(e);
		}
		return list;
	}

	public List<String> getOperations(String csar) {

		String inputString = this.getBaseService().path("CSARControl")
				.path(ContainerClient.URLencode(csar)).path("Operations")
				.accept(MediaType.TEXT_PLAIN).get(String.class);
		String[] methods = inputString.split("&");
		List<String> response = new ArrayList<String>();
		for (String m : methods) {
			if (!m.isEmpty()) {
				response.add(m);
			}
		}

		return response;
	}

	/**
	 * Gets a specific PublicPlan xml from History.
	 * 
	 * @author Christian Endres - endrescn@studi.informatik.uni-stuttgart.de
	 * 
	 * @param csar
	 *            CSARID String
	 * @param internalInstanceID
	 *            the CSAR-Instance ID
	 * @param correlationID
	 *            the CorrelationID which identifies the PublicPlan in History
	 * @return PublicPlans
	 */
	public PublicPlan getPublicPlanFromHistory(String csar,
			String internalInstanceID, String correlationID) {

		WebResource src = this.getBaseService().path("CSARs").path(csar)
				.path("Instances").path(internalInstanceID).path("history")
				.path(correlationID);
		System.out.println(src.getURI());
		PublicPlan ret = src.accept(MediaType.APPLICATION_XML).get(
				PublicPlan.class);
		if (null == ret) {
			System.out.println("Did not get a PublicPlan!");
		} else {
			ret.getPlanID();
		}
		return ret;
	}

	/**
	 * Gets a specific PublicPlans xml for a certain CSAR and a plan type and
	 * ID.
	 * 
	 * @author Christian Endres - endrescn@studi.informatik.uni-stuttgart.de
	 * 
	 * @param csar
	 *            the CSARID String
	 * @param planType
	 *            the plan type String (BUILD, OTHERMANAGEMENT, TERMINATION)
	 * @param internalID
	 *            the ID which identifies the PublicPlan (i.e.: 1 for path
	 *            .../CSARs/TestCSAR.csar/PublicPlans/BUILD/1)
	 * @return PublicPlan
	 */
	public PublicPlan getPublicPlans(String csar, String planType,
			int internalID) {

		WebResource src = this.getBaseService().path("CSARs").path(csar)
				.path("PublicPlans").path(planType)
				.path(Integer.toString(internalID));
		System.out.println(src.getURI());
		PublicPlan ret = src.accept(MediaType.APPLICATION_XML).get(
				PublicPlan.class);
		if (null == ret) {
			System.out.println("Did not get a PublicPlan!");
		} else {
			ret.getPlanID();
		}
		return ret;
	}

	/**
	 * Gets a specific PublicPlan xml which is active at the moment.
	 * 
	 * @author Christian Endres - endrescn@studi.informatik.uni-stuttgart.de
	 * 
	 * @param csar
	 *            CSARID String
	 * @param internalInstanceID
	 *            the CSAR-Instance-ID
	 * @param correlationID
	 *            the CorrelationID which identifies the active PublicPlan
	 * @return PublicPlan
	 */
	public PublicPlan getRunningPublicPlan(String csar,
			String internalInstanceID, String correlationID) {

		WebResource src = this.getBaseService().path("CSARs").path(csar)
				.path("Instances").path(internalInstanceID)
				.path("activePublicPlans").path(correlationID);
		System.out.println(src.getURI());
		PublicPlan ret = src.accept(MediaType.APPLICATION_XML).get(
				PublicPlan.class);
		if (null == ret) {
			System.out.println("Did not get a PublicPlan!");
		} else {
			ret.getPlanID();
		}
		return ret;
	}

	public String[] getServiceTemplates(QName csarID) {

		String serviceTemplates = this.getBaseService().path("CSARControl")
				.path(ContainerClient.URLencode(csarID.toString()))
				.path("ServiceTemplates").accept(MediaType.TEXT_PLAIN)
				.get(String.class);
		return serviceTemplates.split("&");
	}

	public String getState(String csar) {

		return this.getBaseService().path("CSARControl")
				.path(ContainerClient.URLencode(csar)).path("DeploymentState")
				.accept(MediaType.TEXT_PLAIN).get(String.class);
	}

	public QName getThorQName(String thorURI) {

		QName thorID = null;
		try {
			String name = this.getGenericService(thorURI).path("QName")
					.accept(MediaType.TEXT_PLAIN).get(String.class);
			thorID = QName.valueOf(name);
		} catch (ClientHandlerException e) {
			System.out
					.println("An Error occurred! Maybe the Container is not running or cannot be accessed.");
			System.out.println(e);
		}
		return thorID;
	}

	public List<String> invokeMethod(QName thorID, String methodEnum) {

		System.out.println("Trying to invoke Method " + methodEnum
				+ " on ThorFile/Process with ID " + thorID.toString());
		ArrayList<String> result = new ArrayList<String>();
		ClientResponse resp = null;
		try {
			resp = this.getBaseService().path("CSARControl")
					.path(ContainerClient.URLencode(thorID.toString()))
					.post(ClientResponse.class, methodEnum);
			if (!resp.getClientResponseStatus().equals(Status.OK)) {
				System.out.println("Error occurred while invoking Method "
						+ methodEnum + ", Server returned: "
						+ resp.getClientResponseStatus());
				result.add("Invocation Error, Server returned: "
						+ resp.getClientResponseStatus());
			}
		} catch (ClientHandlerException e) {
			System.out
					.println("An Error occurred while invoking Method "
							+ methodEnum
							+ ". Maybe the Container is not running or cannot be accessed.");
			// System.out.println(e);
			e.printStackTrace();
			result.add("An Error occurred while invoking Method "
					+ methodEnum
					+ "! Maybe the Container is not running or cannot be accessed.");
		} catch (NullPointerException e) {
			result = null;
			System.out.println("An Error occurred while invoking Method "
					+ methodEnum);
		}
		return result;
	}

	public List<String> invokeMethod(QName csarID, String serviceTemplate,
			String methodEnum) {

		System.out.println("Trying to invoke Method " + methodEnum
				+ " on ThorFile/Process with ID " + csarID.toString());
		ArrayList<String> result = new ArrayList<String>();
		ClientResponse resp = null;
		try {
			resp = this
					.getBaseService()
					.path("CSARControl")
					.path(ContainerClient.URLencode(csarID.toString()))
					.post(ClientResponse.class,
							methodEnum + "&" + serviceTemplate);
			if (!resp.getClientResponseStatus().equals(Status.OK)) {
				System.out.println("Error occurred while invoking Method "
						+ methodEnum + ", Server returned: "
						+ resp.getClientResponseStatus());
				result.add("Invocation Error, Server returned: "
						+ resp.getClientResponseStatus());
			}
		} catch (ClientHandlerException e) {
			System.out
					.println("An Error occurred while invoking Method "
							+ methodEnum
							+ ". Maybe the Container is not running or cannot be accessed.");
			// System.out.println(e);
			e.printStackTrace();
			result.add("An Error occurred while invoking Method "
					+ methodEnum
					+ "! Maybe the Container is not running or cannot be accessed.");
		} catch (NullPointerException e) {
			result = null;
			System.out.println("An Error occurred while invoking Method "
					+ methodEnum);
		}
		return result;
	}

	/**
	 * Invokes a POST for a PublicPlan and a CSAR-Instance-ID.
	 * 
	 * @author Christian Endres - endrescn@studi.informatik.uni-stuttgart.de
	 * 
	 * @param publicPlan
	 *            the PublicPlan to invoke
	 * @param instanceID
	 *            the CSAR-Instance-ID
	 * @return list of status strings
	 */
	public List<String> postInstanceManagementInvocation(PublicPlan publicPlan,
			int instanceID) {

		System.out.println("manage instance " + instanceID + " for CSAR "
				+ publicPlan.getCSARID());

		ArrayList<String> result = new ArrayList<String>();
		ClientResponse resp = null;

		WebResource src = this.getBaseService().path("CSARs")
				.path(publicPlan.getCSARID()).path("Instances")
				.path(Integer.toString(instanceID));
		System.out.println("at URI " + src.getURI());
		resp = src.post(ClientResponse.class, publicPlan);

		System.out.println(resp.getClientResponseStatus());

		// resp = src.delete(ClientResponse.class);

		if (!resp.getClientResponseStatus().equals(Status.OK)) {
			System.out.println("Error occurred while invoking PublicPlan: "
					+ resp.getClientResponseStatus());
			result.add("Invocation Error, Server returned: "
					+ resp.getClientResponseStatus());
		} else {
			result.add("success");
		}
		return result;

	}

	/**
	 * DebugMethod to print a Array of Strings
	 * 
	 * @param subStrings
	 */
	public void printStringArray(String[] subStrings) {

		for (String sub : subStrings) {
			System.out.println(sub);
		}
	}

	/**
	 * PUT for a BUILD PublicPlan invocation.
	 * 
	 * @author Christian Endres - endrescn@studi.informatik.uni-stuttgart.de
	 * 
	 * @param publicPlan
	 *            the BUILD PublicPlan to invoke.
	 * @return list of status Strings
	 */
	public List<String> putPublicPlanBUILDInvocation(PublicPlan publicPlan) {

		System.out.println("post public plan " + publicPlan.getPlanID());

		ArrayList<String> result = new ArrayList<String>();
		ClientResponse resp = null;

		WebResource src = this.getBaseService().path("CSARs")
				.path(publicPlan.getCSARID()).path("Instances");
		resp = src.put(ClientResponse.class, publicPlan);
		System.out.println("Post of a PublicPlan on the URI " + src.getURI());

		if (!resp.getClientResponseStatus().equals(Status.OK)) {
			System.out.println("Error occurred while invoking PublicPlan: "
					+ resp.getClientResponseStatus());
			result.add("Invocation Error, Server returned: "
					+ resp.getClientResponseStatus());
		} else {
			result.add("success");
		}
		return result;

	}

	public List<String> uploadCSAR(String absoluteFilePath) {

		List<String> result = new ArrayList<String>();

		System.out.println("Trying to upload ThorFile from: "
				+ absoluteFilePath);

		File file = new File(absoluteFilePath);

		if (!file.exists()) {
			System.out.println("Error: file does not exist.");
			result.add("Error: file does not exist.");
			return result;
		}

		System.out.println("Size of the file to upload: "
				+ file.getTotalSpace());

		ClientResponse resp = null;
		FormDataMultiPart multiPart = new FormDataMultiPart();

		FormDataContentDisposition.FormDataContentDispositionBuilder dispositionBuilder = FormDataContentDisposition
				.name("file");
		dispositionBuilder.fileName(file.getName());
		dispositionBuilder.size(file.getTotalSpace());

		FormDataContentDisposition formDataContentDisposition = dispositionBuilder
				.build();

		multiPart.bodyPart(new FormDataBodyPart("file", file,
				MediaType.APPLICATION_OCTET_STREAM_TYPE)
				.contentDisposition(formDataContentDisposition));

		resp = this.getBaseService().path("CSARs")
				.type(MediaType.MULTIPART_FORM_DATA_TYPE)
				.post(ClientResponse.class, multiPart);

		result.add(resp.getClientResponseStatus().toString());

		return result;
	}

	public List<String> uploadCSARDueURL(String urlToUpload) {
		
		System.out.println("Try to send the URL to the ContainerAPI: " + ContainerClient.URLencode(urlToUpload));
		
		ArrayList<String> result = new ArrayList<String>();

		ClientResponse resp = this.getBaseService().path("CSARs").queryParam("url", urlToUpload)
				.post(ClientResponse.class);

		if (!resp.getClientResponseStatus().equals(Status.OK)) {
			System.out.println("Error occurred while uploading CSAR from URL "
					+ urlToUpload + ", Server returned: "
					+ resp.getClientResponseStatus());
			result.add("Invocation Error, Server returned: "
					+ resp.getClientResponseStatus());
		} else {
			result.add("Created");
		}
		return result;
	}
}
