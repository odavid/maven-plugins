package org.apache.maven.ant.tasks;

import java.util.Properties;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class ImportPropertiesParam extends AbstractMavenAntTask{

	private String propertyParam;
	
	private boolean useParamAsPrefix = false;
	
	private String prefix = "";
	
	public String getPropertyParam() {
		return propertyParam;
	}

	public void setPropertyParam(String propertyParam) {
		this.propertyParam = propertyParam;
	}

	public boolean isUseParamAsPrefix() {
		return useParamAsPrefix;
	}
 
	public void setUseParamAsPrefix(boolean useParamAsPrefix) {
		this.useParamAsPrefix = useParamAsPrefix;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void execute() throws BuildException {
		if(StringUtils.isEmpty(propertyParam)){
			throw new BuildException("propertyParam is required");
		}
		
		MojoExecution mojoExecution = getCurrentMojoExecution();
		if(mojoExecution == null){
			throw new BuildException("Could not find mojoExecution, seems not in ant based mojo context");
		}
		Parameter param = (Parameter)mojoExecution.getMojoDescriptor().getParameterMap().get(propertyParam);
		if(param == null || !Properties.class.getName().equals(param.getType())){
			log("param: " + propertyParam + " is either null or not configured as paramater with type java.util.Properties", Project.MSG_WARN);
		}else{
			Xpp3Dom propertiesBean = mojoExecution.getConfiguration().getChild(propertyParam);
			if(propertiesBean == null){
				log("Property " + propertyParam + " is null", Project.MSG_DEBUG);
			}else{
				for (Xpp3Dom child : propertiesBean.getChildren()) {
					String key = child.getName();
					String value = child.getValue();
					String newKey = useParamAsPrefix ? propertyParam + '.' + key : prefix + key;
					getProject().setProperty(newKey, value);
				}
			}
		}
	}
}
