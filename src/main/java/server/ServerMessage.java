package server;

import org.json.simple.JSONObject;

import java.util.*;

public class ServerMessage {

    @SuppressWarnings("unchecked")
    public static JSONObject getApprovalNewID(String approve) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "newidentity");
        jsonObject.put("approved", approve);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getRoomChange(String id, String MainHall) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "roomchange");
        jsonObject.put("identity", id);
        jsonObject.put("former", "");
        jsonObject.put("roomid", MainHall);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateRoom(String id, String approve) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "createroom");
        jsonObject.put("roomid", id);
        jsonObject.put("approved", approve);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateRoomChange(String id, String former, String roomID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "roomchange");
        jsonObject.put("identity", id);
        jsonObject.put("former", former);
        jsonObject.put("roomid", roomID);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getWho(String roomID, List<String> participants, String id) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "roomcontents");
        jsonObject.put("roomid", roomID);
        jsonObject.put("identities", participants);
        jsonObject.put("owner", id);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getList(List<String> rooms) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "roomlist");
        jsonObject.put("rooms", rooms);
        return jsonObject;
    }
}
