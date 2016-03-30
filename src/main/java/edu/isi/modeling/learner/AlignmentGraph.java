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

package edu.isi.modeling.learner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import edu.isi.modeling.alignment.GraphBuilder;
import edu.isi.modeling.alignment.GraphBuilderTopK;
import edu.isi.modeling.alignment.GraphUtil;
import edu.isi.modeling.alignment.GraphVizLabelType;
import edu.isi.modeling.alignment.GraphVizUtil;
import edu.isi.modeling.alignment.LinkIdFactory;
import edu.isi.modeling.alignment.NodeIdFactory;
import edu.isi.modeling.alignment.SemanticModel;
import edu.isi.modeling.common.ModelingParams;
import edu.isi.modeling.common.RandomGUID;
import edu.isi.modeling.config.ModelingConfiguration;
import edu.isi.modeling.ontology.OntologyManager;
import edu.isi.modeling.rep.ColumnNode;
import edu.isi.modeling.rep.DefaultLink;
import edu.isi.modeling.rep.InternalNode;
import edu.isi.modeling.rep.Label;
import edu.isi.modeling.rep.LabeledLink;
import edu.isi.modeling.rep.LinkStatus;
import edu.isi.modeling.rep.Node;
import edu.isi.modeling.rep.NodeSupportingModelsComparator;
import edu.isi.modeling.webserver.ContextParameterMap;
import edu.isi.modeling.webserver.ContextParameterMap.ContextParameter;
import edu.isi.modeling.webserver.ContextParametersRegistry;

public class AlignmentGraph {

	private static Logger logger = LoggerFactory.getLogger(AlignmentGraph.class);
	
	private static ConcurrentHashMap<OntologyManager, AlignmentGraph> instances = new ConcurrentHashMap<OntologyManager, AlignmentGraph>();
	protected OntologyManager ontologyManager;
	protected GraphBuilder graphBuilder;
	protected NodeIdFactory nodeIdFactory; 
	protected long lastUpdateTime;
	protected int totalNumberOfKnownModels;
	
	private final String getGraphJsonName()
	{
		ContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(ontologyManager.getContextId());
		return contextParameters.getParameterValue(ContextParameter.ALIGNMENT_GRAPH_DIRECTORY) + "graph.json";
	}
	private final String getGraphGraphvizName()
	{
		ContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(ontologyManager.getContextId());
		return contextParameters.getParameterValue(ContextParameter.ALIGNMENT_GRAPH_DIRECTORY) + "graph.dot";
	}

	public static AlignmentGraph getInstance(OntologyManager ontologyManager) {
		AlignmentGraph instance = null;
		AlignmentGraph previousInstance = null;
		if (!instances.containsKey(ontologyManager)) {
			try {
				instance = new AlignmentGraph(ontologyManager);
				previousInstance = instances.putIfAbsent(ontologyManager, instance);
				if(previousInstance != null)
					instance = previousInstance;
			} catch (IOException e) {
				logger.error("error in importing the main learning graph!", e);
				return null;
			}
		}
		else
		{
			instance = instances.get(ontologyManager);
		}
		return instance;
	}

	public static AlignmentGraph getEmptyInstance(OntologyManager ontologyManager) {
		AlignmentGraph instance = null;
//		ModelLearningGraph previousInstance = null;
		instance = new AlignmentGraph(ontologyManager, true);
//		previousInstance = instances.putIfAbsent(ontologyManager, instance);
//		if(previousInstance != null)
//			instance = previousInstance;
		return instance;
	}
	
	protected AlignmentGraph(OntologyManager ontologyManager) throws IOException {
		
		this.ontologyManager = ontologyManager;
		this.totalNumberOfKnownModels = 0;
		
		File file = new File(getGraphJsonName());
		if (!file.exists()) {
			this.initializeFromJsonRepository();
		} else {
			logger.info("loading the alignment graph ...");
			DirectedWeightedMultigraph<Node, DefaultLink> graph =
					GraphUtil.importJson(getGraphJsonName());
			this.graphBuilder = new GraphBuilderTopK(ontologyManager, graph);
			this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
			logger.info("loading is done!");
		}
		if (this.graphBuilder.getGraph() != null) {
			logger.info("number of nodes: " + this.graphBuilder.getGraph().vertexSet().size());
			logger.info("number of links: " + this.graphBuilder.getGraph().edgeSet().size());
		}
		this.lastUpdateTime = System.currentTimeMillis();
	}
	
	protected AlignmentGraph(OntologyManager ontologyManager, boolean emptyInstance) {
		this.ontologyManager = ontologyManager;
		this.graphBuilder = new GraphBuilderTopK(ontologyManager, false);
		this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
		this.lastUpdateTime = System.currentTimeMillis();
	}
	
	public GraphBuilder getGraphBuilder() {
		return this.graphBuilder;
	}
	
	public GraphBuilder getGraphBuilderClone() {
		GraphBuilder clonedGraphBuilder = null;
		clonedGraphBuilder = new GraphBuilderTopK(this.ontologyManager, this.getGraphBuilder().getGraph());
		return clonedGraphBuilder;
	}
	
	public NodeIdFactory getNodeIdFactory() {
		return this.nodeIdFactory;
	}
	
	public long getLastUpdateTime() {
		return this.lastUpdateTime;
	}
	
	public void initializeFromJsonRepository() {
		logger.info("initializing the graph from models in the json repository ...");
		ContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(ontologyManager.getContextId());
		this.graphBuilder = new GraphBuilderTopK(ontologyManager, false);
		this.nodeIdFactory = new NodeIdFactory();
		
		Set<InternalNode> addedNodes = new HashSet<InternalNode>();
		Set<InternalNode> temp;
		File ff = new File(contextParameters.getParameterValue(ContextParameter.JSON_MODELS_DIR));
		if (ff.exists()) {
			File[] files = ff.listFiles();
			
			for (File f : files) {
				if (f.getName().endsWith(".json")) {
					try {
						SemanticModel model = SemanticModel.readJson(f.getAbsolutePath());
						if (model != null) {
							temp = this.addModel(model, PatternWeightSystem.JWSPaperFormula);
							if (temp != null) addedNodes.addAll(temp);
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				}
			}
		}
				
		this.exportJson();
		this.exportGraphviz();
		this.lastUpdateTime = System.currentTimeMillis();
		logger.info("initialization is done!");
	}
	
	public void exportJson() {
		try {
			GraphUtil.exportJson(this.graphBuilder.getGraph(), getGraphJsonName(), true, true);
		} catch (Exception e) {
			logger.error("error in exporting the alignment graph to json!");
		}
	}
	
	public void exportGraphviz() {
		try {
			GraphVizUtil.exportJGraphToGraphviz(this.graphBuilder.getGraph(), 
					"main graph", 
					true, 
					GraphVizLabelType.LocalId,
					GraphVizLabelType.LocalUri,
					false, 
					true, 
					getGraphGraphvizName());
		} catch (Exception e) {
			logger.error("error in exporting the alignment graph to graphviz!");
		}
	}
	
	public void addModelAndUpdate(SemanticModel model, PatternWeightSystem weightSystem) {
		this.addModel(model, weightSystem);
		this.updateGraphUsingOntology(model);
	}
	
	public void addModelAndUpdateAndExport(SemanticModel model, PatternWeightSystem weightSystem) {
		this.addModel(model, weightSystem);
		this.updateGraphUsingOntology(model);
		this.exportJson();
		this.exportGraphviz();
	}
	
	private void updateGraphUsingOntology(SemanticModel model) {
		if (ModelingConfiguration.getAddOntologyPaths())
			this.graphBuilder.addClosureAndUpdateLinks(model.getInternalNodes(), null);
	}
	
	public void updateGraphUsingOntology(Set<InternalNode> nodes) {
		if (ModelingConfiguration.getAddOntologyPaths())
			this.graphBuilder.addClosureAndUpdateLinks(nodes, null);
	}
	
	public boolean contains(DirectedWeightedMultigraph<Node, LabeledLink> graph) {
		
		if (graph == null || this.graphBuilder == null || this.graphBuilder.getGraph() == null)
			return false;
		
		Set<String> commonModelIds = null;
		LabeledLink matchedLink = null;
		boolean firstVisit = true;
		for (LabeledLink l : graph.edgeSet()) {
			matchedLink = this.graphBuilder.getIdToLinkMap().get(l.getId());
			if (matchedLink == null)
				return false;
			if (firstVisit) {
				commonModelIds = matchedLink.getModelIds();
				firstVisit = false;
			} else {
				if (commonModelIds == null || matchedLink.getModelIds() == null)
					return false;
				commonModelIds = Sets.intersection(matchedLink.getModelIds(), commonModelIds);
				if (commonModelIds.size() == 0) // no intersection
					return false;
			}
		}
		
		return true;
	}
	
	private void addInternalNodes(SemanticModel model, Set<InternalNode> addedNodes) {
		
		if (model == null || model.getGraph() == null) 
			return;
		
		if (addedNodes == null) addedNodes = new HashSet<InternalNode>();

		HashMap<String, Integer> uriCount = new HashMap<String, Integer>();
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof InternalNode) {
				Integer count = uriCount.get(n.getUri());
				if (count == null) uriCount.put(n.getUri(), 1);
				else uriCount.put(n.getUri(), count.intValue() + 1);
			}
		}
		
		for (String uri : uriCount.keySet()) {
			int modelNodeCount = uriCount.get(uri);
			Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(uri);
			int graphNodeCount = matchedNodes == null ? 0 : matchedNodes.size();
			
			for (int i = 0; i < modelNodeCount - graphNodeCount; i++) {
				String id = this.nodeIdFactory.getNodeId(uri);
				Node n = new InternalNode(id, new Label(uri));
				if (this.graphBuilder.addNode(n))
					addedNodes.add((InternalNode)n);
			}
		}		
	}
	
	private HashMap<Node,Node> getInternalNodeMapping(SemanticModel model) {
		
		HashMap<Node,Node> internalNodeMapping = 
				new HashMap<Node,Node>();
		
		HashMap<String, List<Node>> uriMatchedNodes = 
				new HashMap<String, List<Node>>();
		
		String uri;
		List<Node> sortedNodes = new ArrayList<Node>();
		for (Node n : model.getGraph().vertexSet()) {
			sortedNodes.add(n);
		}
		Collections.sort(sortedNodes);
		for (Node n : sortedNodes) {
			if (n instanceof InternalNode) {
				uri = n.getUri();
				List<Node> sortedMatchedNodes = uriMatchedNodes.get(uri);
				if (sortedMatchedNodes == null) {
					sortedMatchedNodes = new ArrayList<Node>();
					Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(uri);
					if (matchedNodes != null) sortedMatchedNodes.addAll(matchedNodes);
					Collections.sort(sortedMatchedNodes, new NodeSupportingModelsComparator());
					uriMatchedNodes.put(uri, sortedMatchedNodes);
				}
			}
		}

		for (Node n : sortedNodes) {
			if (n instanceof InternalNode) {
				List<Node> sortedMatchedNodes = uriMatchedNodes.get(n.getUri());
				internalNodeMapping.put(n, sortedMatchedNodes.get(0));
				sortedMatchedNodes.remove(0);
			}
		}

		return internalNodeMapping;
	}
	
	private HashMap<Node,Node> getColumnNodeMapping(SemanticModel model, 
			HashMap<Node,Node> internalNodeMapping) {
		
		if (model == null || model.getGraph() == null) 
			return null;
		
		if (internalNodeMapping == null || internalNodeMapping.isEmpty()) 
			return null;

		HashMap<String,List<Node>> dataPropertyColumnNodes = new HashMap<String,List<Node>>();
		
		HashMap<Node,Node> columnNodeMapping = new HashMap<Node,Node>();
		
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof ColumnNode) {
				ColumnNode c = (ColumnNode)n;
				Set<LabeledLink> domainLinks = GraphUtil.getDomainLinksInLabeledGraph(model.getGraph(), (ColumnNode)n);
				if (domainLinks == null || domainLinks.isEmpty())
					continue;
				for (LabeledLink l : domainLinks) {
					if (l.getSource() == null) continue;
					Node domain = l.getSource();
					Node mappedNode = internalNodeMapping.get(domain);
					String linkUri =  l.getUri(); 
					String key =  mappedNode.getId() + linkUri;
					
					List<Node> sortedMatchedNodes;
					if (dataPropertyColumnNodes.containsKey(key)) { 
						sortedMatchedNodes = dataPropertyColumnNodes.get(key);
					} else {
						Set<Node> matchedColumnNodes = this.graphBuilder.getNodeDataProperties().get(key);
						sortedMatchedNodes = new ArrayList<Node>();
						if (matchedColumnNodes != null) sortedMatchedNodes.addAll(matchedColumnNodes);
						Collections.sort(sortedMatchedNodes, new NodeSupportingModelsComparator());
						dataPropertyColumnNodes.put(key, sortedMatchedNodes);
					}
					
					if (sortedMatchedNodes.isEmpty()) {
						ColumnNode newNode = new ColumnNode(new RandomGUID().toString(), 
								c.getHNodeId(), c.getColumnName(), c.getRdfLiteralType());
						if (this.graphBuilder.addNode(newNode)) {
							columnNodeMapping.put(n, newNode);
						}
					} else {
						columnNodeMapping.put(n, sortedMatchedNodes.get(0));
						sortedMatchedNodes.remove(0);
					}
				}
			}
		}
		return columnNodeMapping;
	}
	
	private void addLinks(SemanticModel model, 
			HashMap<Node, Node> internalNodeMapping, 
			HashMap<Node, Node> columnNodeMapping,
			PatternWeightSystem weightSystem) {
		
		if (model == null) 
			return;
		
		String modelId = model.getId();

		Node source, target;
		Node n1, n2;

		HashMap<Node,Node> mapping = new HashMap<Node,Node>();
		if (internalNodeMapping != null) mapping.putAll(internalNodeMapping);
		if (columnNodeMapping != null) mapping.putAll(columnNodeMapping);
		
		for (LabeledLink e : model.getGraph().edgeSet()) {
			
			source = e.getSource();
			target = e.getTarget();
			
			n1 = mapping.get(source);
			if (n1 == null) {
//					logger.warn("the mappings does not include the source node " + source.getId());
				continue;
			}
			
			n2 = mapping.get(target);
			if (n2 == null) {
//					logger.warn("the mappings does not include the target node " + target.getId());
				continue;
			}
			
			String id = LinkIdFactory.getLinkId(e.getUri(), n1.getId(), n2.getId());
			LabeledLink l = this.graphBuilder.getIdToLinkMap().get(id);
			if (l != null) {
				int numOfPatterns = l.getModelIds().size();
				if (weightSystem == PatternWeightSystem.OriginalWeights) {
					double currentW = l.getWeight();
					double newW = model.getGraph().getEdgeWeight(e);
					if (newW < currentW)
						this.graphBuilder.changeLinkWeight(l, newW);
				} else if (weightSystem == PatternWeightSystem.JWSPaperFormula) {
					if (n2 instanceof InternalNode) {
						// wl - x/(n+1)
						// wl = 1
						// x = (numOfPatterns + 1)
						// n = totalNumberOfPatterns
						this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT - 
								((double) (numOfPatterns + 1) / (double) (this.totalNumberOfKnownModels + 1) ));
//						this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT - (0.00001 * numOfPatterns) );
					} else {
						this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT);
					}
				} else {
					this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT);
				}
				l.getModelIds().add(modelId);
				n1.getModelIds().add(modelId);
				n2.getModelIds().add(modelId);
			} else {
//					System.out.println("added links: " + i);
//					i++;
				LabeledLink link = e.copy(id);
				
				if (link == null) {
		    		logger.error("cannot instanciate a link from the type: " + e.getType().toString());
		    		continue;
				}
				link.setStatus(LinkStatus.Normal); // all the links in learning graph are normal

				if (link.getModelIds() != null)
					link.getModelIds().clear();
				link.getModelIds().add(modelId);

				if (weightSystem == PatternWeightSystem.OriginalWeights) {
					if (!this.graphBuilder.addLink(n1, n2, link, model.getGraph().getEdgeWeight(e))) continue;
				} else {
					if (!this.graphBuilder.addLink(n1, n2, link, ModelingParams.PATTERN_LINK_WEIGHT)) continue;
				}

				n1.getModelIds().add(modelId);
				n2.getModelIds().add(modelId);
				
			}
		}
		
		DefaultLink[] graphLinks = this.graphBuilder.getGraph().edgeSet().toArray(new DefaultLink[0]); 
		for (DefaultLink e : graphLinks) {
			source = e.getSource();
			target = e.getTarget();
			if (source instanceof InternalNode && 
					target instanceof InternalNode &&
					e instanceof LabeledLink) {
				LabeledLink l = (LabeledLink)e;
				Set<Node> nodesWithSourceUri = this.graphBuilder.getUriToNodesMap().get(source.getUri());
				Set<Node> nodesWithTargetUri = this.graphBuilder.getUriToNodesMap().get(target.getUri());
				if (nodesWithSourceUri == null || nodesWithTargetUri == null) continue;
				for (Node nn1 : nodesWithSourceUri) {
					for (Node nn2 : nodesWithTargetUri) {
						if (nn1.equals(source) && nn2.equals(target)) continue;
						if (nn1.equals(nn2)) continue;
						String id = LinkIdFactory.getLinkId(l.getUri(), nn1.getId(), nn2.getId());
						LabeledLink newLink = l.copy(id);
						newLink.setModelIds(null);
						this.graphBuilder.addLink(nn1, nn2, newLink, ModelingParams.PATTERN_LINK_WEIGHT);
					}
				}
			}
		}

	}
	
	public Set<InternalNode> addModel(SemanticModel model, PatternWeightSystem weightSystem) {
				
		// adding the patterns to the graph
		
		if (model == null) 
			return null;
		
//		String modelId = model.getId();
//		if (this.graphBuilder.getModelIds().contains(modelId)) {
//			// FIXME	
//			// we need to somehow update the graph, but I don't know how to do that yet.
//			// so, we rebuild the whole graph from scratch.
//			logger.info("the graph already includes the model and needs to be updated, we re-initialize the graph from the repository!");
//			initializeFromJsonRepository();
//			return null;
//		}

		this.totalNumberOfKnownModels ++;

		// add the model  nodes that are not in the graph
		Set<InternalNode> addedInternalNodes = new HashSet<InternalNode>();
		this.addInternalNodes(model, addedInternalNodes);
		HashMap<Node, Node> internalNodeMapping = this.getInternalNodeMapping(model);

		HashMap<Node, Node> columnNodeMapping = this.getColumnNodeMapping(model, internalNodeMapping);
		
		this.addLinks(model, internalNodeMapping, columnNodeMapping, weightSystem);

		this.lastUpdateTime = System.currentTimeMillis();
		return addedInternalNodes;
	}

	public Set<InternalNode> addLodPattern(SemanticModel model) {
		
		Set<InternalNode> addedNodes = new HashSet<InternalNode>();
		
		if (model == null) 
			return addedNodes;
		
		String modelId = model.getId();

		Node source, target;
		Node n1, n2;
		
		String sourceUri, targetUri, linkUri; 
		String id, key;
		
		Set<String> mappedLinks = new HashSet<String>();
		Set<String> mappedNodes = new HashSet<String>();

		HashMap<Node,Node> mapping = new HashMap<Node,Node>();
		
		HashMap<String, List<Node>> uriMatchedNodes = new HashMap<String, List<Node>>();

		List<LabeledLink> sortedLinks = new ArrayList<LabeledLink>();
		List<LabeledLink> idSortedLinks = new ArrayList<LabeledLink>();
		idSortedLinks.addAll(model.getGraph().edgeSet());
		Collections.sort(idSortedLinks);
		for (LabeledLink l : idSortedLinks) {
			key = l.getSource().getUri() + l.getUri() + l.getTarget().getUri();
			if (this.graphBuilder.getPatternLinks().get(key) != null) {
				sortedLinks.add(l);
			}
		}
		for (LabeledLink l : idSortedLinks) {
			if (!sortedLinks.contains(l)) {
				sortedLinks.add(l);
			}
		}
		
		sortedLinks.addAll(model.getGraph().edgeSet());
//		System.out.println("new pattern ...");
		for (LabeledLink e : sortedLinks) {
			

			source = e.getSource();
			target = e.getTarget();
			
//			if (model.getId().equals("p4-022E14EC-57EC-9F3B-CBCD-F64FDFE95609")) {
//				System.out.println(GraphUtil.labeledGraphToString(model.getGraph()));
//				System.out.println(e.getId());
//			}
				
//			if (source.getId().contains("E42_Identifier") &&
//					target.getId().contains("E55_Type")) {
//				System.out.println(GraphUtil.labeledGraphToString(model.getGraph()));
//				System.out.println("debug");
//			}
			
			sourceUri = source.getUri();
			targetUri = target.getUri();
			linkUri = e.getUri();
			
			n1 = mapping.get(source);
			n2 = mapping.get(target);

			key = sourceUri + linkUri + targetUri;

//			if (target.getId().contains("E52_Time-Span2")) {
//				System.out.println(GraphUtil.labeledGraphToString(model.getGraph()));
//				System.out.println("debug");
//			}

			List<LabeledLink> matchedLinks = null;
			if (n1 == null && n2 == null) {
				matchedLinks = this.graphBuilder.getPatternLinks().get(key); 
				if (matchedLinks != null && !matchedLinks.isEmpty()) {
					Collections.sort(matchedLinks);
					for (LabeledLink l : matchedLinks) {
						if (!mappedLinks.contains(l.getId())) {
							mappedLinks.add(l.getId());
							n1 = l.getSource();
							n2 = l.getTarget();
							mapping.put(source,n1);
							mapping.put(target, n2);
							mappedNodes.add(n1.getId());
							mappedNodes.add(n2.getId());
							break;
						}
					}
				}
			} else if (n1 == null) { // target is already mapped
				matchedLinks = this.graphBuilder.getPatternLinks().get(key); 
				if (matchedLinks != null && !matchedLinks.isEmpty()) {
					for (LabeledLink l : matchedLinks) {
						if (!mappedLinks.contains(l.getId()) && 
								!mappedNodes.contains(l.getSource().getId()) &&
								l.getTarget().getId().equalsIgnoreCase(n2.getId())) {
							mappedLinks.add(l.getId());
							n1 = l.getSource();
							mapping.put(source,n1);
							mappedNodes.add(n1.getId());
							break;
						}
					}
				}
			} else if (n2 == null) {
				matchedLinks = this.graphBuilder.getPatternLinks().get(key); 
				if (matchedLinks != null && !matchedLinks.isEmpty()) {
					for (LabeledLink l : matchedLinks) {
						if (!mappedLinks.contains(l.getId()) && 
								!mappedNodes.contains(l.getTarget().getId()) &&
								l.getSource().getId().equalsIgnoreCase(n1.getId())) {
							mappedLinks.add(l.getId());
							n2 = l.getTarget();
							mapping.put(target,n2);
							mappedNodes.add(n2.getId());
							break;
						}
					}
				}
			}
				
			if (n1 == null) {
				
				List<Node> sortedMatchedNodes = uriMatchedNodes.get(sourceUri);
				if (sortedMatchedNodes == null) {
					sortedMatchedNodes = new LinkedList<Node>();
					Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(sourceUri);
					if (matchedNodes != null && !matchedNodes.isEmpty()) {
						sortedMatchedNodes.addAll(matchedNodes);
						Collections.sort(sortedMatchedNodes, new NodeSupportingModelsComparator());
						while (!sortedMatchedNodes.isEmpty()) { 
							if (mappedNodes.contains(sortedMatchedNodes.get(0).getId())) {
								sortedMatchedNodes.remove(0);
								continue;
							}
							Set<DefaultLink> outLinks = this.getGraphBuilder().getGraph().outgoingEdgesOf(sortedMatchedNodes.get(0));
							boolean okLink = true;
							if (outLinks != null) {
								for (DefaultLink dl : outLinks) {
									if (dl instanceof LabeledLink &&
											dl.getUri().equalsIgnoreCase(linkUri)) {
										sortedMatchedNodes.remove(0);
										okLink = false;
										break;
									}
								}
								if (!okLink) continue; 
							}
							break;
						}
						if (!sortedMatchedNodes.isEmpty()) {
							n1 = sortedMatchedNodes.get(0);
							mappedNodes.add(n1.getId());
							mapping.put(source, n1);
							sortedMatchedNodes.remove(0);
						}
						uriMatchedNodes.put(sourceUri, sortedMatchedNodes);
					}
				}
				if (n1 == null) {
					id = this.nodeIdFactory.getNodeId(sourceUri);
					n1 = new InternalNode(id, new Label(sourceUri));
					if (this.graphBuilder.addNode(n1)) {
						mapping.put(source, n1);
						mappedNodes.add(n1.getId());
						addedNodes.add((InternalNode)n1);
					} else {
						System.out.println("Error in adding the node " + id + " to the graph.");
					}
				} 
			}

			if (n2 == null) {
				
//				if(model.getId().equals("p4-06B7640A-8E23-B427-4B46-B1C9C194BDD7"))
//				{
//					System.out.println(GraphUtil.labeledGraphToString(model.getGraph()));	
//				}
				List<Node> sortedMatchedNodes = uriMatchedNodes.get(targetUri);
				if (sortedMatchedNodes == null) {
					sortedMatchedNodes = new LinkedList<Node>();
					Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(targetUri);
					if (matchedNodes != null && !matchedNodes.isEmpty()) {
						sortedMatchedNodes.addAll(matchedNodes);
						Collections.sort(sortedMatchedNodes, new NodeSupportingModelsComparator());
						while (!sortedMatchedNodes.isEmpty()) { 
							if (mappedNodes.contains(sortedMatchedNodes.get(0).getId())) {
								sortedMatchedNodes.remove(0);
								continue;
							}
							Set<DefaultLink> inLinks = this.getGraphBuilder().getGraph().incomingEdgesOf(sortedMatchedNodes.get(0));
							boolean okLink = true;
							if (inLinks != null) {
								for (DefaultLink dl : inLinks) {
									if (dl instanceof LabeledLink &&
											dl.getUri().equalsIgnoreCase(linkUri)) {
										sortedMatchedNodes.remove(0);
										okLink = false;
										break;
									}
								}
								if (!okLink) continue; 
							}
							break;
						}
						if (!sortedMatchedNodes.isEmpty()) {
							n2 = sortedMatchedNodes.get(0);
							mappedNodes.add(n2.getId());
							mapping.put(target, n2);
							sortedMatchedNodes.remove(0);
						}
						uriMatchedNodes.put(targetUri, sortedMatchedNodes);
					}
				}
				if (n2 == null) {
					id = this.nodeIdFactory.getNodeId(targetUri);
//					if (id.contains("E55_Type10"))
//						System.out.println("break");
					n2 = new InternalNode(id, new Label(targetUri));
					if (this.graphBuilder.addNode(n2)) {
						mapping.put(target, n2);
						addedNodes.add((InternalNode)n2);
						mappedNodes.add(n2.getId());
					} else {
						System.out.println("Error in adding the node " + id + " to the graph.");
					}
				} 
			}
			
//			if (n1 == null || n2 == null) {
//				System.out.println(GraphUtil.labeledGraphToString(model.getGraph()));
//				System.out.println("debug");
//			}
			id = LinkIdFactory.getLinkId(e.getUri(), n1.getId(), n2.getId());
			LabeledLink l = this.graphBuilder.getIdToLinkMap().get(id);
			if (l != null) {
				this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT);
				l.getModelIds().add(modelId);
				n1.getModelIds().add(modelId);
				n2.getModelIds().add(modelId);
			} else {

				LabeledLink link = e.copy(id);
				
				if (link == null) {
		    		logger.error("cannot instanciate a link from the type: " + e.getType().toString());
		    		continue;
				}
				link.setStatus(LinkStatus.Normal); // all the links in learning graph are normal

				if (link.getModelIds() != null)
					link.getModelIds().clear();
				link.getModelIds().add(modelId);

				if (!this.graphBuilder.addLink(n1, n2, link, ModelingParams.PATTERN_LINK_WEIGHT)) continue;

				this.graphBuilder.savePatternLink(link);
				n1.getModelIds().add(modelId);
				n2.getModelIds().add(modelId);
			}
		}
		
		return addedNodes;
	}
}
