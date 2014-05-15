package com.github.ohaddavid.maven.plugins.mojo.descriptor;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "clean-generated", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class CleanGeneratedMojo extends AbstractMojo{

	@Parameter(defaultValue="false", property="mojo-descriptor.skip.clean")
	private boolean skipClean;
	
	@Parameter(defaultValue = "${project.build.scriptSourceDirectory}")
	private File scriptsDir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if(skipClean){
			return;
		}
		File[] mojoDescriptors = scriptsDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(MojoDescriptorMojo.MOJO_DESCRIPTOR_EXT);
			}
		});
		for(File mojoDescriptor: mojoDescriptors){
			int indexOfExt = mojoDescriptor.getName().indexOf(MojoDescriptorMojo.MOJO_DESCRIPTOR_EXT);
			String name = mojoDescriptor.getName().substring(0, indexOfExt);
			
			File outputFile = new File(mojoDescriptor.getParentFile(), name + ".mojos.xml");
			if(outputFile.isFile()){
				outputFile.delete();
			}
		}
		
		
	}
	
}
