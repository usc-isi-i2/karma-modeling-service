package edu.isi.modeling.suggestion;

import java.util.List;

import edu.isi.modeling.alignment.SemanticModel;

public class SuggestModelOutput {

	List<SemanticModel> models;
	
	public SuggestModelOutput(List<SemanticModel> models) {
		this.models = models;
	}
	
	public String writeJson() throws Exception {
		
		return SemanticModel.writeJsonToString(models, false);
		
	}
}
