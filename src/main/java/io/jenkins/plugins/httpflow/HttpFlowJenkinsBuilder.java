package io.jenkins.plugins.httpflow;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.jenkins.plugins.httpflow.support.ShellCommandExecutor;
import io.jenkins.plugins.httpflow.support.TempFileManager;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

public class HttpFlowJenkinsBuilder extends Builder implements SimpleBuildStep {

    private String contents;

    @DataBoundConstructor
    public HttpFlowJenkinsBuilder(String contents) {
        this.contents = contents;
    }

    public String getContents() {
        return contents;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        TempFileManager tempFileManager = new TempFileManager();
        tempFileManager.checkHttpFlowInstalled();

        tempFileManager.createInstantFiles(run, contents);
        workspace.child("builds." + run.getNumber()).child("current.hfd.txt").write(contents, run.getCharset().name());

        ShellCommandExecutor executor = new ShellCommandExecutor(run.getRootDir(), listener.getLogger(), 1000);
        int exitCode = executor.execute(convertEnvToArgs(env));

        tempFileManager.deleteInstantFiles();
        listener.getLogger().println("END");

        if (exitCode != 0) {
            run.setResult(Result.FAILURE);
            if (executor.getAssertFailMessage() != null) {
                run.setDescription(executor.getAssertFailMessage().toRunDescription());
            }
        }
    }

    private String convertEnvToArgs(EnvVars env) {
        StringBuilder args = new StringBuilder();
        for (String key : env.keySet()) {
            args.append(key).append("=").append(env.get(key)).append(" ");
        }
        return args.toString();
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckContents(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error(Messages.HttpFlowJenkinsBuilder_DescriptorImpl_errors_missingContents());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.HttpFlowJenkinsBuilder_DescriptorImpl_DisplayName();
        }

    }

}
