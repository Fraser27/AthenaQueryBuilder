Athena Query Builder
=====================

### Steps to Run the Athena Query Builder Project

* Create the springboot jar file using maven clean install dev

* Option 1: Go to the swagger url to test the Athena Query Generator
   http://localhost:8080/athena-query-generator/swagger-ui.html#/athena-query-generation-controller/getAthenaQueryUsingPOST
   Add some brands in the list example ["Nokia", "Tesla"] and hit execute.

* Option 2: CURL script -> curl -X POST "http://localhost:8080/athena-query-generator/generate/athena/query?fromDate=2020-01-01&toDate=2020-04-14" -H "accept: */*" -H "Content-Type: application/json" -d "[ \"Nokia\", \"Tesla\"]"

!https://github.com/Fraser27/AthenaQueryBuilder/blob/master/src/main/resources/swaggersample.PNG