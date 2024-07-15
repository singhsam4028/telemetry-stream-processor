ATTACH TABLE _ UUID '92e6283a-b16c-44a5-b06d-06ec9365063e'
(
    `co` Float32,
    `humidity` Int32,
    `light` Bool,
    `lpg` Float32,
    `motion` Bool,
    `smoke` Float32,
    `temp` Float32
)
ENGINE = MergeTree
ORDER BY (co, humidity)
SETTINGS index_granularity = 8192
