package edu.isi.modeling.resources;

import io.swagger.annotations.Api;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.isi.modeling.alignment.SemanticModel;
import edu.isi.modeling.common.JsonUtil;
import edu.isi.modeling.common.ResponseJsonKey;
import edu.isi.modeling.config.ModelingConfiguration;
import edu.isi.modeling.learner.AlignmentGraph;
import edu.isi.modeling.learner.PatternWeightSystem;
import edu.isi.modeling.ontology.OntologyManager;
import edu.isi.modeling.webserver.ContextParameterMap;
import edu.isi.modeling.webserver.ContextParameterMap.ContextParameter;
import edu.isi.modeling.webserver.ContextParametersRegistry;
import edu.isi.modeling.webserver.ModelingApplication;

@Api
@Path("/models")
public class ModelService {

//	private static Logger logger = LoggerFactory.getLogger(ModelService.class);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getModels(@QueryParam("details") Boolean printDetails) {
		
		ContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(ModelingApplication.getContextId());
		String modelDirPath = contextParameters.getParameterValue(ContextParameter.JSON_MODELS_DIR);
		File modelDir = new File(modelDirPath);
		File[] modelFiles = modelDir.listFiles();
		
		String result, msg;
		
		List<SemanticModel> models = new LinkedList<SemanticModel>();
		for (File f : modelFiles) {
			try {
				SemanticModel m = SemanticModel.readJson(f.getAbsolutePath());
				models.add(m);
			} catch (Exception e) {
				msg = "error in retrieving the models " + f.getName();
				result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);			
				return Response.status(500).entity(result).build();			
			}
		}
		
		try {
			if (printDetails == null || !printDetails) {
				result = SemanticModel.writeJsonToString(models, true);
			} else {
				result = SemanticModel.writeJsonToString(models, false);
			}
		} catch (Exception e) {
			msg = "error in exporting the models to json";
			result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);			
			return Response.status(500).entity(result).build();				
		}
		
		return Response.status(200).entity(result).build();
	}
	
	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getModel(@PathParam("id") String id) {

		ContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(ModelingApplication.getContextId());

		String filename = id + ".model.json";
		String filePath = contextParameters.getParameterValue(ContextParameter.JSON_MODELS_DIR) + "/" + filename;
		Scanner scanner = null;
		JsonObject resultObject;
		String result;
		try {
			scanner = new Scanner(new File(filePath));
			scanner.useDelimiter("\\Z");
	        String json = scanner.next();
			JsonParser parser = new JsonParser();
			resultObject = parser.parse(json).getAsJsonObject();
			result = resultObject.toString();
			return Response.status(200).entity(result).build();
		} catch (IOException e) {
			String msg = "error in retrieving the model " + id;
			result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);			
			return Response.status(500).entity(result).build();
		} finally {
			if (scanner != null)
				scanner.close();
		}
	
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addModel(String json) {
		SemanticModel semanticModel;
		String result, msg;
		ContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(ModelingApplication.getContextId());
		OntologyManager ontologyManager = ModelingApplication.getOntologyManager();
			
		try {
			semanticModel = SemanticModel.readJsonFromString(json);
		} catch (Exception e) {
			msg = "error in parsing the json body";
			result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);
			return Response.status(500).entity(result).build();
		}
		
		boolean modelOverwrite = false;
		String modelId = semanticModel.getId();
		String filename = modelId + ".model.json";
		String filePath = contextParameters.getParameterValue(ContextParameter.JSON_MODELS_DIR) + "/" + filename;
		File jsonFile = new File(filePath);
		if (jsonFile.exists()) {
			modelOverwrite = true;			
		} 
		
		try { 
			semanticModel.writeJson(contextParameters.getParameterValue(ContextParameter.JSON_MODELS_DIR) + 
						semanticModel.getName() + 
						".model.json");
		} catch (Exception e) {
			msg = "error in saving the json file";
			result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);
			return Response.status(500).entity(result).build();		
		}
		
		try {
			semanticModel.writeGraphviz(contextParameters.getParameterValue(ContextParameter.GRAPHVIZ_MODELS_DIR) + 
						semanticModel.getName() + 
						".model.dot", false, false);
		} catch (Exception e) {
			msg = "error in saving the graphviz file";
			result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);
			return Response.status(500).entity(result).build();
		}
		
		try {
			if (ModelingConfiguration.isLearnerEnabled()) {
				if (modelOverwrite) {
					AlignmentGraph.getInstance(ontologyManager).initializeFromJsonRepository();
					msg = "model " + semanticModel.getId() + " overwritten successfully";					
				} else {
					AlignmentGraph.getInstance(ontologyManager).
					addModelAndUpdateAndExport(semanticModel, PatternWeightSystem.JWSPaperFormula);
					msg = "model " + semanticModel.getId() + " added successfully";
				}
			} else {
				msg = "model added, but alignment graph is not updated because learner is disabled";
			}

			result = JsonUtil.getJsonString(ResponseJsonKey.MESSAGE, msg);
			return Response.status(201).entity(result).build();
			
		} catch (Exception e) {
			msg = "error in adding the model to the alignment graph";
			result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);
			return Response.status(500).entity(result).build();
		} 

	}
	
	@Path("{id}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteModel(@PathParam("id") String id) {

		ContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(ModelingApplication.getContextId());
		OntologyManager ontologyManager = ModelingApplication.getOntologyManager();

		String jsonFilename = id + ".model.json";
		String jsonFilePath = contextParameters.getParameterValue(ContextParameter.JSON_MODELS_DIR) + "/" + jsonFilename;

		String graphvizFilename = id + ".model.dot";
		String graphvizFilePath = contextParameters.getParameterValue(ContextParameter.GRAPHVIZ_MODELS_DIR) + "/" + graphvizFilename;

		String result;
		
		File jsonFile = new File(jsonFilePath);
		if (!jsonFile.exists()) {
			String msg = "errir in retrieving the model " + id;
			result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);			
			return Response.status(500).entity(result).build();			
		} 

		try {
			FileUtils.forceDelete(jsonFile);
		} catch (IOException e) {
			String msg = "error in deleting the file " + id + ".model.json";
			result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);			
			return Response.status(500).entity(result).build();
		}
		
		File graphvizFile = new File(graphvizFilePath);
		if (graphvizFile.exists()) {
			try {
				FileUtils.forceDelete(graphvizFile);
			} catch (Exception e) {
				
			}
		} 
		
		try {
			AlignmentGraph.getInstance(ontologyManager).initializeFromJsonRepository();
			String msg = "model " + id + " deleted successfully";
			result = JsonUtil.getJsonString(ResponseJsonKey.MESSAGE, msg);
			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			String msg = "error in rebuilding the alignment graph";
			result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);			
			return Response.status(500).entity(result).build();
		}


	}
}
