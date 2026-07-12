<?php
$host = "127.0.0.1";
$port = 8765;

// You can pass the IP address and port as arguments: php test_battery.php 192.168.1.50
if ($argc > 1) {
    $host = $argv[1];
}
if ($argc > 2) {
    $port = (int)$argv[2];
}

$fp = @fsockopen($host, $port, $errno, $errstr, 3);
if (!$fp) {
    echo "Failed to connect to BatteryService: $errstr ($errno)\n";
} else {
    $response = fread($fp, 4096);
    echo trim($response) . PHP_EOL;
    fclose($fp);
}
