FROM apache/flink:1.14.0

# Copy your Flink job JAR file
COPY target/stream-processor-1.0-SNAPSHOT.jar /flink-job.jar

# Set the entry point to run your Flink job
ENTRYPOINT ["flink", "run", "-c", "org.arc.TelemetryStreamProcessor", "/flink-job.jar"]
