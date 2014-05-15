package com.github.ohaddavid.maven.plugins.mojo.descriptor;

import java.io.OutputStream;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MojoDescriptorsTransformer {

	private static final MojoDescriptorsTransformer instance = new MojoDescriptorsTransformer();

	public static final MojoDescriptorsTransformer getInstance(){return instance;}

	private MojoDescriptorsTransformer (){}

	public void transform(Collection<MojoDescriptor> descriptors, OutputStream out) throws Exception{
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		DomBuilder builder = new DomBuilder(document);
		builder = builder.element("pluginMetadata").element("mojos");
		for(MojoDescriptor descriptor: descriptors){
			createMojo(descriptor, builder);
		}
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		t.transform(new DOMSource(document), new StreamResult(out));
	}

	private void createMojo(MojoDescriptor descriptor, DomBuilder builder) {
		DomBuilder mojoBuilder = builder.element("mojo");
		mojoBuilder.element("call", descriptor.getCall());
		mojoBuilder.element("goal", descriptor.getName());
		mojoBuilder.element("requiresDependencyResolution", descriptor.getRequiresDependencyResolution());
		mojoBuilder.element("requiresProject", descriptor.isRequiresProject());
		mojoBuilder.element("phase", descriptor.getPhase());
		if (!StringUtils.isEmpty(descriptor.getDescription())) {
			mojoBuilder.element("description", descriptor.getDescription());
		}

		DomBuilder parametersBuilder = mojoBuilder.element("parameters");
		for(Parameter param: descriptor.params()){
			createParam(param, parametersBuilder);
		}
		createParam(Parameter.MAVEN_PROJECT, parametersBuilder);
		createParam(Parameter.MAVEN_SESSION, parametersBuilder);
		createParam(Parameter.LOCAL_REPO, parametersBuilder);
		createParam(Parameter.PLUGIN_ARTIFACTS, parametersBuilder);
	}
	private void createParam(Parameter param, DomBuilder parametersBuilder) {
		DomBuilder paramBuilder = parametersBuilder.element("parameter");
		paramBuilder.element("name", param.getName());
		paramBuilder.element("required", param.isRequired());
		paramBuilder.element("readonly", param.isReadonly());
		paramBuilder.element("type", param.getType());
		if(!StringUtils.isEmpty(param.getDefaultValue())){
			paramBuilder.element("defaultValue", param.getDefaultValue());
		}
		if(!StringUtils.isEmpty(param.getExpression())){
			paramBuilder.element("expression", param.getExpression());
		}
		if(!StringUtils.isEmpty(param.getDescription())){
			paramBuilder.element("description", param.getDescription());
		}
	}

	private static class DomBuilder{
		Document document;
		private Element element;

		public DomBuilder(Document document){
			this(document, null);
		}
		public DomBuilder(Document document, Element e){
			this.document = document;
			this.element = e;
		}
		public DomBuilder element(String tag, Boolean value){
			return element(tag, String.valueOf(value));
		}
		public DomBuilder element(String tag, String value){
			Element e = document.createElement(tag);
			if(!StringUtils.isEmpty(value)){
				e.setTextContent(value);
			}
			if(this.element == null){
				document.appendChild(e);
			}else{
				this.element.appendChild(e);
			}
			return new DomBuilder(document, e);
		}

		public DomBuilder element(String tag){
			return element(tag, (String)null);
		}
	}
}
