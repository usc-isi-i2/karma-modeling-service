package edu.isi.modeling.metadata;

import edu.isi.modeling.webserver.ContextParameterMap;
import edu.isi.modeling.webserver.ContextParameterMap.ContextParameter;

public class JSONModelsMetadata extends UserMetadata {

	
	public JSONModelsMetadata(ContextParameterMap contextParameters) throws Exception {
		super(contextParameters);
	}
	
	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.JSON_MODELS_DIR;
	}

	@Override
	protected String getDirectoryPath() {
		return "models-json/";
	}
	@Override
	public MetadataType getType() {
		return StandardUserMetadataTypes.JSON_MODELS;
	}
}
