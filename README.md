# country-covid

# About 
This Android application retrieves user selected country's COVID-19 deaths, confirmed, and recovered statistics using a 3rd party API. The web service is deployed to Heroku and is also connected to MongoDB Atlas, which logs user activity and queries on the Android application. Furthermore, a web browser displays the logs of user activity for further analysis through HTTP request. The project implements a MVC model. 

# Android Application 
The application uses HTTP POST and GET requests. The POST is used for handling Android requests because it is making changes to the MongoDB by logging the request/reply information. GET is used by the dashboard to get data from MongoDB database. 

Here is a screenshot of the initial application layout:\
![image](https://github.com/kowunk/country-covid/blob/master/androidapp1.png)

The application takes in user selection input using a spinner that is populated with country names. Below is a screenshot of the country list that is shown when user clicks on the spinner. The default country is Afghanistan, which is the first country on the list.
![image](https://github.com/kowunk/country-covid/blob/master/androidapp2.png)

Here is a screenshot of when user selects Argentina:\
![image](https://github.com/kowunk/country-covid/blob/master/androidapp3.png)

# MongoDB
Several Android application user activity data are stored into MongoDB: 
1) Time request was made
2) Time request was completed 
3) Phone model making the request 
4) Request Type – for 1) populating country list, this will be “Initialization Data” and for 2) getting COVID
data for a country, this will be “Country Data”. 
5) Request – for 1) populating country list, this will be “countries” and for 2) getting COVID data for a
country, this will be the country name. 
6) Reply – for 1) populating country list, this will be “country list returned” and for 2) getting COVID data
for a country, this will be the country’s confirmed, deaths, and recovered COVID data. 

The following is a screenshot from MongoDB Atlas that shows the logged information.
![image](https://github.com/kowunk/country-covid/blob/master/mongodb.png)

# Web Dashboard
The data are retrieved from MongoDB Atlas in JSON using HTTP GET request, but displayed into readable format. The web service is also deployed to Heroku. The web dashboard displays the following operation analytics: 

1) Top three countries searched
2) Average request completion time (in milliseconds) 
3) Top three phone models used \

If there are no three distinct elements for operation analytics 1) and 3), only the top < 3 elements are displayed. 

Below is a screenshot of the web dashboard: 
![image](https://github.com/kowunk/country-covid/blob/master/web-dashboard.png)
