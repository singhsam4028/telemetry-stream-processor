package org.arc;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.configuration.Configuration;

import java.util.Properties;

public class TelemetryStreamProcessor {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", "kafka:9092");
        kafkaProps.setProperty("request.timeout.ms", "60000"); // 60 seconds
        kafkaProps.setProperty("session.timeout.ms", "60000"); // 60 seconds
        kafkaProps.setProperty("retry.backoff.ms", "5000"); // 5000 milliseconds



        // Kafka Source
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(kafkaProps.getProperty("bootstrap.servers"))
                .setTopics("telemetry-data")
                .setGroupId("flink-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");



        // Deserialize JSON records and filter anomalies
        DataStream<String> anomalies = stream
                .process(new ProcessFunction<String, String>() {
                    @Override
                    public void processElement(String value, Context ctx, Collector<String> out) throws Exception {
                        // Example filter condition: 'temp' > 35
                        try {
                            int startIndex = value.indexOf("\"temp\":") + 7;
                            int endIndex = value.indexOf("}", startIndex);
                            double temp = Double.parseDouble(value.substring(startIndex, endIndex));
                            if (temp > 50) {
                                out.collect(value);
                            }
                        } catch (NumberFormatException | IndexOutOfBoundsException e) {
                            System.err.println("Error processing telemetry record: " + value);
                            e.printStackTrace();

                        }
                    }
                });

        // Kafka Sink for anomalies



        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers(kafkaProps.getProperty("bootstrap.servers"))
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("anomaly-topic")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                ).setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE).build();


        anomalies.sinkTo(sink);

        env.execute("Telemetry Stream Processor");
    }
}
