package org.jenkinsci.plugins.mmc;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Action;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArtifactFilePathSaveAction implements AggregatableAction,
		MavenAggregatedReport, Serializable {
	
	final Set<MavenArtifactWithFilePath> mavenArtifactWithFilePaths;

    ArtifactFilePathSaveAction(Set<MavenArtifactWithFilePath> mavenArtifactWithFilePaths) {
        this.mavenArtifactWithFilePaths = mavenArtifactWithFilePaths;
    }

	public String getDisplayName() {
		return null;
	}

	public String getIconFileName() {
		return null;
	}

	public String getUrlName() {
		return null;
	}

	public Class<? extends AggregatableAction> getIndividualActionType() {
		return null;
	}

	public Action getProjectAction(MavenModuleSet arg0) {
		return null;
	}

	public void update(Map<MavenModule, List<MavenBuild>> arg0, MavenBuild arg1) {
		System.out.println("update");
	}

	public MavenAggregatedReport createAggregatedAction(
			MavenModuleSetBuild arg0, Map<MavenModule, List<MavenBuild>> arg1) {
		return this;
	}

}
