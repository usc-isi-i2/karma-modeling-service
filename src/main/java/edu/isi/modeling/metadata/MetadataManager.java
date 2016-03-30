package edu.isi.modeling.metadata;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import edu.isi.modeling.ontology.OntologyManager;
import edu.isi.modeling.webserver.ContextParameterMap;
import edu.isi.modeling.webserver.ContextParameterMap.ContextParameter;

public class MetadataManager {

	private Map<MetadataType, Metadata> metadataTypes;
	public MetadataManager(ContextParameterMap contextParameters) throws Exception
	{
		createDirectoryForMetadata(contextParameters);
		metadataTypes = new HashMap<MetadataType, Metadata>();
		
	}
	private void createDirectoryForMetadata(ContextParameterMap contextParameters) throws Exception {
		String userDirPath = contextParameters.getParameterValue(ContextParameter.HOME_PATH);
		
		File userDir = new File(userDirPath);
		if(userDir.exists() && !userDir.isDirectory())
		{
			throw new Exception("Directory provided for user preferences is actually a file!");
		}
		if(!userDir.exists())
		{
			if(!userDir.mkdirs())
			{
				throw new Exception("Unable to create directory for HOME_PATH. Please define the environment variable HOME_PATH.");
			}
		}
	}
	public void register(Metadata metadata) throws Exception
	{
		metadataTypes.put(metadata.getType(), metadata);
	}
	
	public boolean isMetadataSupported(MetadataType type)
	{
		return metadataTypes.containsKey(type);
	}
	public void setup(String contextId, OntologyManager ontologyManager) throws Exception {
		// some meta data such as ModelLearner need preloaded-ontologies, we do this meta data first
		for(Metadata metadata : metadataTypes.values())
		{
			if (metadata instanceof OntologyMetadata)
				metadata.setup(contextId, ontologyManager);
		}
		
		for(Metadata metadata : metadataTypes.values())
		{
			if (!(metadata instanceof OntologyMetadata))
				metadata.setup(contextId, ontologyManager);
		}
		
	}

}
