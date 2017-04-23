package ch.uzh.ifi.seal.jcs_lambda.logging;

public class LogMessage {
    public static String red ( String message ){
        return (char)27 + "[31m" + message;
    }

    public static String white ( String message ){
        return (char)27 + "[37m" + message;
    }

    public static boolean hasColor( String message ){
        return message.charAt(0) == (char) 27;
    }
}
