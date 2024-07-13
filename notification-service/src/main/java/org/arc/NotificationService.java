package org.arc;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static final ConcurrentLinkedQueue<String> notificationQueue = new ConcurrentLinkedQueue<>();
    private static final long BATCH_TIMEOUT_MS = 20000; // 20 seconds in milliseconds
    private static final Timer timer = new Timer();

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka:9092");
        props.put("group.id", "notification-service");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("anomaly-topic"));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown initiated. Closing Kafka consumer.");
            running.set(false);
            consumer.wakeup(); // Interrupts consumer.poll() and causes it to throw a WakeupException
            timer.cancel(); // Cancel the timer on shutdown
        }));

        // Timer task to send batched notifications
        TimerTask batchTask = new TimerTask() {
            @Override
            public void run() {
                sendBatchedEmailNotification();
            }
        };
        timer.scheduleAtFixedRate(batchTask, BATCH_TIMEOUT_MS, BATCH_TIMEOUT_MS);

        try {
            while (running.get()) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(100);
                    for (ConsumerRecord<String, String> record : records) {
                        // Send notification logic
                        logger.info("Anomaly detected: " + record.value());
                        bufferNotification("Anomaly detected: " + record.value());
                    }
                } catch (WakeupException e) {
                    if (!running.get()) {
                        // This is expected during shutdown
                        break;
                    } else {
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in notification service: ", e);
        } finally {
            consumer.close();
            logger.info("Kafka consumer closed.");
        }
    }

    private static void bufferNotification(String message) {
        notificationQueue.add(message);
    }

    private static void sendBatchedEmailNotification() {
        StringBuilder batchMessage = new StringBuilder();
        String message;
        while ((message = notificationQueue.poll()) != null) {
            batchMessage.append(message).append("\n");
        }
        if (batchMessage.length() > 0) {
            sendEmailNotification(batchMessage.toString());
        }
    }

    private static void sendEmailNotification(String messageText) {
        String to = "samar4028@gmail.com"; // recipient's email
        String from = "sjrpc2024electioncommitee@gmail.com"; // sender's email
        String host = "smtp.gmail.com";

        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("sjrpc2024electioncommitee@gmail.com", "sjrpcec2024");
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Temperature Anomaly Alert");
            message.setText(messageText);

            Transport.send(message);
            logger.info("Sent batched message successfully...");
        } catch (MessagingException mex) {
            logger.error("Error sending email: ", mex);
        }
    }
}
