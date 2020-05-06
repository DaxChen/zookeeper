# CS739 Final Project

> Providing different consistency level containers for zookeeper

This branch `cs739/dev` is based on tag `release-3.4.12`

## Setup

```sh
# clone this repo
git clone https://github.com/YiShiunChang/zookeeper
```

## Make sure you are on the right branch
```
git checkout [branch name]
- Sol1: Dynamic forceSync: cs739/leaderConsistency
- Sol2: Short-circuit at PrepRequestProcessor: cs739/short-circuit-final-response
- Competitor: Deploy 2 Zookeeper: cs739/2-zookeeper-exp for strong server & cs739/2-zookeeper-exp-weak for weak server
```

## Build

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


## Develop Client program with Java API in Eclipse
1. build zookeeper using ant
2. In eclipse, create a Java project
3. Right click on your Java project -> build path -> add Libraries
4. User Libraries -> User Libraries... -> new -> name:zookeeper-cs739
5. Add external JARs... -> add build/zookeeper-3.4.12.jar & all jars in build/lib
6. Now you can import zookeeper in your java code


## Experiments Results
- Achieved 	**5.04x** in Weak compare to Strong request latency
- Successfully **separated latency entanglement** in different combination of Strong and Weak Clients
- Nearing **no effect** on Strong request **0.93x** and **1.98x** on Weak request latency comparing to 2 ZooKeeper
- Corrupted data isolation to **ensure availability** for **different consistency level**
- [Presentation slides](https://github.com/YiShiunChang/zookeeper/blob/cs739/dev/_Report/CS739%20-%20Final%20Presentation.pdfhttps://github.com/YiShiunChang/zookeeper/blob/cs739/dev/_Report/CS739%20-%20Final%20Presentation.pdf)
- [Report]()

## Benchmark in MongoDB
Performance entanglement on another system, apply multi-consistency and see if weak request is faster than strong
https://github.com/wu0607/MongoDB-benchmark
