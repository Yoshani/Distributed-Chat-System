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
}
