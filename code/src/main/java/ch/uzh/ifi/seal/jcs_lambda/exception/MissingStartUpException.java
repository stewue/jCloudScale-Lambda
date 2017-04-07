package ch.uzh.ifi.seal.jcs_lambda.exception;

public class MissingStartUpException extends RuntimeException
{
    public MissingStartUpException()
    {
        super( "No Start point set with @startUp annotation" );
    }
}