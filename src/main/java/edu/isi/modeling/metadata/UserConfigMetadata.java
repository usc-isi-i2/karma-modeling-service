package edu.isi.modeling.metadata;

import java.io.File;

import edu.isi.modeling.webserver.ContextParameterMap;
import edu.isi.modeling.webserver.ContextParameterMap.ContextParameter;

public class UserConfigMetadata extends UserMetadata {

	public UserConfigMetadata(ContextParameterMap contextParameters) throws Exception {
		super(contextParameters);
	}
	
	@Override
	protected ContextParameter getDirectoryContextParameter() {

		return ContextParameter.USER_CONFIG_DIRECTORY;
	}

	@Override
	protected String getDirectoryPath() {
		return "config" + File.separator;
	}

	@Override
	public MetadataType getType() {
		return StandardUserMetadataTypes.USER_CONFIG;
	}

}
