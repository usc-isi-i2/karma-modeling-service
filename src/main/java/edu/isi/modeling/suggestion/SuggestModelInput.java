package edu.isi.modeling.suggestion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import edu.isi.modeling.rep.ColumnNode;
import edu.isi.modeling.rep.InternalNode;
import edu.isi.modeling.rep.Label;
import edu.isi.modeling.rep.Node;
import edu.isi.modeling.rep.NodeType;
import edu.isi.modeling.rep.SemanticType;
import edu.isi.modeling.rep.SemanticType.Origin;

public class SuggestModelInput {

	List<Node> nodes;
	
	public SuggestModelInput() {
		this.nodes = new ArrayList<Node>();
	}
	
	public SuggestModelInput(List<Node> nodes) {
		this.nodes = nodes;
	}
	
	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public static SuggestModelInput readJson(String json) throws Exception {

		JsonReader reader = null;
		Node n;
		List<Node> nodes = new LinkedList<Node>();
		try {
			InputStream in = IOUtils.toInputStream(json, "UTF-8");			
			reader = new JsonReader(new InputStreamReader(in));
	
			reader.beginObject();

		    while (reader.hasNext()) {
		    	String key = reader.nextName();
				if (key.equals("nodes") && reader.peek() != JsonToken.NULL) {
					reader.beginArray();
				    while (reader.hasNext()) {
				    	n = readNode(reader);
				    	if (n != null)
				    		nodes.add(n);
				    }
				    reader.endArray();
				} else {
					reader.skipValue();
				}
		    }
			reader.endObject();
		} catch (Exception e) {
			throw e;
		} finally {
		    if (reader != null)
		    	reader.close();
		}
		
		if (nodes == null || nodes.isEmpty()) {
			throw new Exception("invalid json body");
		}
		
		SuggestModelInput input = new SuggestModelInput(nodes);
		return input;
	}
		
		private static Node readNode(JsonReader reader) throws IOException {
			
			String id = null;
			String uri = null;
			NodeType type = null;
			String hNodeId = null;
			String columnName = null;
//			ColumnSemanticTypeStatus semanticTypeStatus = null;
			List<SemanticType> learnedSemanticTypes = null;
			List<SemanticType> userSemanticTypes = null;
			
			reader.beginObject();
		    while (reader.hasNext()) {
		    	String key = reader.nextName();
				if (key.equals("id") && reader.peek() != JsonToken.NULL) {
					id = reader.nextString();
				} else if (key.equals("uri") && reader.peek() != JsonToken.NULL) {
					uri = reader.nextString();
				} else if (key.equals("type") && reader.peek() != JsonToken.NULL) {
					type = NodeType.valueOf(reader.nextString());
//				} else if (key.equals("semanticTypeStatus") && reader.peek() != JsonToken.NULL) {
//					semanticTypeStatus = ColumnSemanticTypeStatus.valueOf(reader.nextString());
				} else if (key.equals("hNodeId") && reader.peek() != JsonToken.NULL) {
					hNodeId = reader.nextString();
				} else if (key.equals("columnName") && reader.peek() != JsonToken.NULL) {
					columnName = reader.nextString();
				} else if (key.equals("userSemanticTypes") && reader.peek() != JsonToken.NULL) {
					userSemanticTypes = new ArrayList<SemanticType>();
					reader.beginArray();
				    while (reader.hasNext()) {
				    	SemanticType semanticType = readSemanticType(reader);
				    	userSemanticTypes.add(semanticType);
					}
			    	reader.endArray();				} 
				else if (key.equals("learnedSemanticTypes") && reader.peek() != JsonToken.NULL) {
					learnedSemanticTypes = new ArrayList<SemanticType>();
					reader.beginArray();
				    while (reader.hasNext()) {
				    	SemanticType semanticType = readSemanticType(reader);
				    	learnedSemanticTypes.add(semanticType);
					}
			    	reader.endArray();				
				} else {
					reader.skipValue();
				}
			}
	    	reader.endObject();
	    	
	    	Node n = null;
	    	if (type == NodeType.InternalNode) {
	    		n = new InternalNode(id, new Label(uri));
	    	} else if (type == NodeType.ColumnNode) {
	    		n = new ColumnNode(id, hNodeId, columnName, null);
	    		if (userSemanticTypes != null) {
		    		for (SemanticType st : userSemanticTypes)
		    			((ColumnNode)n).assignUserType(st);
	    		}
	    		((ColumnNode)n).setLearnedSemanticTypes(learnedSemanticTypes);
//	    		((ColumnNode)n).setSemanticTypeStatus(semanticTypeStatus);
	    	} else {
	    		return null;
	    	}
	    	
	    	return n;
		}	
	
	private static SemanticType readSemanticType(JsonReader reader) throws IOException {

		String domain = null;
		String type = null;
		Origin origin = null;
		Double confidenceScore = null;
		
		reader.beginObject();
	    while (reader.hasNext()) {
	    	String key = reader.nextName();
			if (key.equals("domain") && reader.peek() != JsonToken.NULL) {
				domain = reader.nextString();
			} else if (key.equals("type") && reader.peek() != JsonToken.NULL) {
				type = reader.nextString();
			} else if (key.equals("origin") && reader.peek() != JsonToken.NULL) {
				origin = Origin.valueOf(reader.nextString());
			} else if (key.equals("confidenceScore") && reader.peek() != JsonToken.NULL) {
				confidenceScore = reader.nextDouble();
			} else {
			  reader.skipValue();
			}
		}
    	reader.endObject();
    	
    	SemanticType semanticType = new SemanticType(null, new Label(type), new Label(domain), origin, confidenceScore);
    	return semanticType;	
    }
		
	public String writeJson() throws IOException {

		OutputStream out = new ByteArrayOutputStream(); 
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		writer.setIndent("    ");
		try {
			writer.beginObject();
			
			writer.name("nodes");
			writer.beginArray();
			if (nodes != null)
				for (Node l : nodes)
					writeNode(writer, l, false);
			writer.endArray();
			
			writer.endObject();
			writer.close();

			return out.toString();
		} catch (Exception e) {
	    	writer.close();
			throw e;
	    } 
	}
	
	private static void writeNode(JsonWriter writer, Node node, boolean writeNodeAnnotations) throws IOException {
		
		if (node == null)
			return;
		
		String nullStr = null;
		
		writer.beginObject();
		writer.name("id").value(node.getId());
		if (node instanceof InternalNode) 
			writer.name("uri").value(node.getUri());
		writer.name("type").value(node.getType().toString());
//		SemanticTypeUtil semUtil = new SemanticTypeUtil();
		if (node instanceof ColumnNode) {
			ColumnNode cn = (ColumnNode) node;
			writer.name("hNodeId").value(cn.getHNodeId());
			writer.name("columnName").value(cn.getColumnName());
//			writer.name("semanticTypeStatus").value(cn.getSemanticTypeStatus().toString());
			writer.name("userSemanticTypes");
			if (cn.getUserSemanticTypes() == null) writer.value(nullStr);
			else {
				writer.beginArray();
				for (SemanticType semanticType : cn.getUserSemanticTypes())
					writeSemanticType(writer, semanticType);
				writer.endArray();
			}
			writer.name("learnedSemanticTypes");
			if (cn.getLearnedSemanticTypes() == null) writer.value(nullStr);
			else {
				writer.beginArray();
				for (SemanticType semanticType : cn.getLearnedSemanticTypes())
					writeSemanticType(writer, semanticType);
				writer.endArray();
			}
		}
		writer.endObject();
	}
	
	private static void writeSemanticType(JsonWriter writer, SemanticType semanticType) throws IOException {
		
		if (semanticType == null)
			return;
		
		String nullStr = null;
		
		writer.beginObject();
		writer.name("domain");
		if (semanticType.getDomain() == null) writer.value(nullStr);
		else writer.value(semanticType.getDomain().getUri());
		writer.name("type");
		if (semanticType.getType() == null) writer.value(nullStr);
		else writer.value(semanticType.getType().getUri());
		writer.name("origin").value(semanticType.getOrigin().toString());
		writer.name("confidenceScore").value(semanticType.getConfidenceScore());
		writer.endObject();
	}
}
