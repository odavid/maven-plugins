package com.github.odavid.maven.plugins;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;

public class MixinModelProblemCollector implements ModelProblemCollector{
	List<ModelProblemCollectorRequest> problems = new ArrayList<>();
    private Set<ModelProblem.Severity> severities = EnumSet.noneOf( ModelProblem.Severity.class );

	@Override
	public void add(ModelProblemCollectorRequest req) {
		problems.add(req);
		severities.add(req.getSeverity());
	}
	
	public void clear(){
		problems.clear();
		severities.clear();
	}
	
	public void checkErrors(File pom) throws MavenExecutionException{
        if(severities.contains( ModelProblem.Severity.ERROR ) || severities.contains( ModelProblem.Severity.FATAL )){
        	PrintWriter out = new PrintWriter(new StringWriter());
        	for(ModelProblemCollectorRequest request: problems){
        		out.printf("Model Problem: %s%n", request.getMessage());
        	}
    		throw new MavenExecutionException( out.toString(), pom);
        }
		
	}

}
