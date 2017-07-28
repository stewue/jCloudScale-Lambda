package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import jcs_lambda.matrix_multiplication.Calculator;
import jcs_lambda.matrix_multiplication.Matrix;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.*;

public class Endpoint implements RequestStreamHandler {
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        JSONParser parser = new JSONParser();
        Gson gson = new Gson();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            Send send = gson.fromJson( event.toJSONString() , Send.class );

            Matrix response = Calculator.matrixMultiplication( send.a, send.b );

            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write( gson.toJson(response) );
            writer.close();
        }
        catch(Exception ex) {
        }
    }
}