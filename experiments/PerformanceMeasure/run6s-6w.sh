#!/bin/bash

DURATION=180  # default 3 mins
WARM=120      # default 2 mins

# echo "How many strong client want to create?"
STRONG_NUM=1
# echo "How many weak client want to create?"
WEAK_NUM=1
# echo "Which host? [10.10.1.2:2181]"
HOST='10.10.1.2:2181'

PERCENT_WRITE=100
echo "What Percentage Write ops? [0-100]"
read PERCENT_WRITE

for s in $(seq 0 6); do
  STRONG_NUM=$s
  WEAK_NUM=$((6 - $s))
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

