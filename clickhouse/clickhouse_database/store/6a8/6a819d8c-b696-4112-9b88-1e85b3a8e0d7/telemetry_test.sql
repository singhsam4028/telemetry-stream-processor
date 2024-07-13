ATTACH TABLE _ UUID '1bb3d8ac-c1bb-4ee6-b22c-c90dbb966dfe'
(
    `ts` DateTime,
    `device` String,
    `co` Float32,
    `humidity` Float32,
    `light` Bool,
    `lpg` Float32,
    `motion` Bool,
    `smoke` Float32,
    `temp` Float32
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(ts)
ORDER BY (ts, device)
SETTINGS index_granularity = 8192
