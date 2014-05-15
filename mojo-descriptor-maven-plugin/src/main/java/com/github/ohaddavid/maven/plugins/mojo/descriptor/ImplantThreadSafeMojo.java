package com.github.ohaddavid.maven.plugins.mojo.descriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Mojo(name = "implant-thread-safe", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class ImplantThreadSafeMojo extends AbstractMojo {
	public static final String PLUGIN_DESCRIPTOR_DIR = "/META-INF/maven/";
	public static final String PLUGIN_DESCRIPTOR_FILE = "plugin.xml";
	
	@Component
	private MavenProject mavenProject;

	@Component
	private MavenProjectHelper projectHelper;
	
	@Parameter(defaultValue = "${project.build.outputDirectory}")
	private File outputDir;
	
	@Parameter(defaultValue = "true")
	private Boolean enabled;

	
	public void execute() throws MojoExecutionException {
		if (!this.enabled) {
			log("implant-thread-safe mojo disabled");
			return;
		}
		
		//log("PLUGIN_DESCRIPTOR_DIR: " + PLUGIN_DESCRIPTOR_DIR);
		File pluginDescriptorDirectory = new File(this.outputDir + PLUGIN_DESCRIPTOR_DIR);
		//log("pluginDescriptorDirectory: " + pluginDescriptorDirectory.toString());
		File[] pluginDescriptors = pluginDescriptorDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(PLUGIN_DESCRIPTOR_FILE);
			}
		});
		
		log("Found " + pluginDescriptors.length + " plugin file descriptors");
		if (pluginDescriptors.length > 0) {
			File pluginDescriptor = pluginDescriptors[0];
			//log("Found plugin descriptor file: " + pluginDescriptor.toString());
			
			try {
				transform(pluginDescriptor);
			} catch (Exception e) {
				throw new MojoExecutionException("Could not transform plugin descriptor: " + e.getMessage(), e);
			}
		}
	}
	
	public void transform(File pluginDescriptor) throws Exception {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.parse(pluginDescriptor);
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList mojoList = (NodeList)xpath.evaluate("//mojo", document, XPathConstants.NODESET);
		for (int i = 0; i < mojoList.getLength(); i++) {
			Node mojoNode = mojoList.item(i);
			Node parametersNode = (Node)xpath.evaluate("parameters", mojoNode, XPathConstants.NODE);
			
			if (parametersNode != null) {
				Element threadSafeNode = document.createElement("threadSafe");
				threadSafeNode.setTextContent("true");
				mojoNode.insertBefore(threadSafeNode, parametersNode);
			}
		}
		
		
		FileOutputStream out = new FileOutputStream(pluginDescriptor);
		
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		t.transform(new DOMSource(document), new StreamResult(out));
	}
	
	
	private void log(String message) {
		this.getLog().info("implant-thread-safe: " + message);
	}

}
