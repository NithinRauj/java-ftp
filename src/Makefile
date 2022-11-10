all: Server.class Client.class 

Server.class: Server.java
	javac -d . -classpath . Server.java

Client.class: Client.java
	javac -d . -classpath . Client.java

run-server:
	java Server

run-client:
	java Client

clean:
	rm -f *.class