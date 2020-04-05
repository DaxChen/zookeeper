# CS739 Final Project

> Providing different consistency level containers for zookeeper

This branch `cs739/dev` is based on tag `release-3.4.12`

## setup

```sh
# clone this repo
git clone https://github.com/YiShiunChang/zookeeper

cd zookeeper

# make sure you are on the right branch
git checkout cs739/dev
```

## build

```sh
# build zookeeper
ant
```

## Running Replicated ZooKeeper

```
cd bin
[use three different shell, e.g. using tmux]
./zkServer.sh start-foreground ../conf/zoo-1.cfg
./zkServer.sh start-foreground ../conf/zoo-2.cfg
./zkServer.sh start-foreground ../conf/zoo-3.cfg
```

## Running Experiments

