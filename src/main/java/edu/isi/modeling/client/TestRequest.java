package edu.isi.modeling.client;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import edu.isi.modeling.rep.ColumnNode;
import edu.isi.modeling.rep.Label;
import edu.isi.modeling.rep.Node;
import edu.isi.modeling.rep.SemanticType;
import edu.isi.modeling.rep.SemanticType.Origin;
import edu.isi.modeling.suggestion.SuggestLinkInput;
import edu.isi.modeling.suggestion.SuggestModelInput;

public class TestRequest {

	public static void testAddModel() throws IOException {
		
		CloseableHttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 
		Scanner scanner = null;
		
	    try {
	        HttpPost httpPost = new HttpPost("http://localhost:8080/ModelingRESTJersey/rest/models");
	        
	        String filename = "/Users/mohsen/Dropbox/__Mohsen__/ISI/source-modeling/datasets/museum-29-edm/models-json/s01-cb.csv.model.json";
	        scanner = new Scanner(new File(filename));
	        scanner.useDelimiter("\\Z");
	        String json = scanner.next();
	        StringEntity body = new StringEntity(json);
	        httpPost.setEntity(body);
	        httpPost.setHeader("Accept", "application/json");
	        httpPost.setHeader("Content-type", "application/json");
	        
	        CloseableHttpResponse response = httpClient.execute(httpPost);

	        HttpEntity entity = response.getEntity();
	        String responseString = EntityUtils.toString(entity, "UTF-8");
	        System.out.println(responseString);
	        
	        // handle response here...
	    }catch (Exception ex) {
	        // handle exception here
	    } finally {
	    	if (scanner != null)
	    		scanner.close();
        	httpClient.close();
	    }
	}  
	
	public static void testDeleteModel() throws IOException {
		
		CloseableHttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 

	    try {
	        HttpDelete httpDelete = new HttpDelete("http://localhost:8080/ModelingRESTJersey/rest/models/s01-cb.csv");
	        
	        httpDelete.setHeader("Accept", "application/json");
	        
	        CloseableHttpResponse response = httpClient.execute(httpDelete);
	        
	        HttpEntity entity = response.getEntity();
	        String responseString = EntityUtils.toString(entity, "UTF-8");
	        System.out.println(responseString);
	        

	        // handle response here...
	    }catch (Exception ex) {
	        // handle exception here
	    } finally {
        	httpClient.close();
	    }
	} 
	
	public static void testSuggestLink() throws IOException {
		
		CloseableHttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 
		Scanner scanner = null;
		
	    try {
	        HttpPost httpPost = new HttpPost("http://localhost:8080/ModelingRESTJersey/rest/suggestion?type=link&number=3");
	        
//	        SuggestLinkInput input = new SuggestLinkInput(
//	        		"http://www.americanartcollaborative.org/ontology/CulturalHeritageObject");
	        SuggestLinkInput input = new SuggestLinkInput(
	        		"http://www.americanartcollaborative.org/ontology/CulturalHeritageObject",
	        		"http://www.americanartcollaborative.org/ontology/Person");
	        String json = input.writeJson();
	        System.out.println(json);
	        StringEntity body = new StringEntity(json);
	        httpPost.setEntity(body);
	        httpPost.setHeader("Accept", "application/json");
	        httpPost.setHeader("Content-type", "application/json");
	        
	        CloseableHttpResponse response = httpClient.execute(httpPost);

	        HttpEntity entity = response.getEntity();
	        String responseString = EntityUtils.toString(entity, "UTF-8");
	        System.out.println(responseString);
	        
	        // handle response here...
	    }catch (Exception ex) {
	        // handle exception here
	    } finally {
	    	if (scanner != null)
	    		scanner.close();
        	httpClient.close();
	    }
	}
	
	public static void testSuggestModel() throws IOException {
		
		CloseableHttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 
		Scanner scanner = null;
		
	    try {
	        HttpPost httpPost = new HttpPost("http://localhost:8080/ModelingRESTJersey/rest/suggestion?type=model&number=3");
	        
//	        SuggestLinkInput input = new SuggestLinkInput(
//	        		"http://www.americanartcollaborative.org/ontology/CulturalHeritageObject");
	        	        
	        List<Node> nodes = new LinkedList<Node>();
	        
	        ColumnNode cn1 = new ColumnNode("c1", "c1", "column1",null);
	        ColumnNode cn2 = new ColumnNode("c2", "c3", "column2",null);
	        ColumnNode cn3 = new ColumnNode("c3", "c3", "column3",null);
	        
	        SemanticType st1 = new SemanticType(null, 
	        		new Label("http://rdvocab.info/ElementsGr2/nameOfThePerson"), 
	        		new Label("http://www.americanartcollaborative.org/ontology/Person"), 
	        		Origin.TfIdfModel, 0.6812);

	        SemanticType st2 = new SemanticType(null, 
	        		new Label("http://rdvocab.info/ElementsGr2/nameOfThePerson"), 
	        		new Label("http://www.americanartcollaborative.org/ontology/Person"), 
	        		Origin.TfIdfModel, 0.9812);

	        SemanticType st3 = new SemanticType(null, 
	        		new Label("http://purl.org/dc/terms/title"), 
	        		new Label("http://www.americanartcollaborative.org/ontology/CulturalHeritageObject"), 
	        		Origin.TfIdfModel, 0.7812);


	        SemanticType st4 = new SemanticType(null, 
	        		new Label("http://isi.edu/integration/karma/dev#classLink"), 
	        		new Label("http://www.europeana.eu/schemas/edm/WebResource"), 
	        		Origin.TfIdfModel, 0.75);

	        List<SemanticType> c1LearnedSemanticTypes = new LinkedList<SemanticType>();
	        c1LearnedSemanticTypes.add(st1);
	        cn1.setLearnedSemanticTypes(c1LearnedSemanticTypes);

	        List<SemanticType> c2LearnedSemanticTypes = new LinkedList<SemanticType>();
	        c2LearnedSemanticTypes.add(st2);
	        c2LearnedSemanticTypes.add(st3);
	        cn2.setLearnedSemanticTypes(c2LearnedSemanticTypes);

	        List<SemanticType> c3UserSemanticTypes = new LinkedList<SemanticType>();
	        c3UserSemanticTypes.add(st4);
	        cn3.setUserSemanticTypes(c3UserSemanticTypes);

	        nodes.add(cn1);
	        nodes.add(cn2);
	        nodes.add(cn3);
	        SuggestModelInput input = new SuggestModelInput(nodes);

	        String json = input.writeJson();
	        
	        System.out.println(json);
	        
	        StringEntity body = new StringEntity(json);
	        httpPost.setEntity(body);
	        httpPost.setHeader("Accept", "application/json");
	        httpPost.setHeader("Content-type", "application/json");
	        
	        CloseableHttpResponse response = httpClient.execute(httpPost);

	        HttpEntity entity = response.getEntity();
	        String responseString = EntityUtils.toString(entity, "UTF-8");
	        System.out.println(responseString);
	        
	        // handle response here...
	    }catch (Exception ex) {
	        // handle exception here
	    } finally {
	    	if (scanner != null)
	    		scanner.close();
        	httpClient.close();
	    }
	}
	
	public static void main(String[] args) throws IOException {
//		testAddModel();
//		testDeleteModel();
		testSuggestLink();
//		testSuggestModel();
	}
}
