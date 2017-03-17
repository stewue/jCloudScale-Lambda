package ch.uzh.ifi.seal.jcs_lambda.utility;

import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Arrays;

public class JarBuilder {
    /**
     * Builds the maven project using maven sdk for java
     */
    public static void mvnBuild() {
        // TODO
        String path = "E:\\OneDrive\\Uni\\17_FS\\Bachelorarbeit\\jcs_lambda\\code";

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(path));
        request.setGoals(Arrays.asList("clean", "install"));

        Invoker invoker = new DefaultInvoker();
        try {
            //TODO not hard-coded
            if (invoker.getMavenHome() == null){
                invoker.setMavenHome(new File("C:\\Program Files\\apache-maven-3.3.9"));
            }

            InvocationResult result = invoker.execute(request);

            if (result.getExitCode() == 0){

            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }
}
