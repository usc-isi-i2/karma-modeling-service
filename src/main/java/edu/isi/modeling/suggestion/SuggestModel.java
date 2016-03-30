package edu.isi.modeling.suggestion;

import java.util.List;

import edu.isi.modeling.alignment.GraphBuilder;
import edu.isi.modeling.alignment.SemanticModel;
import edu.isi.modeling.learner.ModelLearner;
import edu.isi.modeling.rep.Node;

public class SuggestModel {

	public static SuggestModelOutput suggest(GraphBuilder  graphBuilder, SuggestModelInput input, Integer num) 
	throws Exception{
		
		if (input == null)
			throw new Exception("input is null");
		
		List<Node> nodes = input.getNodes();

		ModelLearner modelLearner;

		modelLearner = new ModelLearner(graphBuilder, nodes);
		
		List<SemanticModel> models = modelLearner.getModels(num);
		if (models == null) {
			throw new Exception("error in generating a semantic model");
		}

		SuggestModelOutput output = new SuggestModelOutput(models);

		return output;
	}
	
}
