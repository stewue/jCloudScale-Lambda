import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;

public class Main {
    @StartUp( deployToCloud = true )
    public static void main ( String [] args ){
        System.out.println("asd");
    }
}
