package io.jenkins.plugins.httpflow.support;

import hudson.EnvVars;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ShellCommandExecutor {

    public static final String SHELL_FILE_NAME = "execute-0.0.1";
    public static final String HTTPFLOW_ASSERT_MESSAGE_PREFIX = "Caused by: com.github.httpflowlabs.httpflow.core.context.exception.WrappedAssertionFailedError:";

    private File workDirectory;
    private PrintStream logger;
    private long sleep;

    private AssertFailMessage assertFailMessage;

    public ShellCommandExecutor(File workDirectory, PrintStream logger) {
        this(workDirectory, logger, 0);
    }

    public ShellCommandExecutor(File workDirectory, PrintStream logger, long sleep) {
        this.workDirectory = workDirectory;
        this.logger = logger;
        this.sleep = sleep;
    }

    public int execute(String args) throws IOException, InterruptedException {
        if (sleep > 0) {
            Thread.sleep(sleep);
        }

        ProcessBuilder builder = new ProcessBuilder();
        setCommand(builder, args);
        builder.directory(workDirectory);

        Process process = builder.start();
        startStandardIoStreamPrint(process.getInputStream());
        startStandardIoStreamPrint(process.getErrorStream());

        int exitCode = process.waitFor();
        logger.println("httpflow process finished. (exitCode : " + exitCode + ")");
        return exitCode;
    }

    private void startStandardIoStreamPrint(InputStream inputStream) {
        Executors.newSingleThreadExecutor().submit(() -> {
            Stream<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines();
            lines.forEach(line -> {
                if (assertFailMessage == null && line.startsWith(HTTPFLOW_ASSERT_MESSAGE_PREFIX)) {
                    assertFailMessage = new AssertFailMessage(line.substring(HTTPFLOW_ASSERT_MESSAGE_PREFIX.length()));
                }
                logger.println(line);
            });
        });
    }

    private void setCommand(ProcessBuilder builder, String args) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (isWindows) {
            builder.command("cmd.exe", SHELL_FILE_NAME + ".bat", args.toString());
        } else {
            builder.command("sh", SHELL_FILE_NAME + ".sh", args.toString());
        }
    }

    public AssertFailMessage getAssertFailMessage() {
        return assertFailMessage;
    }
}
