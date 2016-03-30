package edu.isi.modeling.metadata;

import edu.isi.modeling.webserver.ContextParameterMap;
import edu.isi.modeling.webserver.ContextParameterMap.ContextParameter;

public class GraphVizMetadata extends UserMetadata {

	
	public GraphVizMetadata(ContextParameterMap contextParameters) throws Exception {
		super(contextParameters);
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.GRAPHVIZ_MODELS_DIR;
	}
	
	@Override
	protected String getDirectoryPath() {
		return "models-graphviz/";
	}

	@Override
	public MetadataType getType() {
		return StandardUserMetadataTypes.GRAPHVIZ_MODELS;
	}
}
