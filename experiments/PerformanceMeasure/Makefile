
default: performancemeasure

performancemeasure: src/performancemeasure/*.java
	javac -cp "./bin:../../build/zookeeper-3.4.12.jar:../../build/lib/*:./packages/*" src/performancemeasure/*.java -d bin

clean:
	rm -rf bin
