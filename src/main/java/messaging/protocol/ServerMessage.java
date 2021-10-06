package messaging.protocol;

import org.json.simple.JSONObject;

public class ServerMessage
{
    @SuppressWarnings("unchecked")
    public static JSONObject getNewIdentityReply( String approved) {
        JSONObject newIdentity = new JSONObject();
        newIdentity.put("type", "newidentity");
        newIdentity.put("approved", approved);
        return newIdentity;
    }
}
