#!/bin/bash

# Step 1: Create the user 'samar'
docker exec -it telemetry-stream-processor-clickhouse-1 clickhouse-client --host=clickhouse --port=9000 --query "CREATE USER IF NOT EXISTS samar IDENTIFIED WITH plaintext_password BY 'samar_4028';"

# Step 2: Create the database 'telemetry'
docker exec -it telemetry-stream-processor-clickhouse-1 clickhouse-client --host=clickhouse --port=9000 --query "CREATE DATABASE IF NOT EXISTS telemetry;"

# Step 3: Create the 'anomalies' table
docker exec -it telemetry-stream-processor-clickhouse-1 clickhouse-client --host=clickhouse --port=9000 --query "CREATE TABLE IF NOT EXISTS telemetry.anomalies (co Float32, humidity Int32, light Bool, lpg Float32, motion Bool, smoke Float32, temp Float32) ENGINE = MergeTree() ORDER BY (co, humidity);"

# Step 4: Grant permissions to the user 'samar' for the 'telemetry' database
docker exec -it telemetry-stream-processor-clickhouse-1 clickhouse-client --host=clickhouse --port=9000 --query "GRANT ALL ON telemetry.* TO samar;"
