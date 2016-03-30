package edu.isi.modeling.suggestion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.stream.JsonWriter;

import edu.isi.modeling.rep.InternalNode;
import edu.isi.modeling.rep.LabeledLink;

public class SuggestLinkOutput {
	
	private List<LabeledLink> links;

	public SuggestLinkOutput() {
		this.links = new ArrayList<LabeledLink>();
	}
	public List<LabeledLink> getLinks() {
		return links;
	}

	public void setLinks(List<LabeledLink> links) {
		this.links = links;
	}
	
	public String writeJson() throws IOException {

		OutputStream out = new ByteArrayOutputStream(); 
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		writer.setIndent("    ");
		try {
			writer.beginObject();
			
			writer.name("links");
			writer.beginArray();
			if (links != null)
				for (LabeledLink l : links)
					writeLink(writer, l, false);
			writer.endArray();
			
			writer.endObject();
			writer.close();

			return out.toString();
		} catch (Exception e) {
	    	writer.close();
			throw e;
	    } 
	}
	
	private static void writeLink(JsonWriter writer, LabeledLink link, boolean writeLinkAnnotations) throws IOException {
		
		if (link == null)
			return;
		
		String nullStr = null;

		writer.beginObject();
		writer.name("uri").value(link.getUri());
		if (link.getSource() == null) {
			writer.name("source").value(nullStr);
		} else {
			writer.name("source").value(link.getSource().getUri());
		}
		if (link.getTarget() != null && link.getTarget() instanceof InternalNode) {
			writer.name("target").value(link.getTarget().getUri());			
		} else {
			writer.name("target").value(nullStr);			
		}
		writer.name("type").value(link.getType().toString());
		if (writeLinkAnnotations) {
			writer.name("modelIds");
			if (link.getModelIds() == null) writer.value(nullStr);
			else writeModelIds(writer, link.getModelIds());
		}
		writer.endObject();
	}
	
	private static void writeModelIds(JsonWriter writer, Set<String> modelIds) throws IOException {
		
		if (modelIds == null)
			return;
		
		writer.beginArray();
		for (String s : modelIds)
			writer.value(s);
		writer.endArray();
	}
}
