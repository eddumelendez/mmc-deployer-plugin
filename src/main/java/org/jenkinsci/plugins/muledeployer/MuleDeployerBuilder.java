package org.jenkinsci.plugins.muledeployer;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.util.HttpURLConnection;
import org.bouncycastle.util.encoders.Base64;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

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

		@Override
		public boolean configure(StaplerRequest req, JSONObject json)
				throws hudson.model.Descriptor.FormException {

			mmcUrl = json.getString("mmcUrl");
			mmcUser = json.getString("mmcUser");
			mmcPassword = Secret.fromString(json.getString("mmcPassword"));

			save();

			return super.configure(req, json);
		}

		public FormValidation doTestConnection(@QueryParameter final String mmcUrl,
				@QueryParameter final String mmcUser,
				@QueryParameter final String mmcPassword) throws IOException,
				ServletException {
			
			URL url = new URL(mmcUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			String credentials = mmcUser + ":" + mmcPassword;
			String encodedCredentials = new String(Base64.encode(credentials.getBytes()));
			connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
			connection.connect();
			try {
				if (connection.getResponseCode() == 200) {
					return FormValidation.ok("Success. Connection with MMC verified");
				}
				return FormValidation.error("Failed. Please check the configuration. HTTP Status: " + connection);
			} catch (Exception e) {
				return FormValidation.error("Client error: " + e.getMessage());
			}
		}

	}

}
