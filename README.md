# Distributed Chat System - CS4262 Distributed Systems

## Introduction 

This group project is about creating a distributed chat application. The system consists of two main distributed components: chat servers and chat clients, which can run on different hosts. <br/> 

**This repository contains the server-side codebase** 

Chat clients are programs that can connect to at most one chat server; which can be any of the available servers. Chat clients can be used to send requests for creating, deleting, joining and quitting a chat room. They can also be used to send requests to see the list of available chat rooms in the system and the list of client identities currently connected to a given chat room. Finally, they can be used to send chat messages to other chat clients connected to the same chat room. <br/> 

Chat servers are programs accepting multiple incoming TCP connections from chat clients. There are multiple servers working together to serve chat clients. The number of servers is fixed and does not change once the system is active. Each server is responsible for managing a subset of the system's chat rooms. In particular, a server manages only those chat rooms that were created locally after receiving a request from a client. In order to join a particular chat room, clients must be connected to the server managing that chat room. As a result, clients are redirected between servers when a client wants to join a chat room managed by a different server. Chat servers are also responsible for broadcasting messages received from clients to all other clients connected to the same chat room.   <br/>

During the initialization of a server, a leader election is held and a leader is selected
for the system. The leader is responsible for maintaining global consistency in the
system whilst non-leader servers seek the approval of the leader for certain actions,
thus providing access transparency. When a new leader is elected, all requests that
require the leader’s approval are held until the leader collects all existing client and
chat room lists in the system and the system achieves consistency. In case of a
non-leader server failure, the leader should be notified and that server’s state should
be deleted from the system. For this, a heartbeat is implemented using gossiping and
consensus where the server failure is detected and handled accordingly. With this
implementation, fault tolerance is ensured. The implementation also guarantees
failure transparency to the client. <br/>


## Instructions to Build the executable Jar

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
