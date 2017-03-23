package ch.uzh.ifi.seal.jcs_lambda.utility.builder;

import ch.uzh.ifi.seal.jcs_lambda.configuration.JcsConfiguration;
import ch.uzh.ifi.seal.jcs_lambda.exception.MavenBuildException;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class JarBuilder {
    /**
     * Builds the maven project using maven sdk for java
     */
    public static void mvnBuild() {

        // get absolute project path (folder where pom.xml is)
        Path currentRelativePath = Paths.get("");
        String absolutePath = currentRelativePath.toAbsolutePath().toString();

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(absolutePath));
        request.setGoals(Arrays.asList("clean", "install"));

        Invoker invoker = new DefaultInvoker();
        try {
            if ( invoker.getMavenHome() == null ){
                invoker.setMavenHome(new File( JcsConfiguration.MAVEN_HOME ));
            }

            InvocationResult result = invoker.execute(request);

            // error during building process
            if (result.getExitCode() != 0){
                throw result.getExecutionException();
            }
        } catch (Exception e) {
            e.printStackTrace();

            throw new MavenBuildException();
        }
    }
}
