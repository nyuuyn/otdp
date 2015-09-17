package org.opentosca.otdp.model;

import java.net.URL;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * @author Kálmán Képes - kepeskn@studi.informatik.uni-stuttgart.de
 *
 */
public class TaskState {

	private static final Logger LOGGER = Logger.getLogger(TaskState.class
			.getName());

	public enum State {
		DEPLOYINGCSAR, CSARDEPLOYED, INSTANTIATINGCSAR, CSARINSTANTIATED, ERROR
	}

	private final long id;
	private State currentState = State.DEPLOYINGCSAR;
	private String currentMessage = "CSAR is beeing downloaded";

	private URL csarUrl;
	private String otdpRequestUrl;

	private String sshPrivateKey;
	private String keyPairName;
	private String accesKey;
	private String regionEndpoint;
	private String secretKey;

	private String callbackId;

	private String applicationEndpoint;

	public TaskState(URL csarURL, String otdpRequestUrl, String sshPrivateKey,
			String keyPairName, String accessKey, String regionEndpoint,
			String secretKey) {
		this.id = System.currentTimeMillis();
		this.csarUrl = csarURL;
		this.otdpRequestUrl = otdpRequestUrl;
		this.sshPrivateKey = sshPrivateKey;
		this.keyPairName = keyPairName;
		this.accesKey = accessKey;
		this.regionEndpoint = regionEndpoint;
		this.secretKey = secretKey;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param currentState
	 *            the currentState to set
	 */
	public void setCurrentState(State currentState) {
		LOGGER.info("Changing state of task " + this.id + " to " + currentState);
		this.currentState = currentState;
	}

	/**
	 * @return the currentState
	 */
	public State getCurrentState() {
		return currentState;
	}

	/**
	 * @return the currentMessage
	 */
	public String getCurrentMessage() {
		return currentMessage;
	}

	/**
	 * @param currentMessage
	 *            the currentMessage to set
	 */
	public void setCurrentMessage(String currentMessage) {
		LOGGER.info("Changing message of task " + this.id + " to "
				+ currentMessage);
		this.currentMessage = currentMessage;
	}

	/**
	 * @return the csarUrl
	 */
	public URL getCsarUrl() {
		return csarUrl;
	}

	/**
	 * @return the sshPrivateKey
	 */
	public String getSshPrivateKey() {
		return sshPrivateKey;
	}

	/**
	 * @return the keyPairName
	 */
	public String getKeyPairName() {
		return keyPairName;
	}

	/**
	 * @return the accesKey
	 */
	public String getAccesKey() {
		return accesKey;
	}

	/**
	 * @return the regionEndpoint
	 */
	public String getRegionEndpoint() {
		return regionEndpoint;
	}

	/**
	 * @return the secretKey
	 */
	public String getSecretKey() {
		return secretKey;
	}

	/**
	 * @return the otdpRequestUrl
	 */
	public String getOtdpRequestUrl() {
		return otdpRequestUrl;
	}

	/**
	 * @return the callbackId
	 */
	public String getCallbackId() {
		return callbackId;
	}

	/**
	 * @param callbackId
	 *            the callbackId to set
	 */
	public void setCallbackId(String callbackId) {
		this.callbackId = callbackId;
	}

	/**
	 * @return the applicationEndpoint
	 */
	public String getApplicationEndpoint() {
		return applicationEndpoint;
	}

	/**
	 * @param applicationEndpoint
	 *            the applicationEndpoint to set
	 */
	public void setApplicationEndpoint(String applicationEndpoint) {
		this.applicationEndpoint = applicationEndpoint;
	}

}
