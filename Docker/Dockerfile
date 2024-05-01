#Create MySQL Image for JSP Tutorial Application
FROM mysql
MAINTAINER you
ENV MYSQL_ROOT_PASSWORD Wachtwoord1
ADD initialize.sql /docker-entrypoint-initdb.d
EXPOSE 3306
