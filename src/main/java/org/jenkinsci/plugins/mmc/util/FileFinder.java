package org.jenkinsci.plugins.mmc.util;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

public class FileFinder implements FilePath.FileCallable<List<String>> {

	private final String pattern;

	public FileFinder(final String pattern) {
		this.pattern = pattern;
	}

	public List<String> invoke(File workspace, VirtualChannel channel)
			throws IOException, InterruptedException {
		return find(workspace);
	}

	private List<String> find(final File workspace) {
		try {
			FileSet fileSet = new FileSet();
			Project antProject = new Project();
			fileSet.setProject(antProject);
			fileSet.setDir(workspace);
			fileSet.setIncludes(pattern);

			String[] files = fileSet.getDirectoryScanner(antProject)
					.getIncludedFiles();
			return files == null ? Collections.<String> emptyList() : Arrays
					.asList(files);
		} catch (BuildException exception) {
			return Collections.emptyList();
		}
	}

}
