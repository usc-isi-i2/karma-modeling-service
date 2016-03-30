package edu.isi.modeling.metadata;

import edu.isi.modeling.ontology.OntologyManager;
import edu.isi.modeling.webserver.ContextParameterMap;
import edu.isi.modeling.webserver.ContextParameterMap.ContextParameter;

public abstract class Metadata {

	protected ContextParameterMap contextParameters; 
	
	public Metadata(ContextParameterMap contextParameters) throws Exception
	{
		this.contextParameters = contextParameters;
		createDirectoryForMetadata(contextParameters, getDirectoryContextParameter(), getDirectoryPath());
	}
	
	protected abstract ContextParameter getDirectoryContextParameter();
	protected abstract String getDirectoryPath();
	
	protected abstract void createDirectoryForMetadata(ContextParameterMap contextParameters, ContextParameter parameter, String directory) throws Exception;
	public abstract MetadataType getType();

	public void setup(String contextId, OntologyManager ontologyManager) throws Exception
	{
		
	}
}
