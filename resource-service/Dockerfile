# use this command to build the image on Apple Silicon machine
# docker build --platform=linux/amd64 -t resource-service .

# build module
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build_module

ENV MODULE_ROOT_DIR=/resource-service
WORKDIR $MODULE_ROOT_DIR

COPY pom.xml .
COPY src/main/java ./src/main/java
COPY src/main/resources ./src/main/resources

# download dependencies
RUN mvn dependency:go-offline
# build .jar file in /target dir. There are no unit tests, but still
RUN mvn clean package -Dmaven.test.skip=true

# run module
FROM eclipse-temurin:17-jre-alpine-3.21 AS run_module

ENV MODULE_ROOT_DIR=/resource-service
ENV APP_JAR_FILENAME=app.jar
# default (root) workdir
WORKDIR /

COPY --from=build_module $MODULE_ROOT_DIR/target/*.jar $APP_JAR_FILENAME

# tells Docker the container will listen on port 8091 internally
EXPOSE 8091

# run the app/container
CMD java -jar "$APP_JAR_FILENAME"

# use later --spring.config.location=classpath:/application.yml
