package ch.uzh.ifi.seal.jcs_lambda.exception;

public class MavenBuildException extends RuntimeException
{
    public MavenBuildException()
    {
        super("Error during maven install process. Please check the maven log.");
    }
}
