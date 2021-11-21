package io.jenkins.plugins.httpflow.support;

import hudson.model.Run;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class TempFileManager {

    public static final String JAR_FILE_NAME = "httpflow-console-0.0.1.jar";
    public static final File HTTPFLOW_HOME = new File(System.getProperty("user.home"), ".httpflow-jenkins");

    private File currentHfdFile;
    private File shellFile;

    public void checkHttpFlowInstalled() {
        File file = new File(HTTPFLOW_HOME, JAR_FILE_NAME);
        if (!file.exists()) {
            if (!HTTPFLOW_HOME.exists()) {
                HTTPFLOW_HOME.mkdirs();
            }
            HttpFlowJenkinsUtils.writeFile(file, this.getClass().getResourceAsStream("/" + JAR_FILE_NAME));
        }
    }

    public void createInstantFiles(Run<?, ?> run, String content) throws IOException {
        createHfdFile(run, content);
        createStartShellFile(run.getRootDir());
    }

    private void createHfdFile(Run<?, ?> run, String content) throws IOException {
        this.currentHfdFile = new File(run.getRootDir(), "current.hfd");
        FileUtils.write(currentHfdFile, content, run.getCharset());
    }

    private void createStartShellFile(File rootDir) {
        String ext = (isWindowsOs())? ".bat" : ".sh";

        this.shellFile = new File(rootDir, ShellCommandExecutor.SHELL_FILE_NAME + ext);
        if (!shellFile.exists()) {
            HttpFlowJenkinsUtils.writeFile(shellFile, getShellFileContents());
        }
    }

    private ByteArrayInputStream getShellFileContents() {
        File httpflowJarFile = new File(HTTPFLOW_HOME, JAR_FILE_NAME);
        String args = (isWindowsOs())? "%*" : "$@";

        return new ByteArrayInputStream(("java -jar " + httpflowJarFile.getAbsolutePath() + " current.hfd " + args).getBytes());
    }

    public void deleteInstantFiles() {
        currentHfdFile.delete();
        shellFile.delete();
    }

    private boolean isWindowsOs() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

}
