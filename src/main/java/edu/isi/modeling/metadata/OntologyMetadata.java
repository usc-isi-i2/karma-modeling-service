package edu.isi.modeling.metadata;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.modeling.common.EncodingDetector;
import edu.isi.modeling.ontology.OntologyManager;
import edu.isi.modeling.webserver.ContextParameterMap;
import edu.isi.modeling.webserver.ContextParameterMap.ContextParameter;

public class OntologyMetadata extends UserMetadata {

	private static final Logger logger = LoggerFactory.getLogger(OntologyMetadata.class);
	
	public OntologyMetadata(ContextParameterMap contextParameters) throws Exception
	{
		super(contextParameters);
	}
	
	@Override
	public void setup(String contextId, OntologyManager ontologyManager) throws Exception {
		logger.info("Start OntologyMetadata.setup");
		/** Check if any ontology needs to be preloaded **/
		String preloadedOntDir = contextParameters.getParameterValue(ContextParameterMap.ContextParameter.PRELOADED_ONTOLOGY_DIRECTORY);
		File ontDir = new File(preloadedOntDir);
		logger.info("Load ontologies from " + preloadedOntDir);
		if (ontDir.exists()) {
			File[] ontologies = ontDir.listFiles();
			for (File ontology: ontologies) {
				if(ontology.getName().startsWith(".") || ontology.isDirectory()) {
					continue; //Ignore . files
				}
				if (ontology.getName().endsWith(".owl") || 
						ontology.getName().endsWith(".rdf") || 
						ontology.getName().endsWith(".n3") || 
						ontology.getName().endsWith(".nt") || 
						ontology.getName().endsWith(".ttl") || 
						ontology.getName().endsWith(".xml")) {
					if(ontology.getName().matches("catalog\\-v[0-9]{3}\\.xml")) {
						logger.info("Ignore: " + ontology.getAbsolutePath());
						continue; //ignore mac catalog-v001.xml file
					}
					logger.info("Loading ontology file: " + ontology.getAbsolutePath());
					try {
						String encoding = EncodingDetector.detect(ontology);
						ontologyManager.doImport(ontology, encoding);
					} catch (Exception t) {
						logger.error ("Error loading ontology: " + ontology.getAbsolutePath(), t);
						throw new Exception("Error loading ontology: " + ontology.getAbsolutePath());
					}
				} else {
					logger.error ("the file: " + ontology.getAbsolutePath() + " does not have proper format: xml/rdf/n3/ttl/owl");
					throw new Exception("Error loading ontology: " + ontology.getAbsolutePath() + ". The file does not have proper format: xml/rdf/n3/ttl/owl");
				}
			}
			// update the cache at the end when all files are added to the model
			ontologyManager.updateCache();
		} else {
			logger.info("No directory for preloading ontologies exists.");
		}
	}
	
	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.PRELOADED_ONTOLOGY_DIRECTORY;
	}
	@Override
	protected String getDirectoryPath() {
		return "preloaded-ontologies/";
	}
	@Override
	public MetadataType getType() {
		return StandardUserMetadataTypes.ONTOLOGY;
	}
}
