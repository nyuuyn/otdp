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

	<h4>Task</h4>
	<p>
		ID: ${it.id} <br /> 
		State: ${it.currentState}<br /> 
		Message: ${it.currentMessage}<br />
		CSAR Url: ${it.csarUrl}<br />
		ApplicationEndpoint: ${it.applicationEndpoint}<br/>
	</p>

</body>
</html>
