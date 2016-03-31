Karma Modeling REST API
================================


Karma modeling services are RESTful Web services developed using Jersey framework. The basepath for the service is [http://localhost:8080/KarmaModelingService/rest](http://localhost:8080/KarmaModelingService/rest).

The source code is at src/main/java, and the services are located in the package: edu.isi.modeling.resources. The config file, which includes the path for the server home directory, is src/main/resources/modeling.properties.

The application is set up with Swagger. Swagger automatically reads the API annotations from the package edu.isi.modeling.resources and generates Swagger description at [http://localhost:8080/KarmaModelingService/rest/swagger.json](http://localhost:8080/KarmaModelingService/rest/swagger.json). The link to the swagger UI is [http://localhost:8080/KarmaModelingService/swagger](http://localhost:8080/KarmaModelingService/swagger).
