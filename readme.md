# Transfer-Hub

Transfer-Hub is an Alpha solution for setting up file transfers between systems. Currently it consists of a back-end and a front-end. Both repositories are added into this main repository.


## Contents
- [Installation](#installation)
- [Using the solution](#Using the solution)


## Installation
This section describes what elements need to be installed to make sure that you can contribute or continue development.
#### Back-end installation
The back-end is based on Java 17 and Spring Boot. To get it to work you will need to use the Development-Deploy script that is in the Docker directory. 
Please note that alongside the back-end, database and the front-end, also a reverse-proxy will be installed to ensure connectivity and a working product. For more information, please head over to the deploymment script.
This script will install everything in Docker, as such this is required to have running on your target system.


If you don't want to use a permanent database, You can use the H2 in-memory database. Make sure to add the H2 database for debugging purposes by adding it to the application.properties. This file is inside the core directory of the Docker directory.

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

Please note, using the MySQL database requires the following code to be added, do not add both, unless working with a Test-Profile:
```properties
spring.datasource.url=jdbc:mysql://<Server and port>/transfer_hub
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=<password>
```

#### Transfer Project Structure
The application object-oriented and thus set-up in different sections.

##### Configuration
Contains the configuration elements needed for the Transfer-Hub to run. Currently this is a list of the Active Routes in the solution.

##### Controller
The controller is used to accept HTTP-Requests to create, update, read or delete routes in the database and Application.

##### Factory
The factory is the section that is responsible for dynamically creating routes and endpoints in the application.

- **Endpoint** is used to create endpoints that are used in the route.
- **Processor** is used to define behaviour in the transfers. Like handling additional errors, logging and/or event registration.
- **Transfers** is used to create the transfers based on the Processors and Endpoints in the other modules.

##### Model
The model consists of entitites, POJO's and Data Transfer Objects (DTO).

##### Parsers
The parsers are used to create Endpoint url's from different connector types. Like SFTP, Azure-Blob, Azure-Files and SMB.

##### Repository
Are the JPA interfaces for interacting with the database.

##### Service
Contains all the services to translate data from database to definitive routes. However also contains code to manage the CamelContext.

##### utilities
The utilities are the static code that is used within multiple places in the code.


## Using the solution
When you are not interested in development, but using the solution, please follow this chapter on how to install it.

For installation purposes scripts have been created for both Linux and Windows based systems to install 4 containers in Docker. Alternativly it is possible to also install a Splunk container. 
However, currently this is not yet running, Splunk would need to be configured seperatly. Please note that the logging will be stored in the logs directory in the transfer-hub-core directory within the Docker directory.

The script will install the database, the front-end, a back-end (core) and a reverse-proxy, these containers will be running in a seperate Docker network. Please note that Docker needs to be installed for this to work. 

Before installation, there are a few options to be set.

- **First** head to the Docker directory and into the Dockerfile, change the root password to something of your liking and save the file.
- **Second** head to the transfer-hub-core directory and go to the applications.properties file. Only change the username and password if needed, changing the database port and server is recommended for experienced users as it includes changing the deployment scripts.
- **Third** return to the Docker directory and execute the script to your liking.

**Please note**, when using the script, it will install the Transfer-Hub in a Docker environment on the system that you are using.

You can reach the application by <ip-address of system it is installed on>:9096, unless you changed the ports in the docker commands.
