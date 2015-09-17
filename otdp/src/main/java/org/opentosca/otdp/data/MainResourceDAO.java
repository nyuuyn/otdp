package org.opentosca.otdp.data;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * @author Kálmán Képes - kepeskn@studi.informatik.uni-stuttgart.de
 *
 */
public class MainResourceDAO {

	public final String containerAddress;
	public final String sshPrivateKey;
	public final String secretKey;
	public final String keyPairName;
	public final String accessKey;
	public final String regionEndpoint;

	public MainResourceDAO(Configuration configuration) {
		this.containerAddress = configuration.getContainerAddress();
		this.sshPrivateKey = configuration.getSshPrivateKey();
		this.secretKey = configuration.getSecretKey();
		this.keyPairName = configuration.getKeyPairName();
		this.accessKey = configuration.getAccessKey();
		this.regionEndpoint = configuration.getRegionEndpoint();
	}

	public String getContainerAddress() {
		return containerAddress;
	}

	public String getSshPrivateKey() {
		return sshPrivateKey;
	}

	public String getKeyPairName() {
		return this.keyPairName;
	}

	public String getAccessKey() {
		return this.accessKey;
	}

	public String getSecretKey() {
		return this.secretKey;
	}

	public String getRegionEndpoint() {
		return this.regionEndpoint;
	}

}
