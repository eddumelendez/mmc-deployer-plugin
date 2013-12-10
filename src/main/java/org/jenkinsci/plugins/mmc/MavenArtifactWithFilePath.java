package org.jenkinsci.plugins.mmc;

import java.io.Serializable;

public class MavenArtifactWithFilePath implements Serializable {

	final String groupId;
	final String artifactId; 
	final String version; 
	final String filePath; 
	final String type;

	public MavenArtifactWithFilePath(String groupId, String artifactId,
			String version, String filePath, String type) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.filePath = filePath;
		this.type = type;
	}

}
