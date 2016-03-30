package edu.isi.modeling.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonUtil {

	public static JsonObject getJsonObject(String type, String msg) {
		JsonObject o = new JsonObject();
		o.add(type, new JsonPrimitive(msg));
		return o;
	}
	
	public static String getJsonString(String type, String msg) {
		return getJsonObject(type, msg).toString();
	}
}
