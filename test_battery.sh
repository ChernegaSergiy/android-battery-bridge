#!/data/data/com.termux/files/usr/bin/bash

# Connect to the BatteryService TCP socket and read the response
RESPONSE=$(nc 127.0.0.1 8765)

if [ -n "$RESPONSE" ]; then
    echo "$RESPONSE"
else
    echo "No response or failed to connect"
fi
