package com.github.odavid.maven.plugins;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/** 
 * This mojo is a fake mojo just for having the mixins configuration available in IDEs such as intelliJ
 * @author odavid
 *
 */
@Mojo(name = "fake-mixin-mojo", threadSafe = true)
public class FakeMixinMojo extends AbstractMojo{
	
	@Parameter
	private List<Mixin> mixins = new ArrayList<Mixin>();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		//DO NOTHING...
	}

}
