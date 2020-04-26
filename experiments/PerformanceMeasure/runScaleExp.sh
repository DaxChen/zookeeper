#!/bin/bash

DURATION=180  # default 3 mins
WARM=120      # default 2 mins

CLIENT_NUM=1
HOST=localhost:2182
MODE=write   
PERCENT=100
PERCENT_WRITE=100

echo "How many client want to create?"
read CLIENT_NUM
if [ $CLIENT_NUM -lt 1 ]; then
  echo "ERROR input! number of clients should be positive"
  exit 1
fi
echo "Which mode? [write/read/readwrite]"
read MODE
echo "Which host? [10.10.1.2:2181]"
read HOST
echo "What Percentage Strong path ops? [0-100]"
read PERCENT
echo "What Percentage Write ops? [0-100]"
read PERCENT_WRITE

echo ""
echo "=============================="
echo "===       START EXP        ==="
echo "=============================="
echo ""

mkdir -p outputs

java -cp "./bin:../../build/zookeeper-3.4.12.jar:../../build/lib/*:./packages/*" \
            performancemeasure.ScalabiltyExp -d $DURATION -w $WARM -n $CLIENT_NUM -m $MODE -h $HOST -p $PERCENT -pw $PERCENT_WRITE

echo "Done writing stat to" ./outputs/exp-$MODE-$PERCENT_WRITE%write-$PERCENT%strong-$CLIENT_NUM-client

echo ""
echo "=============================="
echo "===        END EXP         ==="
echo "=============================="
echo ""