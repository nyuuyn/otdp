package org.opentosca.otdp.data;

/**
 * @author Kálmán Képes - kepeskn@studi.informatik.uni-stuttgart.de
 *
 */
public class Configuration {

	private static String wineryAddress = "http://dev.winery.opentosca.org/winery/";
	private static String containerAddress = "http://localhost:1337/containerapi";

	// never
	private static String sshPrivateKey = "";
	private static String secretKey = "";
	
	private static String keyPairName = "KalleKeyPair";
	private static String accessKey = "opentosca.kalman";

	private static String regionEndpoint ="129.69.209.127";

	private Configuration() {
	}

	private static class SingletonHolder {
		private static final Configuration INSTANCE = new Configuration();
	}

	public static Configuration getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * @return the wineryAddress
	 */
	public String getWineryAddress() {
		return wineryAddress;
	}

	/**
	 * @param wineryAddress
	 *            the wineryAddress to set
	 */
	public void setWineryAddress(String wineryAddress) {
		Configuration.wineryAddress = wineryAddress;
	}

	/**
	 * @return the containerAddress
	 */
	public String getContainerAddress() {
		return containerAddress;
	}

	/**
	 * @param containerAddress
	 *            the containerAddress to set
	 */
	public void setContainerAddress(String containerAddress) {
		Configuration.containerAddress = containerAddress;
	}

	/**
	 * @return the sshPrivateKey
	 */
	public static String getSshPrivateKey() {
		return sshPrivateKey;
	}

	/**
	 * @return the secretKey
	 */
	public static String getSecretKey() {
		return secretKey;
	}

	/**
	 * @return the keyPairName
	 */
	public static String getKeyPairName() {
		return keyPairName;
	}

	/**
	 * @return the accessKey
	 */
	public static String getAccessKey() {
		return accessKey;
	}

	/**
	 * @return the regionEndpoint
	 */
	public static String getRegionEndpoint() {
		return regionEndpoint;
	}
}
