#!/bin/bash

RED='\033[0;31m'
NC='\033[0m' # No color

# ask user what to run
PS3='What do you want to run?'
options=(
  "MeasureSetDataStrongWeak"
  "MeasureSetDataAsync"
  "Measure"
  "Quit"
)
select opt in "${options[@]}"
do
  case $opt in
    'MeasureSetDataStrongWeak'| \
    'MeasureSetDataAsync'| \
    'Measure')
      echo "running command:"
      echo "java -cp \"./bin:../../build/zookeeper-3.4.12.jar:../../build/lib/*\" performancemeasure.$opt $@"
      java -cp "./bin:../../build/zookeeper-3.4.12.jar:../../build/lib/*" performancemeasure.$opt $@
      break
      ;;
    'Quit')
      echo -e "\nBye~~~~~~"
      break
      ;;
    *) echo "Invalid option.. $REPLY try again";;
  esac
done