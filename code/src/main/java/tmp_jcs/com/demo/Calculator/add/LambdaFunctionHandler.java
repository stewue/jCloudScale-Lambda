package tmp_jcs.com.demo.Calculator.add; 

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.*;

public class LambdaFunctionHandler implements RequestStreamHandler {
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        JSONParser parser = new JSONParser();
        Gson gson = new Gson();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        Response response = null;

        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            Request request = gson.fromJson( event.toJSONString() , Request.class );

            response = new Response( emulateMethod( request.a, request.b ) );
        }
        catch(Exception ex) {
            System.out.println( ex );
        }

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write( gson.toJson(response) );
        writer.close();
    }

    private int emulateMethod( int a, int b ){
        return a + b; 
    }
}