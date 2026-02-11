<?php
$host = '127.0.0.1';
$port = rand(10000, 60000);

$socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
socket_bind($socket, $host, $port);
socket_listen($socket, 1);

exec("am broadcast -a com.chernegasergiy.battery.GET_STATUS --ei remote_port $port -n com.chernegasergiy.battery/.BatteryReceiver");

$client = socket_accept($socket);
$response = socket_read($client, 4096);
socket_close($client);
socket_close($socket);

echo $response . PHP_EOL;
