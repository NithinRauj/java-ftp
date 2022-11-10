all: Server.class Client.class 

Server.class: ./src/Server.java
	javac -d ./bin -classpath ./src ./src/Server.java

Client.class: ./src/Client.java
	javac -d ./bin -classpath ./src ./src/Client.java

run-server:
	java -cp ./bin Server

run-client:
	java -cp ./bin Client

clean:
	rm -f ./bin/*.class