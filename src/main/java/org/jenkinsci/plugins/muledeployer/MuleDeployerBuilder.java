package org.jenkinsci.plugins.muledeployer;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.Secret;

import java.io.IOException;

public class MuleDeployerBuilder extends Builder {

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {

		final String mmcUrl = getDescriptor().getMmcUrl();
		final String mmcUser = getDescriptor().getMmcUser();
		final Secret mmcPassword = getDescriptor().getMmcPassword();

		if (!validatePluginConfiguration(mmcUrl, mmcUser,
				Secret.toString(mmcPassword))) {
			listener.getLogger().println(
					"Please configure Nexus Metadata Plugin");
			return false;
		}

		return super.perform(build, launcher, listener);
	}

	private boolean validatePluginConfiguration(final String url,
			final String user, final String password) {

		if (url == null || user == null || password == null || url.isEmpty()
				|| user.isEmpty() || password.isEmpty()) {
			return false;
		}
		
		return true;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

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

	}

}
