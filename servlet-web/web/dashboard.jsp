<%--
  Created by Ko-Wun Kim
  Andrewid: kowunk
  Last Modified: 4/8/2021
  Dashboard JSP file to display log and operation analytics data.
--%>
<%@ page import="org.bson.Document" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>

<head>
    <title>Title</title>

</head>
<body style="font-family: sans-serif">
<h1>Country COVID-19 App Dashboard</h1>
<h2><u>Operation Analytics </u></h2>
<%--generating HTMl table is taken from: https://www.w3schools.com/html/tryit.asp?filename=tryhtml_table--%>
<p><b>Top Three Countries Searched: </b></p>

<%if (request.getAttribute("topThreeCountries")!=null) {
    ArrayList<String> topThree = (ArrayList<String>) request.getAttribute("topThreeCountries");
    for (int i=0; i<topThree.size(); i++){
        out.println(i+1+". "+topThree.get(i));
    }
}%>
<p><b>Average Request Completion Time: </b><%=request.getAttribute("averageTime")%></p>
<p><b>Top Three Phone Models Used: </b></p>
<%if (request.getAttribute("topThreeModels")!=null) {
    ArrayList<String> topThree = (ArrayList<String>) request.getAttribute("topThreeModels");
    for (int i=0; i<topThree.size(); i++){
        out.println(i+1+". "+topThree.get(i));
    }
}%>
<br><br>
<h2><u>Data Log</u></h2>
<form action="covidAppDashboard" method="GET">
    <table id="dataLogTable" style="width:100%">
        <tr>
            <th>Request Time</th>
            <th>Request Complete Time</th>
            <th>Phone Model</th>
            <th>Request Type</th>
            <th>Request</th>
            <th>Reply</th>
        </tr>
        <%
        ArrayList<Document> dataLogList = (ArrayList<Document>) request.getAttribute("dataLog");
        for (int i=0; i<dataLogList.size(); i++){ //go through rows of data
            if (dataLogList.get(i).get("Request Type")!=null && dataLogList.get(i).get("Request Type").toString().equalsIgnoreCase("Country Data")){
                Document reply = (Document) dataLogList.get(i).get("Reply");
                if (reply!=null){
                    String confirmed = (String) reply.get("confirmed");
                    String recovered = (String) reply.get("recovered");
                    String deaths = (String) reply.get("deaths");
                    out.println("<tr><td>"+dataLogList.get(i).get("Request Time")+"</td><td>"+dataLogList.get(i).get("Request Complete Time")+"</td><td>"
                            +dataLogList.get(i).get("Phone Model")+"</td><td>"+dataLogList.get(i).get("Request Type")+"</td><td>"
                            +dataLogList.get(i).get("Request Country")+"</td><td>"+"confirmed: "+confirmed+" deaths: "+deaths+" recovered: "+recovered
                            +"</td><tr>");
                }
            } else{
                out.println("<tr><td>"+dataLogList.get(i).get("Request Time")+"</td><td>"+dataLogList.get(i).get("Request Complete Time")+"</td><td>"
                        +dataLogList.get(i).get("Phone Model")+"</td><td>"+dataLogList.get(i).get("Request Type")+"</td><td>"
                        +dataLogList.get(i).get("Request")+"</td><td>"+dataLogList.get(i).get("Reply")
                        +"</td><tr>");

            }

        }

        %>
    </table>

</form>

</body>
</html>
