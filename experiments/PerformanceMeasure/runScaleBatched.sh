#!/bin/bash

DURATION=20  # default 3 mins
WARM=10      # default 2 mins

# echo "How many strong client want to create?"
STRONG_NUM=1
# echo "How many weak client want to create?"
WEAK_NUM=1
# echo "Which host? [10.10.1.2:2181]"
HOST='10.10.1.2:2181'
# echo "What Percentage Write ops? [0-100]"
PERCENT_WRITE=100

for ((i = 1; i <= 128; i *= 2)); do
  STRONG_NUM=$i
  WEAK_NUM=0
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
done

