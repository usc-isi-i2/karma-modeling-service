package edu.isi.modeling.webserver;

import java.util.concurrent.ConcurrentHashMap;


public class ContextParametersRegistry {
	private static ContextParametersRegistry singleton = new ContextParametersRegistry();

	private final ConcurrentHashMap<String, ContextParameterMap> homeToContextParameters = new ConcurrentHashMap<String, ContextParameterMap>();

	public static ContextParametersRegistry getInstance() {
		return singleton;
	}

	private String defaultContextId = null;
	
	public synchronized ContextParameterMap registerByHome(String homePath)
	{
		if(homePath == null || !homeToContextParameters.containsKey(homePath))
		{
			ContextParameterMap contextParameters = new ContextParameterMap(homePath);
			homeToContextParameters.putIfAbsent(contextParameters.getHomePath(), contextParameters);
			homePath = contextParameters.getHomePath();
		}
		return homeToContextParameters.get(homePath);
	}

	public ContextParameterMap getContextParameters(String homePath) {
	
		return registerByHome(homePath);
	}
	
	public ContextParameterMap getDefault()
	{
		ContextParameterMap contextMap = registerByHome(defaultContextId);	
		defaultContextId = contextMap.getId();
		return contextMap;
	}
	
	public synchronized void deregister(String contextId) {
		homeToContextParameters.remove(contextId);
	}
}
