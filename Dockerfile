# Stage 1: Build the Maven application
FROM maven:3.8.8-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the WAR file
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application inside Tomcat
FROM tomcat:10.1-jre17-alpine

# Clean up default Tomcat applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR to Tomcat's ROOT context so it serves at the root "/"
COPY --from=builder /app/target/vinheria-agnello.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
