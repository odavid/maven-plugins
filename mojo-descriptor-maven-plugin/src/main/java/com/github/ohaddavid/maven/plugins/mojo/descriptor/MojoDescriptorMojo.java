package com.github.ohaddavid.maven.plugins.mojo.descriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.maven.artifact.repository.ArtifactRepository;
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


@Mojo(name = "mojo-descriptor", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class MojoDescriptorMojo extends AbstractMojo {
	public static final String MOJO_DESCRIPTOR_EXT = ".mojo-desc.xml";

	@Component
	private MavenProject mavenProject;

	@Component
	private MavenProjectHelper projectHelper;

	@Parameter(defaultValue = "${localRepository}")
	private ArtifactRepository localRepository;

	@Parameter(defaultValue = "${project.build.scriptSourceDirectory}")
	private File scriptsDir;

	public void execute() throws MojoExecutionException {

		File[] mojoDescriptors = scriptsDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(MOJO_DESCRIPTOR_EXT);
			}
		});
		
		for(File mojoDescriptor: mojoDescriptors){
			try {
				transform(mojoDescriptor);
			} catch (Exception e) {
				throw new MojoExecutionException("Could not transform: " + mojoDescriptor + ", " + e.getMessage(), e);
			}
		}
	}

	private void transform(File mojoDescriptor) throws Exception{
		int indexOfExt = mojoDescriptor.getName().indexOf(MOJO_DESCRIPTOR_EXT);
		String name = mojoDescriptor.getName().substring(0, indexOfExt);
		
		File outputFile = new File(mojoDescriptor.getParentFile(), name + ".mojos.xml");
		transform(mojoDescriptor, outputFile);
	}

	private void transform(File mojoDescriptor, File outputFile) throws Exception {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.parse(mojoDescriptor);
		Collection<MojoDescriptor> descriptors = MojoDescriptorsBuilder.getInstance().parseAll(document);
		FileOutputStream fos = new FileOutputStream(outputFile);
		try{
			MojoDescriptorsTransformer.getInstance().transform(descriptors, fos);
		}finally{
			try{
				fos.close();
			}catch(Throwable e){
			}
		}
		
		
	}
}
