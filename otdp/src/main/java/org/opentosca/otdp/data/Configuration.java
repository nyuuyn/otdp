package org.opentosca.otdp.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Kálmán Képes - kepeskn@studi.informatik.uni-stuttgart.de
 *
 */
public class Configuration {

	/**
	 * The name of the properties file.
	 */
	private static final String PROPERTIES_FILENAME = "otdp.config.properties";

	/**
	 * The properties as loaded from the file system.
	 */
	private static Properties properties = new Properties();

	private static String wineryAddress = "http://dev.winery.opentosca.org/winery/";
	private static String containerAddress = "http://localhost:1337/containerapi";

	// never
	private static String sshPrivateKey = "";
	private static String secretKey = "";

	private static String keyPairName = "KalleKeyPair";
	private static String accessKey = "opentosca.kalman";

	private static String regionEndpoint = "129.69.209.127";

	private Configuration() {
	}

	static {
		InputStream inputStream = null;
		try {
			inputStream = Configuration.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME);
			if (inputStream == null) {
				throw new FileNotFoundException();
			}
			properties.load(inputStream);
		} catch (IOException e) {

		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {

			}
		}
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
		return this.properties.getProperty("otdp.winery.address");
	}

	/**
	 * @param wineryAddress
	 *            the wineryAddress to set
	 */
	public void setWineryAddress(String wineryAddress) {
		this.properties.setProperty("otdp.winery.address", containerAddress);
	}

	/**
	 * @return the containerAddress
	 */
	public String getContainerAddress() {
		return this.properties.getProperty("otdp.container.address");
	}

	/**
	 * @param containerAddress
	 *            the containerAddress to set
	 */
	public void setContainerAddress(String containerAddress) {
		this.properties.setProperty("otdp.container.address", containerAddress);
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
