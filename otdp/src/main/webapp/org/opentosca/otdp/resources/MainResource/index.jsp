<%@ page language="java" contentType="text/html; charset=US-ASCII"
	pageEncoding="US-ASCII"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>OpenTOSCA Deploy and Provision</title>
</head>
<body>
	<h1>OpenTOSCA Deploy and Provision</h1>

	<h4>Configuration:</h4>
	<p>
	<form action="./configuration" method="post"
		enctype="multipart/form-data">
		Container Address: <input type="text" value="${it.containerAddress}"
			name="containerAddress" /> <input type="submit" value="Update" /><br />
	</form>
	</p>

	<h4>CSAR Upload:</h4>

	<p>
	
	<form id="deployAndInstantiateForm" action="." method="post" enctype="multipart/form-data">
		SSHPrivateKey/VMPrivateKey: <textarea form="deployAndInstantiateForm" name="sshPrivateKey" rows="1"></textarea><br /> 
		KeyPairName/VMKeyPairName: <input type="text" name="keyPairName" value="${it.keyPairName}" /><br />
		AccessKey/HypervisorUserName: <input type="text" name="accessKey" value="${it.accessKey}" /><br />
		RegionEndpoint/HypervisorEndpoint: <input type="text" name="regionEndpoint" value="${it.regionEndpoint}" /><br /> 
		SecretKey/HypervisorUserPassword: <input type="text" name="secretKey" value="${it.secretKey}" /><br /> 
		CSAR Address: <input type="text" name="csarAddress" /><br /> 
		<input type="submit" value="Upload" /><br />
	</form>
	</p>



	</form>
</body>
</html>
