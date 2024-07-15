## Use a lightweight JRE base image
#FROM openjdk:17-jdk-slim AS build
#
## Copy the JAR file into the image
#COPY target/stream-processor-1.0-SNAPSHOT.jar /app/stream-processor.jar
#
## Set the entry point to run your Flink job
#ENTRYPOINT ["java", "-jar", "/app/stream-processor.jar"]


# Use the Flink base image
FROM apache/flink:1.14.0

# Copy your application JAR into the image
COPY target/stream-processor-1.0-SNAPSHOT.jar /opt/flink/usrlib/

# Set the entry point to run the Flink job manager and your JAR
ENTRYPOINT ["standalone-job", "--job-classname", "org.arc.TelemetryStreamProcessor", "--jars", "/opt/flink/usrlib/stream-processor-1.0-SNAPSHOT.jar"]
