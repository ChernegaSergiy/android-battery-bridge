#!/data/data/com.termux/files/usr/bin/bash

# Kill any existing listener
killall nc 2>/dev/null

# Generate random port
PORT=$((RANDOM % 50000 + 10000))

# Create FIFO for response
RESPONSE=$(mktemp)

# Start nc listener in background
nc -l -p $PORT > "$RESPONSE" 2>/dev/null &

# Give nc time to start
sleep 0.3

# Send port to service via Unix socket
echo "PORT:$PORT" | nc -U -w 1 com.chernegasergiy.battery 2>/dev/null

# Wait for response
sleep 0.5

# Read and display response
if [ -s "$RESPONSE" ]; then
    cat "$RESPONSE"
else
    echo "No response"
fi

# Cleanup
rm -f "$RESPONSE"
killall nc 2>/dev/null
