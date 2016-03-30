


/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.modeling.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelingConfiguration {

	private static Logger logger = LoggerFactory.getLogger(ModelingConfiguration.class);


	private static Boolean thingNode;
	private static Boolean nodeClosure;
	private static Boolean propertiesDirect;
	private static Boolean propertiesIndirect;
	private static Boolean propertiesWithOnlyDomain;
	private static Boolean propertiesWithOnlyRange;
	private static Boolean propertiesWithoutDomainRange;
	private static Boolean propertiesSubClass;

	private static String userHomeDir;

	private static Boolean trainOnApplyHistory;
	private static Boolean predictOnApplyHistory;

	private static Boolean compatibleProperties;
	private static Boolean ontologyAlignment;
	private static Boolean knownModelsAlignment;


	private static Integer numCandidateMappings;
	private static Integer mappingBranchingFactor;
	private static Integer topKSteinerTree;


	private static Double scoringConfidenceCoefficient;
	private static Double scoringCoherenceSCoefficient;
	private static Double scoringSizeCoefficient;

	private static Boolean learnerEnabled;
	private static Boolean addOntologyPaths;
//	private static Boolean learnAlignmentEnabled;
	private static Boolean multipleSamePropertyPerNode;
	
	private final static String newLine = System.getProperty("line.separator").toString();
	
	private static String defaultModelingProperties = 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Home Directory" + newLine + 
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"" + newLine + 
			"user.home.dir=." + newLine + 
			"" + newLine + 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Semantic Typing" + newLine + 
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"" + newLine + 
			"train.on.apply.history=false" + newLine + 
			"predict.on.apply.history=false" + newLine + 
			"" + newLine + 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Alignment" + newLine + 
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"" + newLine + 
//			"manual.alignment=false" + newLine + 
			"# turning off the next two flags is equal to manual alignment" + newLine + 
			"compatible.properties=false" + newLine + 
			"ontology.alignment=false" + newLine + 
			"knownmodels.alignment=false" + newLine + 
			"" + newLine + 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Graph Builder" + newLine + 
			"# (the flags in this section will only take effect when the \"ontology.alignment\" is true)" + newLine +
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"" + newLine + 
			"thing.node=false" + newLine + 
			"" + newLine + 
			"node.closure=true" + newLine + 
			"" + newLine + 
			"properties.direct=true" + newLine + 
			"properties.indirect=true" + newLine + 
			"properties.subclass=true" + newLine + 
			"properties.with.only.domain=true" + newLine + 
			"properties.with.only.range=true" + newLine + 
			"properties.without.domain.range=false" + newLine + 
			"" + newLine + 
			"##########################################################################################" + newLine + 
			"#" + newLine + 
			"# Model Learner" + newLine + 
			"#" + newLine + 
			"##########################################################################################" + newLine + 
			"" + newLine + 
			"learner.enabled=true" + newLine + 
			"" + newLine + 
			"add.ontology.paths=false" + newLine + 
			"" + newLine + 
//			"learn.alignment.enabled=false" + newLine + 
//			"" + newLine + 
			"mapping.branching.factor=50" + newLine + 
			"num.candidate.mappings=10" + newLine + 
			"topk.steiner.tree=10" + newLine + 
			"multiple.same.property.per.node=false" + newLine + 
			"" + newLine + 
			"# scoring coefficients, should be in range [0..1]" + newLine + 
			"scoring.confidence.coefficient=1.0" + newLine + 
			"scoring.coherence.coefficient=1.0" + newLine + 
			"scoring.size.coefficient=0.5" + newLine + 
			"" + newLine 
			;


	public static void load() {
		try {
			Properties modelingProperties = loadParams();

			userHomeDir = modelingProperties.getProperty("user.home.dir", ".");

			trainOnApplyHistory = Boolean.parseBoolean(modelingProperties.getProperty("train.on.apply.history", "false"));
			
			predictOnApplyHistory = Boolean.parseBoolean(modelingProperties.getProperty("predict.on.apply.history", "false"));
			
			compatibleProperties = Boolean.parseBoolean(modelingProperties.getProperty("compatible.properties", "false"));			
			
			ontologyAlignment = Boolean.parseBoolean(modelingProperties.getProperty("ontology.alignment", "false"));
			
			knownModelsAlignment = Boolean.parseBoolean(modelingProperties.getProperty("knownmodels.alignment", "false"));
			
			learnerEnabled = Boolean.parseBoolean(modelingProperties.getProperty("learner.enabled", "true"));

			addOntologyPaths = Boolean.parseBoolean(modelingProperties.getProperty("add.ontology.paths", "true"));
			
			thingNode = Boolean.parseBoolean(modelingProperties.getProperty("thing.node", "false"));

			nodeClosure = Boolean.parseBoolean(modelingProperties.getProperty("node.closure", "true"));

			propertiesDirect = Boolean.parseBoolean(modelingProperties.getProperty("properties.direct", "true"));

			propertiesIndirect = Boolean.parseBoolean(modelingProperties.getProperty("properties.indirect", "true"));

			propertiesWithOnlyDomain = Boolean.parseBoolean(modelingProperties.getProperty("properties.with.only.domain", "true"));

			propertiesWithOnlyRange = Boolean.parseBoolean(modelingProperties.getProperty("properties.with.only.range", "true"));

			propertiesWithoutDomainRange = Boolean.parseBoolean(modelingProperties.getProperty("properties.without.domain.range", "false"));

			propertiesSubClass = Boolean.parseBoolean(modelingProperties.getProperty("properties.subclass", "true"));

			mappingBranchingFactor = Integer.parseInt(modelingProperties.getProperty("mapping.branching.factor", "10"));

			numCandidateMappings = Integer.parseInt(modelingProperties.getProperty("num.candidate.mappings", "10"));

			topKSteinerTree = Integer.parseInt(modelingProperties.getProperty("topk.steiner.tree", "20"));

			multipleSamePropertyPerNode = Boolean.parseBoolean(modelingProperties.getProperty("multiple.same.property.per.node", "false"));

			scoringConfidenceCoefficient = Double.parseDouble(modelingProperties.getProperty("scoring.confidence.coefficient", "1"));

			scoringCoherenceSCoefficient = Double.parseDouble(modelingProperties.getProperty("scoring.coherence.coefficient", "1"));

			scoringSizeCoefficient = Double.parseDouble(modelingProperties.getProperty("scoring.size.coefficient", "0.5"));

		} catch (IOException e) {
			logger.error("Error occured while reading config file ...", e);
			System.exit(1);
		}
	}

	private static Properties loadParams()
			throws IOException {
		Properties prop = new Properties();

		logger.info("Load modeling.properties... ");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream("modeling.properties");

		if(input == null) {
			InputStream in = IOUtils.toInputStream(defaultModelingProperties, "UTF-8");			
			logger.info("Used default modeling properties");
			prop.load(in);
		} else {
			prop.load(input);
		}
		
		logger.debug("Done Loading modeling.properties");


		return prop;
	}

	public static String getUserHomeDir() {
		if (userHomeDir == null)
			load();
		return userHomeDir.trim();
	}

	public static Boolean getThingNode() {

		if (getOntologyAlignment() == false)
			return false;

		if (thingNode == null)
			load();

		return thingNode;
	}

	public static Boolean getNodeClosure() {

		if (getOntologyAlignment() == false)
			return false;

		if (nodeClosure == null)
			load();

		return nodeClosure;
	}

//	public static Boolean getManualAlignment() {
//		if (manualAlignment == null) {
//			load();
//			logger.debug("Manual Alignment:" + manualAlignment);
//		}
//		return manualAlignment;
//	}
	
	public static Boolean getTrainOnApplyHistory() {
		if (trainOnApplyHistory == null) {
			load();
		}
		return trainOnApplyHistory;
	}
	
	public static Boolean getPredictOnApplyHistory() {
		if (predictOnApplyHistory == null) {
			load();
		}
		return predictOnApplyHistory;
	}

	public static Boolean getCompatibleProperties() {
		if (compatibleProperties == null) {
			load();
		}
		return compatibleProperties;
	}

	
	public static Boolean getOntologyAlignment() {
		if (ontologyAlignment == null) {
			load();
			logger.debug("Use Ontology in Alignment:" + ontologyAlignment);
		}
		return ontologyAlignment;
	}
	
	public static Boolean getKnownModelsAlignment() {
		if (knownModelsAlignment == null) {
			load();
			logger.debug("Use Known Models in Alignment:" + knownModelsAlignment);
		}
		return knownModelsAlignment;
	}
	
	public static Boolean getPropertiesDirect() {
		if (propertiesDirect == null)
			load();
		return propertiesDirect;
	}

	public static Boolean getPropertiesIndirect() {
		if (propertiesIndirect == null)
			load();
		return propertiesIndirect;
	}

	public static Boolean getPropertiesWithOnlyDomain() {
		if (propertiesWithOnlyDomain == null)
			load();
		return propertiesWithOnlyDomain;
	}

	public static Boolean getPropertiesWithOnlyRange() {
		if (propertiesWithOnlyRange == null)
			load();
		return propertiesWithOnlyRange;
	}

	public static Boolean getPropertiesWithoutDomainRange() {
		if (propertiesWithoutDomainRange == null)
			load();
		return propertiesWithoutDomainRange;
	}

	public static Boolean getPropertiesSubClass() {
		if (propertiesSubClass == null)
			load();
		return propertiesSubClass;
	}

	public static Integer getNumCandidateMappings() {
		if (numCandidateMappings == null)
			load();
		return numCandidateMappings;
	}

	public static Integer getMappingBranchingFactor() {
		if (mappingBranchingFactor == null)
			load();
		return mappingBranchingFactor;
	}
	
	public static Integer getTopKSteinerTree() {
		if (topKSteinerTree == null)
			load();
		return topKSteinerTree;
	}

	public static Double getScoringConfidenceCoefficient() {
		if (scoringConfidenceCoefficient == null)
			load();
		return scoringConfidenceCoefficient;
	}

	public static Double getScoringCoherenceSCoefficient() {
		if (scoringCoherenceSCoefficient == null)
			load();
		return scoringCoherenceSCoefficient;
	}

	public static Double getScoringSizeCoefficient() {
		if (scoringSizeCoefficient == null)
			load();
		return scoringSizeCoefficient;
	}

	public static boolean isLearnerEnabled() {
		if (learnerEnabled == null)
			load();
		return learnerEnabled;
	}

	public static boolean getAddOntologyPaths() {
		if (addOntologyPaths == null)
			load();
		return addOntologyPaths;
	}
	
	public static boolean isMultipleSamePropertyPerNode() {
		if (multipleSamePropertyPerNode == null)
			load();
		return multipleSamePropertyPerNode;
	}


	
}
