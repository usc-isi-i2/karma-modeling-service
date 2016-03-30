package edu.isi.modeling.suggestion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.io.IOUtils;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class SuggestLinkInput {

	private String sourceUri;
	private String targetUri;

	public SuggestLinkInput(String sourceUri) {
		this.sourceUri = sourceUri;
		this.targetUri = null;
	}

	public SuggestLinkInput(String sourceUri, String targetUri) {
		this.sourceUri = sourceUri;
		this.targetUri = targetUri;
	}
	
	public String getSourceUri() {
		return sourceUri;
	}
	public String getTargetUri() {
		return targetUri;
	}
	
	public String writeJson() throws IOException {
		OutputStream out = new ByteArrayOutputStream(); 
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		writer.setIndent("    ");
		try {
			writer.beginObject();
			writer.name("sourceuri").value(this.getSourceUri());
			if (this.getTargetUri() != null && !this.getTargetUri().isEmpty())
				writer.name("targeturi").value(this.getTargetUri());
			writer.endObject();
			writer.close();
			return out.toString();
		} catch (Exception e) {
	    	writer.close();
			throw e;
	    } 
	}
	
	public static SuggestLinkInput readJson(String json) throws Exception {
		
		String sourceUri = null, targetUri = null;
		JsonReader reader = null;
		try {
			InputStream in = IOUtils.toInputStream(json, "UTF-8");			
			reader = new JsonReader(new InputStreamReader(in));
	
			reader.beginObject();
		    while (reader.hasNext()) {
		    	String key = reader.nextName();
				if (key.equalsIgnoreCase("sourceuri") && reader.peek() != JsonToken.NULL) {
					sourceUri = reader.nextString();
				} else if (key.equalsIgnoreCase("targeturi") && reader.peek() != JsonToken.NULL) {
					targetUri = reader.nextString();
				} 
			}
		    reader.endObject();
		} catch (Exception e) {
			throw e;
		} finally {
		    if (reader != null)
		    	reader.close();
		}

		if (sourceUri == null || sourceUri.isEmpty())
			throw new Exception("error in parsing the json body");
		
		SuggestLinkInput input = new SuggestLinkInput(sourceUri, targetUri);
		return input;
	}
	
}
