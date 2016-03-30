package edu.isi.modeling.webserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkspaceKarmaHomeRegistry {
	private static WorkspaceKarmaHomeRegistry singleton = new WorkspaceKarmaHomeRegistry();

	private final Map<String, String> workspaceToKarmaHome = new ConcurrentHashMap<String, String>();

	public static WorkspaceKarmaHomeRegistry getInstance() {
		return singleton;
	}

	public void register(String workspaceId, String karmaHome) {
		if(workspaceId == null)
		{
			//TODO error
		}
		workspaceToKarmaHome.put(workspaceId, karmaHome);
	}

	public String getKarmaHome(String workspaceId) {
		if(workspaceId == null)
		{
			ContextParameterMap contextParameters  =ContextParametersRegistry.getInstance().getContextParameters(null);
			return contextParameters.getHomePath();
		}
		return workspaceToKarmaHome.get(workspaceId);
	}
	
	public void deregister(String workspaceId) {
		workspaceToKarmaHome.remove(workspaceId);
	}
}
