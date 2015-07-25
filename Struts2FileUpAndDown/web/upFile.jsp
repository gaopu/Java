<%@ taglib prefix="s" uri="/struts-tags" %>
<%--
  Created by IntelliJ IDEA.
  User: geekgao
  Date: 15-7-25
  Time: 上午10:29
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
</head>
<body>
<form action="file_up" method="post" enctype="multipart/form-data">
    <s:file name="file" label="上传"></s:file>
    <s:submit type="submit"></s:submit>
</form>
<s:debug></s:debug>
</body>
</html>
