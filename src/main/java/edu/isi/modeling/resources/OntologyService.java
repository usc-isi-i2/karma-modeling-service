package edu.isi.modeling.resources;

import io.swagger.annotations.Api;

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
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@Api
@Path("/ontologies")
public class OntologyService {

//	private static Logger logger = LoggerFactory.getLogger(ModelService.class);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOntologies(@QueryParam("details") Boolean printDetails) {
		//TODO
		return Response.status(200).build();
	}
	
	@Path("{id}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getOntology(@PathParam("id") String id) {
		//TODO
		return Response.status(200).build();
	}
	
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addOntology(@QueryParam("lang") String lang, String body) {
		//TODO
		return Response.status(201).build();
	}
	
	@Path("{id}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteOntology(@PathParam("id") String id) {
		//TODO
		return Response.status(200).build();
	}
}
