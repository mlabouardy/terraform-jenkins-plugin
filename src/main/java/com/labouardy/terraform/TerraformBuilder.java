package com.labouardy.terraform;
import hudson.AbortException;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import java.io.BufferedReader;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link TerraformBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #templatePath})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class TerraformBuilder extends Builder implements SimpleBuildStep{

    private final String templatePath;
    private final String command;
    private final String envVariables;
    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public TerraformBuilder(String templatePath, String command, String envVariables) {
        this.templatePath = templatePath;
        this.command = command;
        this.envVariables = envVariables;
    }

    /**
     * We'll use this from the {@code config.jelly}.
     * @return Terraform template path in workspace
     */
    
    public String getTemplatePath(){
        return templatePath;
    }
    
    public String getCommand(){
        return command;
    }

    public String getEnvVariables() {
        return envVariables;
    }
    

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        String cmd = getDescriptor().getPath() + " " + getCommand() + " " + getTemplatePath();
        launcher.launch()
                 .pwd(workspace)
                 .envs(getEnvVariables())
                 .cmds(cmd)
                 .stdout(listener)
                 .join();
        /*List<String> envp = new ArrayList<String>();
        if(getEnvVariables() != null && getEnvVariables().trim().length() > 0){
            for(String env:getEnvVariables().split(" "))
                envp.add(env);
        }
        Runtime rt = Runtime.getRuntime();
        try{
           Process pr = rt.exec(cmd, envp.toArray(new String[0]));
           pr.waitFor();
           try(BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));){
                String line = null; 
                while ((line = input.readLine()) != null)
                    listener.getLogger().println(line);
            }catch(Exception e){
                listener.error(e.getMessage());
            }
            
            try(BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));){
                String line = null; 
                while ((line = error.readLine()) != null)
                    listener.getLogger().println(line);
            }catch(Exception e){
                throw new AbortException(e.getMessage());
            }
        }catch(Exception e){
            throw new AbortException(e.getMessage());
        }
        */
        listener.getLogger().println("OK:" + cmd);
    }

    /**
     * Descriptor for {@link TerraformBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly}
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use {@code transient}.
         */
        private String path;

        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         *      <p>
         *      Note that returning {@link FormValidation#error(String)} does not
         *      prevent the form from being saved. It just means that a message
         *      will be displayed to the user. 
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }
        
        public FormValidation doCheckPath(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a terraform binary path");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
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
            // To persist global configuration information,
            // set that to properties and call save().
            path = formData.getString("path");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         * @return terraform binary path
         */
        public String getPath() {
            return path;
        }
    }
}

