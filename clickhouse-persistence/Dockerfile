FROM apache/flink:1.16.0

# Copy your Flink job JAR file
COPY target/clickhouse-persistence-1.0-SNAPSHOT.jar /flink-job.jar

# Set the entry point to run your Flink job
ENTRYPOINT ["flink", "run", "-c", "org.arc.ClickHousePersistence", "/flink-job.jar"]
