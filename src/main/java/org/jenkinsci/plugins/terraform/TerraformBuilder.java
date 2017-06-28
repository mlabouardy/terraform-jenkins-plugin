package org.jenkinsci.plugins.terraform;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.Descriptor.FormException;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link TerraformBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #command})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked. 
 *
 * @author Mohamed Labouardy
 * @version 1.0.0
 */
public class TerraformBuilder extends Builder implements SimpleBuildStep {
    private final String command;
   // private final String path;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public TerraformBuilder(final String command) {
        this.command = command;
    }

    /**
     * We'll use this from the {@code config.jelly}.
     * @return form fields
     */
    public String getCommand() {
		return command;
	}
    
   /* public String getPath() {
		return path;
	}*/

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        listener.getLogger().println("execute, "+command+"!");
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }
    
    /**
     * Descriptor for {@link TerraformBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly}
     * for the actual HTML fragment for the configuration screen.
     * 
     * @author Mohamed Labouardy
     * @version 1.0.0
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        private String terraformPath;

        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Infrastructure as Code";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        	terraformPath = formData.getString("path");
            save();
            return super.configure(req,formData);
        }

        public String getTerraformPath() {
            return terraformPath;
        }
    }
}

