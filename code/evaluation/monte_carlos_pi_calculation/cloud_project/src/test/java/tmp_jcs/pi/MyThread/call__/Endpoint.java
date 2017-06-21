package tmp_jcs.pi.MyThread.call__; 

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.JVMContext; 
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger; 
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.*;
import pi.*; 
import java.lang.reflect.Method; 
import java.lang.reflect.Field; 

public class Endpoint implements RequestStreamHandler {
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        JVMContext.setServerContext(); 
        JSONParser parser = new JSONParser();
        Gson gson = new Gson();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        Response response = null;

        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            Logger.info( "Input: " +  event.toJSONString() );
            Request request = gson.fromJson( event.toJSONString() , Request.class );

            response = invoke( request );
        }
        catch(Exception ex) {
            ex.printStackTrace(); 
            response = new Response( ex.getStackTrace() ); 
        }

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write( gson.toJson(response) );
        writer.close();
        
        Logger.info( "Output: " +  gson.toJson(response) );
    }

    private Response invoke( Request request ) throws Exception {
        JVMContext.setContextId( request._uuid_ ); 
        MyThread object = new MyThread(); 
        Class params[] = request.getClassArray(); 
        Object paramsObj[] = request.getObjectArray(); 

        Method method = object.getClass().getDeclaredMethod("call", params ); 
        method.setAccessible(true); 

        Field field; 

        field = object.getClass().getDeclaredField("numPointsInCircle" ); 
        field.setAccessible(true); 
        field.set( object, request.numPointsInCircle ); 

        field = object.getClass().getDeclaredField("pointsOfThread" ); 
        field.setAccessible(true); 
        field.set( object, request.pointsOfThread ); 

        field = object.getClass().getDeclaredField("quit" ); 
        field.setAccessible(true); 
        field.set( object, request.quit ); 

        field = object.getClass().getDeclaredField("nameThread" ); 
        field.setAccessible(true); 
        field.set( object, request.nameThread ); 

        field = object.getClass().getDeclaredField("sideSquare" ); 
        field.setAccessible(true); 
        field.set( object, request.sideSquare ); 

        return new Response( method.invoke( object, paramsObj ) ); 
    }

}