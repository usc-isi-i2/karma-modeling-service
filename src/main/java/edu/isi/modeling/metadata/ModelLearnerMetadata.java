package edu.isi.modeling.metadata;

import edu.isi.modeling.config.ModelingConfiguration;
import edu.isi.modeling.learner.AlignmentGraphLoaderThread;
import edu.isi.modeling.ontology.OntologyManager;
import edu.isi.modeling.webserver.ContextParameterMap;
import edu.isi.modeling.webserver.ContextParameterMap.ContextParameter;

public class ModelLearnerMetadata extends UserMetadata {

	
	public ModelLearnerMetadata(ContextParameterMap contextParameters) throws Exception
	{
		super(contextParameters);
	}
	
	@Override
	public void setup(String contextId, OntologyManager ontologyManager) {
		if (ModelingConfiguration.isLearnerEnabled())
			new AlignmentGraphLoaderThread(ontologyManager).run();
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.ALIGNMENT_GRAPH_DIRECTORY;
	}

	@Override
	protected String getDirectoryPath() {
		return "alignment-graph/";
	}

	@Override
	public MetadataType getType() {
		return StandardUserMetadataTypes.MODEL_LEARNER;
	}
}

