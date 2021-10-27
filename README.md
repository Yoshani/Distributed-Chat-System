# Distributed Chat System - CS4262 Distributed Systems

This group project is about creating a distributed chat application. The system consists of two main distributed components: chat servers and chat clients, which can run on different hosts. <br/> 

Chat clients are programs that can connect to at most one chat server; which can be any of the available servers. Chat clients can be used to send requests for creating, deleting, joining and quitting a chat room. They can also be used to send requests to see the list of available chat rooms in the system and the list of client identities currently connected to a given chat room. Finally, they can be used to send chat messages to other chat clients connected to the same chat room. <br/> 

Chat servers are programs accepting multiple incoming TCP connections from chat clients. There are multiple servers working together to serve chat clients. The number of servers is fixed and does not change once the system is active. Each server is responsible for managing a subset of the system's chat rooms. In particular, a server manages only those chat rooms that were created locally after receiving a request from a client. In order to join a particular chat room, clients must be connected to the server managing that chat room. As a result, clients are redirected between servers when a client wants to join a chat room managed by a different server. Chat servers are also responsible for broadcasting messages received from clients to all other clients connected to the same chat room.   <br/>

##Instructions to Build the executable Jar

Development Environment - `IntelliJ IDEA`

install java (version `1.8`)
\
install Maven (version `3.6.3`)

run the following commands to install dependencies and build 
 
 `mvn clean install `
 \
 `mvn clean compile assembly:single`
 
The output jar will be created inside the `'target'` folder named `Distributed-Chat-System-1.0-SNAPSHOT-jar-with-dependencies.jar`

##Instructions to Run the Jar

run the following command in a terminal 

`java -jar Distributed-Chat-System-1.0-SNAPSHOT-jar-with-dependencies.jar s1 "C:code\src\main\java\config\server_conf.txt"`

note `s1` should be changed according to the server instance
\
note the path to the `server_conf.txt` should be given according to the configuration file location