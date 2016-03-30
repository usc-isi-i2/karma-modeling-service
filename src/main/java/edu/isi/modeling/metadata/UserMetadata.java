package edu.isi.modeling.metadata;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.modeling.webserver.ContextParameterMap;
import edu.isi.modeling.webserver.ContextParameterMap.ContextParameter;

public abstract class UserMetadata extends Metadata{

	private static final Logger logger = LoggerFactory.getLogger(UserMetadata.class);
	
	public UserMetadata(ContextParameterMap contextParameters) throws Exception
	{
		super(contextParameters);
	}
	
	protected void createDirectoryForMetadata(ContextParameterMap contextParameters, ContextParameter parameter, String directory) throws Exception {
		
		String metadataDirPath = contextParameters.getParameterValue(parameter);
		String userDirPath = contextParameters.getParameterValue(ContextParameter.HOME_PATH);
		metadataDirPath = userDirPath + directory;
		contextParameters.setParameterValue(parameter, metadataDirPath);
		
		logger.info("Set parameter: " + parameter + " -> " + metadataDirPath);
		try{ 
			createDirectory(metadataDirPath);
		}
		catch(Exception  e)
		{
			logger.error("Unable to create directory for " + parameter.name());
			throw new Exception("Unable to create directory for " + parameter.name(), e);
		}
	}

	protected void createDirectory(
			String metadataDirPath) throws Exception {
		File metadataDir = new File(metadataDirPath);
		if(metadataDir.exists() && !metadataDir.isDirectory())
		{
			throw new Exception("Directory provided is actually a file!" + metadataDirPath);
		}
		if(!metadataDir.exists())
		{
			if(!metadataDir.mkdirs())
			{
				throw new Exception("Unable to create directory for metadata! " + metadataDirPath);
			}
		}
	}
	protected void createFile(
			String metadataFilePath) throws Exception {
		File metadataFile = new File(metadataFilePath);
		if(!metadataFile.exists())
		{
			try{
				metadataFile.createNewFile();
			}
			catch (IOException e)
			{
				throw new Exception("Unable to create directory for metadata! " + metadataFilePath, e);
			}
		}
	}
	
}
