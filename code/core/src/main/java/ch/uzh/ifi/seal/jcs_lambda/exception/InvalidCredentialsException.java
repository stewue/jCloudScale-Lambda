package ch.uzh.ifi.seal.jcs_lambda.exception;

public class InvalidCredentialsException extends RuntimeException
{
    public InvalidCredentialsException()
    {
        super("Your credentials are invalid!");
    }
}