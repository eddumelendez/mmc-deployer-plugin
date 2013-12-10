package org.jenkinsci.plugins.mmc;

import hudson.Extension;
import hudson.Launcher;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.bouncycastle.util.encoders.Base64;
import org.jenkinsci.plugins.mmc.util.FileFinder;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class MuleDeployerBuilder extends Builder {

	public final String filePattern;

	public MuleDeployerBuilder(String filePattern) {
		this.filePattern = filePattern;
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {

		final String mmcUrl = getDescriptor().getMmcUrl();
		final String mmcUser = getDescriptor().getMmcUser();
		final Secret mmcPassword = getDescriptor().getMmcPassword();

		if (!validatePluginConfiguration(mmcUrl, mmcUser,
				Secret.toString(mmcPassword))) {
			listener.getLogger()
					.println("Please configure MMC Metadata Plugin");
			return false;
		}
		
		List<ArtifactFilePathSaveAction> artifactFilePathSaveActions = retrieveArtifactFilePathSaveActions(build);

        if (artifactFilePathSaveActions.isEmpty() && StringUtils.isBlank(filePattern)) {
			return true;
		}

		String warPath = null;
		Set<String> candidates = new HashSet<String>();
		for (ArtifactFilePathSaveAction artifactFilePathSaveAction : artifactFilePathSaveActions) {
            for (MavenArtifactWithFilePath artifactWithFilePath : artifactFilePathSaveAction.mavenArtifactWithFilePaths) {
                if (StringUtils.equals("zip", artifactWithFilePath.type)) {
                    candidates.add(artifactWithFilePath.filePath);
                }
            }
        }
		
		if (candidates.size() > 1) {
            if (StringUtils.isBlank(filePattern)) {
                //listener.error(Messages.CloudbeesDeployer_AmbiguousMavenWarArtifact());
                return false;
            }
            Iterator<String> it= candidates.iterator();
            while (it.hasNext()) {
                if (SelectorUtils.match(filePattern, it.next())) continue;
                it.remove();
            }

            if (candidates.size() > 1) {
                //listener.error(Messages.CloudbeesDeployer_StillAmbiguousMavenWarArtifact());
                return false;
            }

            if (candidates.size() == 0) {
                //listener.error(Messages.CloudbeesDeployer_NoWarArtifactToMatchPattern());
                return false;
            }
        }

        if (candidates.size() == 1) {
            warPath = candidates.iterator().next();
        }

        if (StringUtils.isBlank(warPath)) {
            if (StringUtils.isBlank(filePattern)) {
                //listener.error(Messages.CloudbeesPublisher_noWarArtifacts());
                return false;
            } else {
                //search file in the workspace with the pattern
                FileFinder fileFinder = new FileFinder(filePattern);
                List<String> fileNames = build.getWorkspace().act(fileFinder);
                listener.getLogger().println("found remote files : " + fileNames);
                if (fileNames.size() > 1) {
                    //listener.error(Messages.CloudbeesPublisher_ToManyFilesMatchingPattern());
                    return false;
                } else if (fileNames.size() == 0) {
                    //listener.error(Messages.CloudbeesPublisher_noArtifactsFound(filePattern));
                    return false;
                }
                // so we use only the first found
                warPath = fileNames.get(0);
            }
        }

        //listener.getLogger().println(Messages.CloudbeesPublisher_WarPathFound(warPath));
        doDeploy(build, listener, warPath);

		return true;
	}

	private void doDeploy(AbstractBuild build, BuildListener listener,
			String warPath) {
		
	}

	private boolean validatePluginConfiguration(final String url,
			final String user, final String password) {

		if (url == null || user == null || password == null || url.isEmpty()
				|| user.isEmpty() || password.isEmpty()) {
			return false;
		}

		return true;
	}
	
	private List<ArtifactFilePathSaveAction> retrieveArtifactFilePathSaveActions(AbstractBuild<?, ?> build) {
        List<ArtifactFilePathSaveAction> artifactFilePathSaveActions = new ArrayList<ArtifactFilePathSaveAction>();
        List<ArtifactFilePathSaveAction> actions = build.getActions(ArtifactFilePathSaveAction.class);
        if (actions != null) artifactFilePathSaveActions.addAll(actions);

        if (build instanceof MavenModuleSetBuild) {
            for (List<MavenBuild> mavenBuilds : ((MavenModuleSetBuild) build).getModuleBuilds().values()) {
                for (MavenBuild mavenBuild : mavenBuilds) {
                    actions = mavenBuild.getActions(ArtifactFilePathSaveAction.class);
                    if (actions != null) artifactFilePathSaveActions.addAll(actions);
                }
            }
        }
        return artifactFilePathSaveActions;
    }

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Builder> {

		private String mmcUrl;

		private String mmcUser;

		private Secret mmcPassword;

		public String getMmcUrl() {
			return mmcUrl;
		}

		public void setMmcUrl(String mmcUrl) {
			this.mmcUrl = mmcUrl;
		}

		public String getMmcUser() {
			return mmcUser;
		}

		public void setMmcUser(String mmcUser) {
			this.mmcUser = mmcUser;
		}

		public Secret getMmcPassword() {
			return mmcPassword;
		}

		public void setMmcPassword(Secret mmcPassword) {
			this.mmcPassword = mmcPassword;
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> arg0) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json)
				throws hudson.model.Descriptor.FormException {

			mmcUrl = json.getString("mmcUrl");
			mmcUser = json.getString("mmcUser");
			mmcPassword = Secret.fromString(json.getString("mmcPassword"));

			save();

			return super.configure(req, json);
		}

		public FormValidation doTestConnection(
				@QueryParameter final String mmcUrl,
				@QueryParameter final String mmcUser,
				@QueryParameter final String mmcPassword) throws IOException,
				ServletException {

			URL url = new URL(mmcUrl);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			String credentials = mmcUser + ":" + mmcPassword;
			String encodedCredentials = new String(Base64.encode(credentials
					.getBytes()));
			connection.setRequestProperty("Authorization", "Basic "
					+ encodedCredentials);
			connection.connect();
			try {
				if (connection.getResponseCode() == 200) {
					return FormValidation
							.ok("Success. Connection with MMC verified");
				}
				return FormValidation
						.error("Failed. Please check the configuration. HTTP Status: "
								+ connection);
			} catch (Exception e) {
				return FormValidation.error("Client error: " + e.getMessage());
			}
		}

	}

}
