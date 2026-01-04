package common;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    private RequestType type;
    private Object payload;

    public Request( Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public RequestType getType() { return type; }
    public Object getPayload() { return payload; }

    public void setType(RequestType type) { this.type = type; }
    public void setPayload(Object payload) { this.payload = payload; }
}
