ATTACH TABLE _ UUID 'b84aaa7b-b14a-4c98-ab5c-11527e34be14'
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
ORDER BY tuple()
SETTINGS index_granularity = 8192
