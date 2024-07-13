ATTACH TABLE _ UUID '94ad92d0-4410-48ae-ab94-928b1edbb2b2'
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
