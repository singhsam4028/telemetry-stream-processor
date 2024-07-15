# telemetry-stream-processor


topic: telemetry-data 
docker exec -it telemetry-stream-processor-kafka-1 /usr/bin/kafka-console-consumer --bootstrap-server kafka:9092 --topic telemetry-data --from-beginning



topic: anomaly-topic
docker exec -it telemetry-stream-processor-kafka-1 /usr/bin/kafka-console-consumer --bootstrap-server kafka:9092 --topic anomaly-topic --from-beginning


notification-service:

docker logs -f telemetry-stream-processor-notification-service-1 | grep "Anomaly detected:"
