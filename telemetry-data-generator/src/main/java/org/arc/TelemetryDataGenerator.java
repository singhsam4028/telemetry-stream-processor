package org.arc;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;

public class TelemetryDataGenerator {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka:9092");
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        Producer<String, String> producer = new KafkaProducer<>(props);
        Random random = new Random();

        while (true) {
            String data = "{\"co\": " + random.nextFloat() +
                    ", \"humidity\": " + random.nextInt(100) +
                    ", \"light\": " + random.nextBoolean() +
                    ", \"lpg\": " + random.nextFloat() +
                    ", \"motion\": " + random.nextBoolean() +
                    ", \"smoke\": " + random.nextFloat() +
                    ", \"temp\": " + (random.nextInt(80) - 20) + "}";
            producer.send(new ProducerRecord<>("telemetry-data", data));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
