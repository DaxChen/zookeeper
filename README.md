# CS739 Final Project

> Providing different consistency level containers for zookeeper

This branch `cs739/dev` is based on tag `release-3.4.12`

## setup

```sh
# clone this repo
git clone https://github.com/YiShiunChang/zookeeper
```

## make sure you are on the right branch
```
git checkout [branch name]
- Sol1: Dynamic forceSync: cs739/leaderConsistency
- Sol2: Short-circuit at PrepRequestProcessor: cs739/short-circuit-final-response
- Competitor: Deploy 2 Zookeeper: cs739/2-zookeeper-exp for strong server & cs739/2-zookeeper-exp-weak for weak server
```

## build

```sh
# build zookeeper
ant
```

## Running Replicated ZooKeeper

```
cd bin
# [use three different shell, e.g. using tmux]
./zkServer.sh start-foreground ../conf/zoo-1.cfg
./zkServer.sh start-foreground ../conf/zoo-2.cfg
./zkServer.sh start-foreground ../conf/zoo-3.cfg
```

## Running Experiments


## Develop Client program with Java API in Eclipse
1. build zookeeper using ant
2. In eclipse, create a Java project
3. Right click on your Java project -> build path -> add Libraries
4. User Libraries -> User Libraries... -> new -> name:zookeeper-cs739
5. Add external JARs... -> add build/zookeeper-3.4.12.jar & all jars in build/lib
6. Now you can import zookeeper in your java code

## Benchmark in MongoDB
Performance entanglement on another system, apply multi-consistency and see if weak request is faster than strong
https://github.com/wu0607/MongoDB-benchmark
