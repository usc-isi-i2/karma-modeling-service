package edu.isi.modeling.learner;

public enum PatternWeightSystem {
	Default, // assign wl to all links
	OriginalWeights,
	JWSPaperFormula // w = wl - [x / (n+1)]
}
