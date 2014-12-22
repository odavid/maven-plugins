package com.github.odavid.maven.plugins.groovy;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.maven.tools.plugin.extractor.MojoDescriptorExtractor;
import org.apache.maven.tools.plugin.scanner.DefaultMojoScanner;
import org.apache.maven.tools.plugin.scanner.MojoScanner;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Removing java-annotations and use only java-annotations-and-groovy
 * @author odavid
 *
 */
@Component(role=MojoScanner.class)
class GroovyMojoScanner extends DefaultMojoScanner{

	GroovyMojoScanner(){
		super();
	}
	
	@Inject
	GroovyMojoScanner(Map<String, MojoDescriptorExtractor> extractors) {
		super(extractors);
	}

	@Override
	protected Set<String> getActiveExtractors() {
		Set<String> active = super.getActiveExtractors();
		active.remove("java-annotations");
		return active;
	}
}
