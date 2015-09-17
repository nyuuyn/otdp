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

	<h4>Tasks:</h4>
	<p>
		<c:forEach var="task" items="${it.tasks}">
			ID: <a href="./tasks/${task.id}">${task.id}</a>
			<br />
			State: ${task.currentState}<br />
			Message: ${task.currentMessage}<br />
			CSAR Url: ${task.csarUrl}<br />
			<br />
		</c:forEach>

	</p>

</body>
</html>
