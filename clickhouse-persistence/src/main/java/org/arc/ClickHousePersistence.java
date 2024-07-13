package org.arc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.clickhouse.catalog.ClickHouseCatalog;
import org.apache.flink.connector.clickhouse.config.ClickHouseConfig;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.types.Row;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.IOException;

public class ClickHousePersistence {

    public static void main(String[] args) throws Exception {
        // Set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, EnvironmentSettings.newInstance().inStreamingMode().build());

        // Configure Kafka properties
        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", "kafka:9092");

        // Define Kafka source
        KafkaSource<Row> source = KafkaSource.<Row>builder()
                .setBootstrapServers(kafkaProps.getProperty("bootstrap.servers"))
                .setTopics("anomaly-topic")
                .setGroupId("clickhouse-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(new KafkaRecordDeserializationSchema<Row>() {
                    private final ObjectMapper objectMapper = new ObjectMapper();

                    @Override
                    public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<Row> out) throws IOException {
                        String json = new String(record.value());
                        JsonNode jsonNode = objectMapper.readTree(json);

                        Row row = Row.of(
                                (float) jsonNode.get("co").asDouble(),  // Use float for co
                                jsonNode.get("humidity").asInt(),
                                jsonNode.get("light").asBoolean() ? (byte) 1 : (byte) 0, // Convert to byte
                                (float) jsonNode.get("lpg").asDouble(), // Use float for lpg
                                jsonNode.get("motion").asBoolean() ? (byte) 1 : (byte) 0, // Convert to byte
                                (float) jsonNode.get("smoke").asDouble(), // Use float for smoke
                                (float) jsonNode.get("temp").asDouble() // Use float for temp
                        );

                        out.collect(row);
                    }

                    @Override
                    public TypeInformation<Row> getProducedType() {
                        return Types.ROW_NAMED(
                                new String[]{"co", "humidity", "light", "lpg", "motion", "smoke", "temp"},
                                Types.FLOAT,   // Use Types.FLOAT for co
                                Types.INT,
                                Types.BYTE,    // Use Types.BYTE for light
                                Types.FLOAT,   // Use Types.FLOAT for lpg
                                Types.BYTE,    // Use Types.BYTE for motion
                                Types.FLOAT,   // Use Types.FLOAT for smoke
                                Types.FLOAT    // Use Types.FLOAT for temp
                        );
                    }
                })
                .build();

        // Create a data stream from the Kafka source
        DataStream<Row> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        // Configure ClickHouse properties
        Map<String, String> props = new HashMap<>();
        props.put(ClickHouseConfig.DATABASE_NAME, "telemetry");
        props.put(ClickHouseConfig.URL, "clickhouse://clickhouse:8123");
        props.put(ClickHouseConfig.USERNAME, "samar");
        props.put(ClickHouseConfig.PASSWORD, "samar_4028");
        props.put(ClickHouseConfig.SINK_FLUSH_INTERVAL, "30s");

        // Register ClickHouse catalog
        Catalog clickHouseCatalog = new ClickHouseCatalog("clickhouse", props);
        tableEnv.registerCatalog("clickhouse", clickHouseCatalog);
        tableEnv.useCatalog("clickhouse");
        tableEnv.useDatabase("telemetry");

        // Register the data stream as a table with the schema in the ClickHouse catalog
        tableEnv.createTemporaryView("inputTable", stream, Schema.newBuilder()
                .column("co", DataTypes.FLOAT())    // Use DataTypes.FLOAT for co
                .column("humidity", DataTypes.INT())
                .column("light", DataTypes.TINYINT()) // Use DataTypes.TINYINT for light
                .column("lpg", DataTypes.FLOAT())   // Use DataTypes.FLOAT for lpg
                .column("motion", DataTypes.TINYINT()) // Use DataTypes.TINYINT for motion
                .column("smoke", DataTypes.FLOAT()) // Use DataTypes.FLOAT for smoke
                .column("temp", DataTypes.FLOAT())  // Use DataTypes.FLOAT for temp
                .build());

        // Define and execute the SQL query to insert data into ClickHouse
        String insertIntoTable = "INSERT INTO anomalies SELECT * FROM inputTable";
        TableResult result = tableEnv.executeSql(insertIntoTable);

        result.getJobClient().ifPresent(jobClient -> {
            try {
                jobClient.getJobExecutionResult().get();
            } catch (Exception e) {
                e.printStackTrace(); // Handle exception as needed
            }
        });

        // Execute the Flink job
        env.execute("ClickHouse Persistence");
    }
}
