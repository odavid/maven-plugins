package com.github.ohaddavid.maven.plugins.mojo.descriptor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MojoDescriptorsBuilder {
	private static final MojoDescriptorsBuilder instance = new MojoDescriptorsBuilder();
	private MojoDescriptorsBuilder(){}

	public static final MojoDescriptorsBuilder getInstance(){ return instance;}

	public Collection<MojoDescriptor> parseAll(Node node) throws XPathExpressionException{
		Map<String,MojoDescriptor> descriptorMap = new HashMap<>();

		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList mojoList = (NodeList)xpath.evaluate("//mojo", node, XPathConstants.NODESET);
		for(int i = 0; i < mojoList.getLength(); i++){
			Node mojoNode = mojoList.item(i);
			MojoDescriptor descriptor = parseDescriptor(mojoNode);
			if(descriptorMap.containsKey(descriptor.getName())){
				throw new IllegalArgumentException("Mojo " + descriptor.getName() + " already exist");
			}
			descriptorMap.put(descriptor.getName(), descriptor);
		}

		Collection<MojoDescriptor> mojos = processMojos(descriptorMap);
		return mojos;
	}

	private Collection<MojoDescriptor> processMojos( Map<String, MojoDescriptor> descriptorMap) {
		DirectedGraph<MojoDescriptor, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		for(MojoDescriptor descriptor: descriptorMap.values()){
			graph.addVertex(descriptor);
			if(!StringUtils.isEmpty(descriptor.getBaseDescriptor())){
				MojoDescriptor base = descriptorMap.get(descriptor.getBaseDescriptor());
				if(base == null){
					throw new IllegalArgumentException("base descriptor not found: " + descriptor.getBaseDescriptor() + " declared in " + descriptor.getName());
				}
				if(!graph.vertexSet().contains(base)){
					graph.addVertex(base);
				}
				graph.addEdge(descriptor, base);
			}
		}
		CycleDetector<MojoDescriptor, DefaultEdge> cycleDetector = new CycleDetector<>(graph);
		Set<MojoDescriptor> cycles = cycleDetector.findCycles();
		if(cycles != null && !cycles.isEmpty()){
			throw new IllegalStateException("found cyclic dependencies in inheritance tree");
		}
		List<MojoDescriptor> descriptors = new ArrayList<>();
		for(MojoDescriptor descriptor: descriptorMap.values()){
			fillParentParams(descriptor, descriptorMap);
			if(!descriptor.isAbstract()){
				descriptors.add(descriptor);
			}
		}
		return descriptors;
	}

	private void fillParentParams(MojoDescriptor descriptor, Map<String,MojoDescriptor> context){
		if(!StringUtils.isEmpty(descriptor.getBaseDescriptor())){
			MojoDescriptor base = context.get(descriptor.getBaseDescriptor());
			fillParentParams(base, context);
			for(Parameter param: base.params()){
				if(!descriptor.paramKeys().contains(param.getName())){
					descriptor.addParam(param);
				}
			}
		}
	}


	public MojoDescriptor parseDescriptor(Node node) throws XPathExpressionException{
		XPath xpath = XPathFactory.newInstance().newXPath();
		String name = (String)xpath.evaluate("@name", node, XPathConstants.STRING);
		String isAbstractStr = (String)xpath.evaluate("@abstract", node, XPathConstants.STRING);
		String call = (String)xpath.evaluate("@call", node, XPathConstants.STRING);
		String phase = (String)xpath.evaluate("@phase", node, XPathConstants.STRING);
		String requiresDependencyResolution = (String)xpath.evaluate("@requiresDependencyResolution", node, XPathConstants.STRING);
		String baseDescriptor = (String)xpath.evaluate("@base", node, XPathConstants.STRING);
		String requiresProjectStr = (String)xpath.evaluate("@requiresProject", node, XPathConstants.STRING);

		boolean requiresProject = requiresProjectStr == null ? false : Boolean.valueOf(requiresProjectStr);
		boolean isAbstract = isAbstractStr == null ? false : Boolean.valueOf(isAbstractStr);
		if(StringUtils.isEmpty(name)){
			throw new IllegalStateException("mojo name is required");
		}

		if(StringUtils.isEmpty(phase) && !isAbstract){
			throw new IllegalStateException("phase is required for concrete mojo " + name);
		}

		if(name.equals(baseDescriptor)){
			throw new IllegalStateException("mojo " + name + " cannot inherit from itself");
		}
		call = StringUtils.isEmpty(call) ? name : call;
		requiresDependencyResolution = StringUtils.isEmpty(requiresDependencyResolution) ? "test" : requiresDependencyResolution;

		String description = (String)xpath.evaluate("@description", node, XPathConstants.STRING);


		MojoDescriptor descriptor = new MojoDescriptor();
		descriptor.setAbstract(isAbstract);
		descriptor.setName(name);
		descriptor.setBaseDescriptor(baseDescriptor);
		descriptor.setCall(call);
		descriptor.setPhase(phase);
		descriptor.setRequiresDependencyResolution(requiresDependencyResolution);
		descriptor.setRequiresProject(requiresProject);
		descriptor.setDescription(description);
		NodeList params = (NodeList) xpath.evaluate("parameter", node, XPathConstants.NODESET);
		for(int i = 0; i < params.getLength(); i++){
			Node paramNode = params.item(i);
			Parameter param = parseParameter(descriptor, paramNode);
			descriptor.addParam(param);
		}
		return descriptor;
	}

	public Parameter parseParameter(MojoDescriptor mojoDescriptor, Node node) throws XPathExpressionException{
		XPath xpath = XPathFactory.newInstance().newXPath();

		String name = (String)xpath.evaluate("@name", node, XPathConstants.STRING);
		String expression = (String)xpath.evaluate("@expression", node, XPathConstants.STRING);
		String required = (String)xpath.evaluate("@required", node, XPathConstants.STRING);
		String readonly = (String)xpath.evaluate("@readonly", node, XPathConstants.STRING);
		String type = (String)xpath.evaluate("@type", node, XPathConstants.STRING);
		String defaultValue = (String)xpath.evaluate("@defaultValue", node, XPathConstants.STRING);
		String description = (String)xpath.evaluate("@description", node, XPathConstants.STRING);

		if(StringUtils.isEmpty(name)){
			throw new IllegalStateException("param name is required for mojo: " + mojoDescriptor.getName());
		}
		type = StringUtils.isEmpty(type) ? String.class.getName() : type;

		Parameter param = new Parameter();
		param.setName(name);
		param.setDefaultValue(defaultValue);
		param.setExpression(expression);
		param.setReadonly(readonly == null ? false : Boolean.valueOf(readonly));
		param.setRequired(required == null ? false : Boolean.valueOf(required));
		param.setType(type);
		param.setDescription(description);
		return param;
	}

	public static void main(String[] args) throws Exception {
		File mojoFile = new File("src/test/resources/test-mojos.xml");
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.parse(mojoFile);
		Collection<MojoDescriptor> mojos = MojoDescriptorsBuilder.getInstance().parseAll(document);
		MojoDescriptorsTransformer.getInstance().transform(mojos, System.out);

	}
}
