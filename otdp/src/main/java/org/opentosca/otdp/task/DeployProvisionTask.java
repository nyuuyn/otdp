package org.opentosca.otdp.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.eclipse.winery.model.selfservice.ApplicationOption;
import org.opentosca.otdp.data.Configuration;
import org.opentosca.otdp.model.TaskState;
import org.opentosca.otdp.model.TaskState.State;
import org.opentosca.ui.admin.action.client.ContainerClient;
import org.opentosca.ui.vinothek.ApplicationInstantiationServlet;
import org.opentosca.ui.vinothek.CallbackManager;
import org.opentosca.ui.vinothek.VinothekContainerClient;
import org.opentosca.ui.vinothek.model.ApplicationInstance;
import org.opentosca.ui.vinothek.model.ApplicationWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.zeroturnaround.zip.ZipUtil;

import ch.qos.logback.classic.db.names.TableName;

/**
 * @author Kálmán Képes - kepeskn@studi.informatik.uni-stuttgart.de
 *
 */
public class DeployProvisionTask implements Runnable {

	private static final Logger LOGGER = Logger
			.getLogger(DeployProvisionTask.class.getName());

	private TaskState currentState;

	public DeployProvisionTask(TaskState newTaskState) {
		this.currentState = newTaskState;
	}

	@Override
	public void run() {
		// fetch CSAR URL
		URL csarURL = this.currentState.getCsarUrl();

		// download csar:

		// FIXME: As we don't want to break with the current release of the
		// contaienr we will
		// download the CSAR first.
		// This comes due the fact that the container can't handle URL's such as
		// ../BPELStack/?csar, it calculates the filename from the URL wrong
		// (last path part = filename).
		//
		// For example it rather should download the file first and check with
		// the http headers
		// for the file name.

		Client csarDownloadClient = ClientBuilder.newClient();
		Response csarDownloadResponse = csarDownloadClient
				.target(csarURL.toString()).request().get();
		LOGGER.info("CSAR GET Request MediaType: "
				+ csarDownloadResponse.getMediaType().getType()
				+ csarDownloadResponse.getMediaType().getSubtype());

		for (String header : csarDownloadResponse.getHeaders().keySet()) {
			LOGGER.info(header + " : "
					+ csarDownloadResponse.getHeaderString(header));
		}

		String fileName = csarDownloadResponse.getHeaderString(
				"Content-Disposition").split(";")[1].split("=")[1].replace(
				"\"", "").trim();

		LOGGER.info("Downloading file " + fileName);
		InputStream csarDownloadInputStream = (InputStream) csarDownloadResponse
				.getEntity();

		Path csarDownloadTempPath = null;
		try {
			csarDownloadTempPath = Paths.get(Files.createTempDirectory("otdp")
					.toString(), fileName);
			FileUtils.copyInputStreamToFile(csarDownloadInputStream,
					csarDownloadTempPath.toFile());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (csarDownloadTempPath == null) {
			LOGGER.severe("Couldn't download CSAR from URL " + csarURL);
			this.currentState.setCurrentState(TaskState.State.ERROR);
			this.currentState
					.setCurrentMessage("Couldn't download CSAR from URL "
							+ csarURL);
			return;
		}

		ContainerClient client = ContainerClient.getInstance();
		try {
			client.BASEURI = UriBuilder.fromUri(
					Configuration.getInstance().getContainerAddress()).build();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UriBuilderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// send to container for deployment
		// List<String> response = client.uploadCSARDueURL(csarURL.toString());
		//
		// LOGGER.info("UploadCSAR with URL" + csarURL
		// + " got following response:");
		// for (String resp : response) {
		// LOGGER.info(resp);
		// }

		List<String> response = client.uploadCSAR(csarDownloadTempPath.toFile()
				.toString());
		LOGGER.info("UploadCSAR with URL" + csarURL
				+ " got following response:");
		for (String resp : response) {
			LOGGER.info(resp);
		}

		// get csars
		// get all the CSARID links
		List<String[]> csarsResponse = client.getLinksFromUri(
				ContainerClient.BASEURI + "/CSARs", false);

		List<String> csarIds = new ArrayList<String>();
		// extract the CSARIDs and cut off the ".csar" at the end

		for (String[] csarId : csarsResponse) {
			csarIds.add(csarId[0]);
		}

		String csarId = null;

		for (String foundCsarId : csarIds) {
			if (fileName.equals(foundCsarId)) {
				csarId = foundCsarId;
			}
		}

		if (csarId == null) {
			LOGGER.severe("Couldn't find CSARId for file " + fileName);
			this.currentState.setCurrentState(TaskState.State.ERROR);
			this.currentState
					.setCurrentMessage("Couldn't find CSARId for file "
							+ fileName);
		}

		String vinothekClientContainerApiUrl = Configuration.getInstance()
				.getContainerAddress();

		if (vinothekClientContainerApiUrl.contains("/containerapi")) {
			vinothekClientContainerApiUrl = vinothekClientContainerApiUrl
					.replace("/containerapi", "");
		}

		// find csar entry servicetemplate
		VinothekContainerClient vinoClient = new VinothekContainerClient(
				new VinothekClientConstructorHttpRequest(
						vinothekClientContainerApiUrl));

		Map<String, ApplicationWrapper> appMap = vinoClient.getApplications();

		for (String key : appMap.keySet()) {
			ApplicationWrapper app = appMap.get(key);
			LOGGER.info("Found application " + app.getDisplayName()
					+ " in CSAR " + app.getCsarName() + " with key " + key);
			if (app.getCsarName().equals(fileName)) {
				LOGGER.info("Using application " + app.getDisplayName());

				ApplicationInstantiationServlet instantiationServlet = new ApplicationInstantiationServlet();

				String container = vinothekClientContainerApiUrl;
				String applicationId = key;
				ApplicationOption selectedOption = this
						.findFirstValidOption(app);
				String optionId = selectedOption.getId();

				String xml = vinoClient.get(app,
						selectedOption.getPlanInputMessageUrl());

				try {
					xml = this.replaceInputData(xml);
				} catch (XPathExpressionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (TransformerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				String otdpEndpoint = this.currentState.getOtdpRequestUrl();

				VinothekClientAppInstantiationHttpRequest vinoAppInstantiationRequest = new VinothekClientAppInstantiationHttpRequest(
						container, applicationId, optionId, xml, otdpEndpoint);
				VinothekClientAppInstantiationHttpResponse vinoAppInstantiationResponse = new VinothekClientAppInstantiationHttpResponse();
				try {
					instantiationServlet.doGet(vinoAppInstantiationRequest,
							vinoAppInstantiationResponse);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// check with callbackmanager
				String callbackId = null;
				try {
					// fetch callbackId
					callbackId = vinoAppInstantiationResponse.getWriter()
							.toString().split("\\?")[1].split("=")[1];

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (ApplicationInstance instance : CallbackManager
						.getAllInstances()) {

					if (instance.getCallbackId().equals(callbackId)) {
						LOGGER.info("Found ApplicationInstance");

						LOGGER.info("Instance AppId: "
								+ instance.getApplicationId());
						LOGGER.info("Instance CallbackId: "
								+ instance.getCallbackId());
						LOGGER.info("Instance ContainerUrl: "
								+ instance.getContainerUrl());
						LOGGER.info("Instance EndpointUrl: "
								+ instance.getEndpointUrl());
						LOGGER.info("Instance Message: "
								+ instance.getMessage());
						LOGGER.info("Instance SelfserviceMessage: "
								+ instance.getSelfserviceMessage());
						LOGGER.info("Instance SelfserviceStatus: "
								+ instance.getSelfserviceStatus());
						this.currentState.setCallbackId(callbackId);
						break;
					}
				}

			}

		}

		// find buildplan
		// instantiate
		// publish app endpoint
	}

	private String replaceInputData(String xml)
			throws XPathExpressionException, TransformerException {

		InputSource source = new InputSource(new StringReader(xml));

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		String xpathQuerySoapBodyChild = "/*/*[local-name()='Body']/*[1]";

		Node soapBodyDataNode = (Node) xpath.evaluate(xpathQuerySoapBodyChild,
				source, XPathConstants.NODE);

		if (soapBodyDataNode.getNodeType() == Node.ELEMENT_NODE) {
			Element dataElement = (Element) soapBodyDataNode;

			NodeList childNodes = dataElement.getChildNodes();

			for (int index = 0; index < childNodes.getLength(); index++) {
				Node childNode = childNodes.item(index);
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					Element childElement = (Element) childNode;

					String inputDataValue = childElement.getTextContent();
					String inputParam = null;

					switch (childElement.getLocalName()) {
					case "sshKey":
						inputParam = this.currentState.getSshPrivateKey();
						break;
					case "accessKey":
						inputParam = this.currentState.getAccesKey();
						break;
					case "regionEndpoint":
						inputParam = this.currentState.getRegionEndpoint();
						break;
					case "secretKey":
						inputParam = this.currentState.getSecretKey();
						break;
					case "keyPairName":
						inputParam = this.currentState.getKeyPairName();
						break;
					default:
						break;
					}

					if (inputDataValue.contains("Please fill in")
							&& inputParam != null && !inputParam.isEmpty()) {
						childElement.setTextContent(inputParam);
					}
				}
			}
		}
		// sshKey, accessKey, regionEndpoint, secretKey, keyPairName

		StringWriter sw = new StringWriter();
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();

		transformer.transform(
				new DOMSource(soapBodyDataNode.getOwnerDocument()),
				new StreamResult(sw));
		return sw.toString();

	}

	private ApplicationOption findFirstValidOption(ApplicationWrapper app) {
		for (ApplicationOption option : app.getOptions().getOption()) {
			if (option.getPlanInputMessageUrl() != null
					&& !option.getPlanInputMessageUrl().trim().isEmpty()) {
				return option;
			}
		}
		return null;
	}

	private String xpathQueryTargetDefinitions() {
		return "/*[local-name()='Definitions']";
	}

	private String xpathQueryTargetDA(QName serviceTemplate,
			String nodeTemplate, QName artifactType, String deploymentArtifact) {
		return this.xpathQueryTargetDefinitions()
				+ "/*[local-name()='ServiceTemplate' and @id='"
				+ serviceTemplate.getLocalPart()
				+ "']/*[local-name()='TopologyTemplate']/*[local-name()='NodeTemplate' and @id='"
				+ nodeTemplate
				+ "']/*[local-name()='DeploymentArtifacts']/*[local-name()='DeploymentArtifact' and @name='"
				+ deploymentArtifact + "']";
	}

}
