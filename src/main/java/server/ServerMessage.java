package server;

import org.json.simple.JSONObject;

public class ServerMessage {

    @SuppressWarnings("unchecked")
    public static JSONObject getApprovalNewID(String approve) {
        JSONObject join = new JSONObject();
        join.put("type", "newidentity");
        join.put("approved", approve);
        return join;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getRoomChange(String id, String MainHall) {
        JSONObject join = new JSONObject();
        join.put("type", "roomchange");
        join.put("identity", id);
        join.put("former","");
        join.put("roomid",MainHall);
        return join;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateRoom(String id, String approve) {
        JSONObject join = new JSONObject();
        join.put("type", "createroom");
        join.put("roomid", id);
        join.put("approved",approve);
        return join;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateRoomChange(String id, String former, String roomID) {
        JSONObject join = new JSONObject();
        join.put("type", "roomchange");
        join.put("identity", id);
        join.put("former",former);
        join.put("roomid",roomID);
        return join;
    }
}
