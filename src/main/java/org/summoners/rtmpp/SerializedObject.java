package org.summoners.rtmpp;

import java.util.*;

public class SerializedObject extends HashMap<String, Object> {
    private static final long serialVersionUID = 1244827787088018807L;

    private String type;
    
    public String getType() {
    	return type;
    }

	public void setType(String type) {
		this.type = type;
	}

	public SerializedObject() {
		super();
	}

	public SerializedObject(String type) {
		super();
	}

	public SerializedObject(Map<String, Object> map) {
		super();
		this.putAll(map);
	}

	public static SerializedObject makeArrayCollection(Object[] obj) {
		SerializedObject object = new SerializedObject("flex.messaging.io.ArrayCollection");
		object.put("array", obj);
		return object;
	}
	
}
