<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>myTv</title>
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap-theme.min.css">
    </head>
    <body>


        <div class="container">
            <h1>myTv  <small>- hey watch it!</small></h1>
            <div class="row">
                <div class="col-md-3"></div>
                <div class="col-md-6">
                    <h4><u>Delete Show Data: </u></h4><br>
                            <c:forEach items="${showslist}" var="s">
                        <a href="delete?sid=${s}">${s} <span class="glyphicon glyphicon-trash"/> </a> <br/>
                    </c:forEach>
                    
                </div>
                <div class="col-md-3"></div>
            </div>
            <div class="well" style="margin-top: 50px;">
                <a class="btn btn-default" href="${pageContext.request.contextPath}/">Return <span class="glyphicon glyphicon-arrow-left"></span></a>
            </div>

        </div>
    </div>
    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.min.js"></script>
</body>
</html>
