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
                <div class="col-md-6">
                    <h4><u>Latest Unwatched Episodes</u></h4><br>
                            <c:forEach items="${unwatchedshows}" var="ushow">
                                <c:forEach items="${ushow.unwatched_ids}" var="uw">
                                    <c:set var="splituw" value="${fn:split(uw, ',')}"></c:set>
                            <a href="${baseurl}${ushow._id}">${ushow._id}</a> // 
                            <a href="${baseurl}${ushow._id}/${splituw[0]}.html#${splituw[1]}" target="_blank">${splituw[0]}#${splituw[1]}</a>
                            <a style="margin-left: 10px;" href="seen?eid=${splituw[2]}&_id=${ushow._id}" title="Mark as Seen"><span class="glyphicon glyphicon-eye-open"></span></a> <br>
                            </c:forEach><br>
                    </c:forEach>

                </div>
                <div class="col-md-6">
                    <h4><u>Upcoming Episodes</u></h4>
                            <c:forEach items="${nextepisodes}" var="nextepi">
                                <c:set var="splitne" value="${fn:split(nextepi.nexteid, ',')}"></c:set>
                        <a href="${baseurl}${nextepi._id}">${nextepi._id}</a> // 
                        <a href="${baseurl}${nextepi._id}/${splitne[0]}.html#${splitne[1]}">${splitne[0]}#${splitne[1]}</a> - <fmt:formatDate value="${nextepi.nextepidate}" pattern="EEE dd MMM"/><br>
                    </c:forEach>

                </div>
            </div>
            <div class="well" style="margin-top: 50px;">
                <form method="GET" action="add" class="form-inline">
                    <input type="text" name="_id" class="form-control">
                    <button class="btn btn-default" type="submit">Add <span class="glyphicon glyphicon-plus"></span></button> // 
                    <a class="btn btn-default" href="update">Update Shows <span class="glyphicon glyphicon-refresh"></span></a> 
                </form>
            </div>

        </div>
        <!-- http://www.free-tv-video-online.me/search/filter.php?inside=0&movie=2&pos=0&mask=A 
        http://www.free-tv-video-online.me/search/filter.php?movie=2&mask=s
        -->
    </div>
    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.min.js"></script>
</body>
</html>
