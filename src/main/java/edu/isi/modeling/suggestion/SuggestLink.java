package edu.isi.modeling.suggestion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.isi.modeling.alignment.GraphBuilder;
import edu.isi.modeling.rep.InternalNode;
import edu.isi.modeling.rep.LabeledLink;
import edu.isi.modeling.rep.Node;


public class SuggestLink {

	public static SuggestLinkOutput suggest(GraphBuilder  graphBuilder, SuggestLinkInput input, Integer num) throws Exception {
		SuggestLinkOutput output = new SuggestLinkOutput();
		
		Set<LabeledLink> candidateLinks = new HashSet<LabeledLink>();
		List<LabeledLink> incomingLinks, outgoingLinks;
		
		String sourceUri = input.getSourceUri();
		String targetUri = input.getTargetUri();
		String key;
		
		Set<Node> nodesHavingSourceUris = graphBuilder.getUriToNodesMap().get(sourceUri);
		if (targetUri == null || targetUri.isEmpty()) {
			if (nodesHavingSourceUris != null) {
				for (Node n : nodesHavingSourceUris) {
					incomingLinks = graphBuilder.getIncomingLinks(n.getId());
					if (incomingLinks != null) candidateLinks.addAll(incomingLinks);
					outgoingLinks = graphBuilder.getOutgoingLinks(n.getId());
					if (outgoingLinks != null) candidateLinks.addAll(outgoingLinks);
				}
			}
		} else {
			Set<Node> nodesHavingTargetUris = graphBuilder.getUriToNodesMap().get(targetUri);
			if (nodesHavingSourceUris != null && nodesHavingTargetUris != null) {
				for (Node n1 : nodesHavingSourceUris) {
					for (Node n2 : nodesHavingTargetUris) {
						incomingLinks = graphBuilder.getLinks(n2.getId(), n1.getId());
						if (incomingLinks != null) candidateLinks.addAll(incomingLinks);
						outgoingLinks = graphBuilder.getLinks(n1.getId(), n2.getId());
						if (outgoingLinks != null) candidateLinks.addAll(outgoingLinks);
					}
				}
			}
			
		}

		List<LabeledLink> uniqueCandidates = new LinkedList<LabeledLink>();
		HashMap<String, LabeledLink> linkAndModelIds = new HashMap<String, LabeledLink>();
		for (LabeledLink l : candidateLinks) {
			if (l.getTarget() instanceof InternalNode) {
				key = l.getSource().getUri() + l.getUri() + l.getTarget().getUri();
			} else {
				key = l.getSource().getUri() + l.getUri();
			}
			if (!linkAndModelIds.containsKey(key)) {
				linkAndModelIds.put(key, l);
				uniqueCandidates.add(l);
			} else if (l.getModelIds() != null && !l.getModelIds().isEmpty()) {
				LabeledLink existingLink = linkAndModelIds.get(key);
				if (existingLink.getModelIds() == null) 
					existingLink.setModelIds(new HashSet<String>(l.getModelIds()));
				else
					existingLink.getModelIds().addAll(l.getModelIds());
			}
		}
		
		int count = 0;
		for (LabeledLink l : uniqueCandidates) {
			if (num == null || count < num) {
				output.getLinks().add(l);
				count++;
			}
		}

		return output;
	}


}
