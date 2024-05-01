FROM openjdk
VOLUME /tmp
VOLUME /logs
EXPOSE 8080
ARG JAR_FILE=*.jar
ARG JAR_PROPERTIES=application.properties
ADD ${JAR_FILE} app.jar
ADD ${JAR_PROPERTIES} application.properties
ENTRYPOINT ["java","-jar","/app.jar","--spring.config.additional-location=application.properties"]