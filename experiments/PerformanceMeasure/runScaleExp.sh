#!/bin/bash

DURATION=180
WARM=120
CLIENT_NUM=1
PERCENT=100 # default strong ops
MODE=write  # default write
HOST=localhost:2182

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
echo "What Percentage Strong? [0-100%]"
read PERCENT


echo ""
echo "=============================="
echo "===       START EXP        ==="
echo "=============================="
echo ""

mkdir -p outputs

java -cp "./bin:../../build/zookeeper-3.4.12.jar:../../build/lib/*:./packages/*" \
            performancemeasure.ScalabiltyExp -d $DURATION -w $WARM -z $ZK_PATH -n $CLIENT_NUM -m $MODE -h $HOST -p $PERCENT

echo "Done writing stat to" ./outputs/exp-$MODE-$PERCENT%-$CLIENT_NUM-client

echo ""
echo "=============================="
echo "===        END EXP         ==="
echo "=============================="
echo ""