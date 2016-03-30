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
package edu.isi.modeling.webserver;


import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextParameterMap {
	private Map<ContextParameter, String> valuesMap = new ConcurrentHashMap<ContextParameter, String>();

	protected final String id;
	
	private static Logger logger = LoggerFactory
			.getLogger(ContextParameterMap.class);
 
	public enum ContextParameter {
		HOME_PATH,
		PRELOADED_ONTOLOGY_DIRECTORY, 
		ALIGNMENT_GRAPH_DIRECTORY, 
		USER_CONFIG_DIRECTORY,
		GRAPHVIZ_MODELS_DIR,
		JSON_MODELS_DIR 
	}
	
	public ContextParameterMap(String homePath)
	{
		setup(homePath);
		id = getHomePath(); 
	}
	public void setup(String homePath) {
		
		if(!homePath.endsWith(File.separator))
		{
			homePath += File.separator;
		}
		setParameterValue(ContextParameter.HOME_PATH, homePath);
		logger.info("Home Directory: " + homePath);
		
	}
	public void setParameterValue(ContextParameter param, String value) {
		valuesMap.put(param, value);
	}

	public String getParameterValue(ContextParameter param) {
		if (valuesMap.containsKey(param))
			return valuesMap.get(param);

		return "";
	}

	public String getHomePath() {
		
		return getParameterValue(ContextParameter.HOME_PATH);
	}
	
	public String getId(){
		return id;
	}

	@Override
	public String toString(){
		
		return id + ": " + valuesMap.toString();
	}
}
