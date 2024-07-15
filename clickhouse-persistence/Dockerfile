#FROM apache/flink:1.16.0
#
## Copy your Flink job JAR file
#COPY target/clickhouse-persistence-1.0-SNAPSHOT.jar /flink-job.jar
#
## Set the entry point to run your Flink job
#ENTRYPOINT ["flink", "run", "-c", "org.arc.ClickHousePersistence", "/flink-job.jar"]



# Use the Flink image as the base
FROM apache/flink:1.16.0

# Copy your ClickHouse persistence JAR file
COPY target/clickhouse-persistence-1.0-SNAPSHOT.jar /opt/flink/usrlib/

# Set the entry point to run the Flink job
ENTRYPOINT ["standalone-job", "--job-classname", "org.arc.ClickHousePersistence", "--jars", "/opt/flink/usrlib/clickhouse-persistence-1.0-SNAPSHOT.jar"]
