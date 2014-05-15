package com.github.ohaddavid.maven.plugins.mojo.descriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MojoDescriptor {
	private String name;
	private String call;
	private String phase;
	private String requiresDependencyResolution;
	private String baseDescriptor;
	private boolean isAbstract;
	private boolean requiresProject;
	private String description;

	public boolean isRequiresProject() {
		return requiresProject;
	}
	public void setRequiresProject(boolean requiresProject) {
		this.requiresProject = requiresProject;
	}
	private Map<String,Parameter> paramters = new HashMap<>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCall() {
		return call;
	}
	public void setCall(String call) {
		this.call = call;
	}
	public String getPhase() {
		return phase;
	}
	public void setPhase(String phase) {
		this.phase = phase;
	}
	public String getRequiresDependencyResolution() {
		return requiresDependencyResolution;
	}
	public void setRequiresDependencyResolution(String requiresDependencyResolution) {
		this.requiresDependencyResolution = requiresDependencyResolution;
	}
	public String getBaseDescriptor() {
		return baseDescriptor;
	}
	public void setBaseDescriptor(String baseDescriptor) {
		this.baseDescriptor = baseDescriptor;
	}
	public boolean isAbstract() {
		return isAbstract;
	}
	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public void addParam(Parameter param){
		if(!paramExist(param.getName())){
			paramters.put(param.getName(), param);
		}else{
			throw new IllegalArgumentException("param " + param.getName() + " already exist in mojo " + getName());
		}

	}
	public boolean paramExist(String paramName){
		return paramters.containsKey(paramName);
	}
	public Collection<Parameter> params(){
		return paramters.values();
	}
	public Set<String> paramKeys(){
		return paramters.keySet();
	}
}
