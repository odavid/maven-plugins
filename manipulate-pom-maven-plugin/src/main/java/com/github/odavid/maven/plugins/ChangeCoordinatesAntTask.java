package com.github.odavid.maven.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ChangeCoordinatesAntTask extends Task {
	private String from;
	private String to;
	private File pom;
	private File targetFile;
	

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public File getPom() {
		return pom;
	}

	public void setPom(File pom) {
		this.pom = pom;
	}

	@Override
	public void execute() throws BuildException {
		Document document = null;
		try{
			document = loadDocument();
		}catch(Exception e){
			throw new BuildException("Cannot load document " + pom, e);
		}
		Coordinates fromCoord = new Coordinates(from);
		Collection<Node> nodes = null;
		try {
			nodes = fromCoord.match(document);
		} catch (Exception e) {
			throw new BuildException("Cannot match " + from, e);
		}
		Coordinates toCoord = new Coordinates(to);
		if(nodes.size() > 0){
			for(Node node: nodes){
				try {
					toCoord.replace(node);
				} catch (Exception e) {
					throw new BuildException("Cannot replace " + from + " to " + to, e);
				}
			}
			log("Changing " + from + " to " + to + " in " + pom);
			targetFile = targetFile == null ? pom : targetFile;
			try {
				if(targetFile.exists() && !targetFile.canWrite()){
					targetFile.setWritable(true);
				}
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.transform(new DOMSource(document), new StreamResult(targetFile));
			} catch (Exception e) {
				throw new BuildException("Cannot write file " + targetFile, e);
			}
		}
	}
	
	
	private Document loadDocument() throws Exception{
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pom);
		return document;
	}
	
	public File getTargetFile() {
		return targetFile;
	}

	public void setTargetFile(File targetFile) {
		this.targetFile = targetFile;
	}

	public static class Coordinates{
		private String groupId;
		private String artifactId;
		
		public Coordinates(String coordinate){
			String[] c = coordinate.split(":");
			this.groupId = c[0];
			this.artifactId = c[1];
			if("*".equals(this.groupId)){
				this.groupId = null;
			}
			if("*".equals(this.artifactId)){
				this.artifactId = null;
			}
		}
		
		public Node replace(Node node) throws Exception{
			if(groupId != null){
				Node gid = (Node)XPathFactory.newInstance().newXPath().evaluate("groupId", node, XPathConstants.NODE);
				gid.setTextContent(groupId);
			}
			if(artifactId != null){
				Node aid = (Node)XPathFactory.newInstance().newXPath().evaluate("artifactId", node, XPathConstants.NODE);
				aid.setTextContent(artifactId);
			}
			return node;
		}
		public Collection<Node> match(Document document) throws Exception{
			String gidMatch = groupId != null ? "groupId/text() = '" + groupId + "'" : null;
			String aidMatch = artifactId != null ? "artifactId/text() = '" + artifactId + "'" : null;
			String matcher = "[";
			if(gidMatch != null){
				matcher += gidMatch;
			}
			if(aidMatch != null){
				if(gidMatch != null){
					matcher += " and ";
				}
				matcher += aidMatch;
			}
			matcher += ']';
			List<Node> nodes = new ArrayList<>();
			NodeList nodeList = (NodeList)XPathFactory.newInstance().newXPath().evaluate("//*" + matcher, document, XPathConstants.NODESET);
			for(int i = 0; i < nodeList.getLength(); i++){
				nodes.add(nodeList.item(i));
			}
			nodeList = (NodeList)XPathFactory.newInstance().newXPath().evaluate("//plugin" + matcher, document, XPathConstants.NODESET);
			for(int i = 0; i < nodeList.getLength(); i++){
				nodes.add(nodeList.item(i));
			}
			return nodes;
		}

//		public static void main(String[] args) {
//			ChangeCoordinatesAntTask task = new ChangeCoordinatesAntTask();
//			task.pom = new File("c:/dev/git/maven-plugins/ant-mojos-parent/pom.xml");
//			task.from = "com.github.odavid.maven.plugins:*";
//			task.to = "com.github.shlomo.1.1:*";
//			task.execute();
//		}
	}
}