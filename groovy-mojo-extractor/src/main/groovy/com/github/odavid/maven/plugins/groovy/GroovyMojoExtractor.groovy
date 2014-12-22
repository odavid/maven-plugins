package com.github.odavid.maven.plugins.groovy;

import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException
import org.apache.maven.plugin.descriptor.MojoDescriptor
import org.apache.maven.plugin.descriptor.Parameter
import org.apache.maven.tools.plugin.PluginToolsRequest
import org.apache.maven.tools.plugin.extractor.ExtractionException
import org.apache.maven.tools.plugin.extractor.MojoDescriptorExtractor
import org.apache.maven.tools.plugin.util.PluginUtils
import org.codehaus.groovy.groovydoc.GroovyClassDoc
import org.codehaus.groovy.groovydoc.GroovyRootDoc
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool
import org.codehaus.plexus.component.annotations.Component
import org.codehaus.plexus.component.annotations.Requirement

@Component(role=MojoDescriptorExtractor.class, hint=GroovyMojoExtractor.ROLE_HINT)
class GroovyMojoExtractor implements MojoDescriptorExtractor {
	static final String ROLE_HINT = "java-annotations-and-groovy";
	
	@Requirement(role=MojoDescriptorExtractor.class, hint="java-annotations")
	MojoDescriptorExtractor javaAnnotationsExtractor;

	@Override
	public List<MojoDescriptor> execute(PluginToolsRequest request) throws ExtractionException, InvalidPluginDescriptorException {
		List<MojoDescriptor> descriptors = javaAnnotationsExtractor.execute(request);
		def compileSourceRoots = request.project.compileSourceRoots
		GroovyDocTool groovyDocTool = new GroovyDocTool(compileSourceRoots as String[]);
		compileSourceRoots.each { root ->
			if(new File(root).exists()){
				groovyDocTool.add (PluginUtils.findSources(root, "**/*.groovy") as List<String>);
			}
		}
		GroovyRootDoc groovyDocRoot = groovyDocTool.getRootDoc();
		def gClasses = [:]
		groovyDocRoot.classes().each{ GroovyClassDoc c ->
			gClasses[c.qualifiedTypeName()] = c
		}
		descriptors.each {MojoDescriptor d ->
			if(d.goal != "help"){
				GroovyClassDoc gClass = gClasses[d.implementation]
				if(gClass){
					def gClassProperties = [:]
					gClass.properties().each {
						gClassProperties[it.name()] = it
					}
					d.description = gClass.commentText()?.trim()
					d.parameters.each {
						if(gClassProperties[it.name]){
							it.description = gClassProperties[it.name].commentText()?.trim()
						}
					}
				}
			}
		} 
		return descriptors;
	}
}
