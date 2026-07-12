<?php
$client = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
if (socket_connect($client, "127.0.0.1", 8765)) {
    $response = socket_read($client, 4096);
    echo $response . PHP_EOL;
} else {
    echo "Failed to connect to BatteryService" . PHP_EOL;
}
socket_close($client);
