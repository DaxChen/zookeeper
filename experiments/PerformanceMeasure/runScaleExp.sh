#!/bin/bash

DURATION=20  # default 3 mins
WARM=10      # default 2 mins

STRONG_NUM=1
WEAK_NUM=1
HOST=localhost:2182 
PERCENT_WRITE=100

echo "How many strong client want to create?"
read STRONG_NUM
if [ $STRONG_NUM -lt 0 ]; then
  echo "ERROR input! number of clients should be >= 0"
  exit 1
fi
echo "How many weak client want to create?"
read WEAK_NUM
if [ $WEAK_NUM -lt 0 ]; then
  echo "ERROR input! number of clients should be >= 0"
  exit 1
fi
echo "Which host? [10.10.1.2:2181]"
read HOST
echo "What Percentage Write ops? [0-100]"
read PERCENT_WRITE

echo ""
echo "=============================="
echo "===       START EXP        ==="
echo "=============================="
echo ""

mkdir -p outputs

java -cp "./bin:../../build/zookeeper-3.4.12.jar:../../build/lib/*:./packages/*" \
            performancemeasure.ScalabiltyExp -d $DURATION -w $WARM -ns $STRONG_NUM -nw $WEAK_NUM -h $HOST -pw $PERCENT_WRITE

echo "Done writing stat to" ./outputs/exp-$PERCENT_WRITE%write-${STRONG_NUM}s${WEAK_NUM}w

echo ""
echo "=============================="
echo "===        END EXP         ==="
echo "=============================="
echo ""