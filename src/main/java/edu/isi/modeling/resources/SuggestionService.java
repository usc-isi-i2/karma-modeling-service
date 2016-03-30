package edu.isi.modeling.resources;

import io.swagger.annotations.Api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.isi.modeling.common.JsonUtil;
import edu.isi.modeling.common.ResponseJsonKey;
import edu.isi.modeling.learner.AlignmentGraph;
import edu.isi.modeling.ontology.OntologyManager;
import edu.isi.modeling.suggestion.SuggestLink;
import edu.isi.modeling.suggestion.SuggestLinkInput;
import edu.isi.modeling.suggestion.SuggestLinkOutput;
import edu.isi.modeling.suggestion.SuggestModel;
import edu.isi.modeling.suggestion.SuggestModelInput;
import edu.isi.modeling.suggestion.SuggestModelOutput;
import edu.isi.modeling.webserver.ModelingApplication;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@Api
@Path("/suggestion")
public class SuggestionService {

	//	private static Logger logger = LoggerFactory.getLogger(SuggestionService.class);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSuggestion(
			@QueryParam("type") String type,
			@QueryParam("number") String number,
			String json) {

		String result, msg;

		if (type == null || type.isEmpty()) {
			msg = "invalid query parameters";
			result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);
			return Response.status(400).entity(result).build();
		}

		Integer num = null;
		try {
			if (number != null && !number.isEmpty())
				num = Integer.parseInt(number);
		} catch (Exception e) {
			msg = "invalid query parameters";
			result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);
			return Response.status(400).entity(result).build();
		}

		OntologyManager ontologyManager = ModelingApplication.getOntologyManager();

		if (type.equalsIgnoreCase("link")) {

			SuggestLinkInput suggestLinkInput;
			SuggestLinkOutput suggestLinkOutput;

			try {
				suggestLinkInput = SuggestLinkInput.readJson(json);
			} catch (Exception e) {
				msg = "invalid json body";
				result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);
				return Response.status(400).entity(result).build();
			}

			try {
				suggestLinkOutput = SuggestLink.suggest(AlignmentGraph.getInstance(ontologyManager).getGraphBuilder(), suggestLinkInput, num);
			} catch (Exception e) {
				msg = "error in suggesting links";
				result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);
				return Response.status(500).entity(result).build();				
			}

			try {
				result = suggestLinkOutput.writeJson();
				return Response.status(200).entity(result).build();
			} catch (Exception e) {
				msg = "error in exporting the results into json";
				result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);
				return Response.status(500).entity(result).build();
			}

		} else if (type.equalsIgnoreCase("model")) {

			SuggestModelInput suggestModelInput;
			SuggestModelOutput suggestModelOutput;

			try {
				suggestModelInput = SuggestModelInput.readJson(json);
			} catch (Exception e) {
				msg = "invalid json body";
				result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);
				return Response.status(400).entity(result).build();
			}

			try {
				suggestModelOutput = SuggestModel.suggest(AlignmentGraph.getInstance(ontologyManager).getGraphBuilder(), suggestModelInput, num);
			} catch (Exception e) {
				msg = "error in suggesting models";
				result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);
				return Response.status(500).entity(result).build();				
			}

			try {
				result = suggestModelOutput.writeJson();
				return Response.status(200).entity(result).build();
			} catch (Exception e) {
				msg = "error in exporting the results into json";
				result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);
				return Response.status(500).entity(result).build();
			}

		} else {
			msg = "invalid query parameters";
			result = JsonUtil.getJsonString(ResponseJsonKey.ERROR, msg);
			return Response.status(400).entity(result).build();
		}
	}

}
