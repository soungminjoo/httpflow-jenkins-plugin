package io.jenkins.plugins.httpflow.support;

import org.apache.commons.io.IOUtils;

import java.io.*;

public class HttpFlowJenkinsUtils {

    public static void writeFile(File file, InputStream is) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            IOUtils.copy(is, fos);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

}
