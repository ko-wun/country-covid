import org.bson.Document;
import org.json.JSONObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Author: Ko-Wun Kim
 * Last Modified: April 8, 2021
 *
 * This is a web application that gets appropriate data from 3rd party API, logs data to MongoDB, and present log information
 * on web-based dashboard as well.
 * Unlike Project 4 Task 1, this servlet uses POST and GET requests.
 * 1) POST request handles Android requests because as Android request is fulfilled, it is making changes to MongoDB
 * 2) GET request handles retrieving data from MongoDB for the web-based dashboard.
 */
@WebServlet(name = "CountryCovidServlet",
        urlPatterns = {"/covidAppDashboard"})
public class Project4Task2_CountryCovidServlet extends HttpServlet {

    Project4Task2_CountryCovidModel ccm = null;

    @Override
    public void init() { ccm = new Project4Task2_CountryCovidModel(); }

    /**
     * Receives Android request with the following arguments:
     * time: time request was made.
     * model: phone model making the request.
     * param: request that is being made. If this is equal to "countries", returns list of countries. If not, return country's COVID data.
     * It then sends the arguments to the model, which retrieves response to send back to Android as well as logs onto MongoDB .
     * @param request
     * @param response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

        String time = request.getParameter("time"); // time request was made
        String model = request.getParameter("model"); // phone model making the request
        String requestString = request.getParameter("param"); //request information

        if (requestString!=null){ //if request is made
            JSONObject result = null;
            if (requestString.equals("countries")){ //return list of countries
                result = ccm.getCountries(time, requestString, model);
            } else { //return requested country's data
                result = ccm.getCovidData(time, requestString, model);

                }

            //send JSON data back to android
            //The following part is from https://www.baeldung.com/servlet-json-response
            try{
                PrintWriter out = response.getWriter();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                out.write(result.toString());
                out.flush();
                out.close();
            } catch (IOException e){ //handles IOException
                System.out.println("Unable to send response");
            }

        }
    }

    /**
     * Gets data from MongoDB to display on web-based dashboard.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //all data log from MongoDB
        ArrayList<Document> log = ccm.receiveMongoDB();
        request.setAttribute("dataLog",log);

        //top three countries that was searched
        ArrayList<String> topThreeCountries = ccm.topThreeSearched(log);
        request.setAttribute("topThreeCountries", topThreeCountries);

        //average time it took to complete request
        String average = String.valueOf(ccm.averageResponseTime(log));
        request.setAttribute("averageTime", average); //in milliseconds

        //top three phone models that made request
        ArrayList<String> topThreeModels = ccm.topThreeModels(log);
        request.setAttribute("topThreeModels", topThreeModels);

        //transfer view to the dashboard
        RequestDispatcher view = request.getRequestDispatcher("dashboard.jsp");
        view.forward(request, response);

    }


}
