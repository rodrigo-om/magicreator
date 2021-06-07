FROM adoptopenjdk:14-jre
ARG JAR_FILE=target/*.jar
ARG API_KEY
ENV POTTER_API_KEY=$API_KEY
ENV MONGODB_CONNECTION_STRING="mongodb://host.docker.internal:27017/admin?ssl=false"
EXPOSE 8080
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]