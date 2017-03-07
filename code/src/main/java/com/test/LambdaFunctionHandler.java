package com.test;
/*
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaFunctionHandler implements RequestHandler<Object, String> {
    @Override
    public String handleRequest(Object request, Context context){
        context.getLogger().log("##################" + request );
        return "Fehlerhaft?";
    }
}*/

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;


public class LambdaFunctionHandler implements RequestStreamHandler {
    JSONParser parser = new JSONParser();


    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

        LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of ProxyWithStream");

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject responseJson = new JSONObject();

        try {
            JSONObject event = (JSONObject)parser.parse(reader);

            // do something

            JSONObject responseBody = new JSONObject();
            responseBody.put("message", "Hello !");

            JSONObject headerJson = new JSONObject();
            //headerJson.put("x-custom-response-header", "my custom response header value");

            responseJson.put("statusCode", "200");
            //responseJson.put("headers", headerJson);
            responseJson.put("body", responseBody.toString());

        } catch(Exception ex) {
            responseJson.put("statusCode", "400");
            responseJson.put("exception", ex);
        }

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toJSONString());
        writer.close();
    }
}