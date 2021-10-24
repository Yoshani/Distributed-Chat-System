package messaging;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ServerMessage
{
    private static ServerMessage instance = null;

    private ServerMessage() {
    }

    public static synchronized ServerMessage getInstance() {
        if (instance == null) instance = new ServerMessage();
        return instance;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getHeartbeat( String sender) {
        // {"option": "heartbeat", "sender": "s1"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", "heartbeat");
        jsonObject.put("sender", sender);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getElection(String source) {
        // {"option": "election", "source": "s1"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", "election");
        jsonObject.put("source", source);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCoordinator(String leader) {
        // {"option": "coordinator", "leader": "s3"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", "coordinator");
        jsonObject.put("leader", leader);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getOk(String sender) {
        // {"option": "ok", "sender": "s1"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", "ok");
        jsonObject.put("sender", sender);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getClientIdApprovalRequest(String clientID, String sender, String threadID) {
        // {"type" : "clientidapprovalrequest", "clientid" : "Adel", "sender" : "s2", "threadid" : "10"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "clientidapprovalrequest");
        jsonObject.put("clientid", clientID);
        jsonObject.put("sender", sender);
        jsonObject.put("threadid", threadID);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getClientIdApprovalReply(String approved, String threadID) {
        // {"type" : "clientidapprovalreply", "approved" : "1", "threadid" : "10"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "clientidapprovalreply");
        jsonObject.put("approved", approved);
        jsonObject.put("threadid", threadID);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getRoomCreateApprovalRequest(String clientID, String roomID, String sender, String threadID) {
        // {"type" : "roomcreateapprovalrequest", "clientid" : "Adel", "roomid" : "jokes", "sender" : "s2", "threadid" : "10"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "roomcreateapprovalrequest");
        jsonObject.put("clientid", clientID);
        jsonObject.put("roomid", roomID);
        jsonObject.put("sender", sender);
        jsonObject.put("threadid", threadID);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getRoomCreateApprovalReply(String approved, String threadID) {
        // {"type" : "roomcreateapprovalreply", "approved" : "1", "threadid" : "10"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "roomcreateapprovalreply");
        jsonObject.put("approved", approved);
        jsonObject.put("threadid", threadID);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getJoinRoomRequest(String clientID, String roomID, String formerRoomID, String sender, String threadID, String isLocalRoomChange) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "joinroomapprovalrequest");
        jsonObject.put("sender", sender);
        jsonObject.put("roomid", roomID);
        jsonObject.put("former", formerRoomID);
        jsonObject.put("clientid", clientID);
        jsonObject.put("threadid", threadID);
        jsonObject.put("isLocalRoomChange", isLocalRoomChange);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getJoinRoomApprovalReply(String approved, String threadID, String host, String port) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "joinroomapprovalreply");
        jsonObject.put("approved", approved);
        jsonObject.put("host", host);
        jsonObject.put("port", port);
        jsonObject.put("threadid", threadID);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getMoveJoinRequest(String clientID, String roomID, String formerRoomID, String sender, String threadID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "movejoinack");
        jsonObject.put("sender", sender);
        jsonObject.put("roomid", roomID);
        jsonObject.put("former", formerRoomID);
        jsonObject.put("clientid", clientID);
        jsonObject.put("threadid", threadID);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getQuit(String clientID) {
        // {"type" : "quit", "clientid" : "Adel"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "quit");
        jsonObject.put("clientid", clientID);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getListRequest(String clientID, String threadID, String sender) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "listrequest");
        jsonObject.put("sender", sender);
        jsonObject.put("clientid", clientID);
        jsonObject.put("threadid", threadID);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getListResponse(ArrayList<String> roomIDList, String threadID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "listresponse");
        jsonObject.put("threadid", threadID);
        jsonObject.put("rooms", roomIDList);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getDeleteRoomRequest(String ownerID, String roomID, String mainHallID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "deleterequest");
        jsonObject.put("owner", ownerID);
        jsonObject.put("roomid", roomID);
        jsonObject.put("mainhall", mainHallID);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject gossipMessage(Integer serverId, HashMap<Integer, Integer> heartbeatCountList) {
        // {"type":"gossip","serverid":"1","heartbeatcountlist":{"1":0,"2":1,"3":1,"4":2}}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "gossip");
        jsonObject.put("serverId", serverId);
        jsonObject.put("heartbeatCountList", heartbeatCountList);
        return jsonObject;
    }
}
