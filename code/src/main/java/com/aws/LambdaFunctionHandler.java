package com.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;


public class LambdaFunctionHandler implements RequestStreamHandler {
    JSONParser parser = new JSONParser();
    Gson gson = new Gson();

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ResponseDTO responseDTO = new ResponseDTO();

        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            RequestDTO requestDTO = gson.fromJson( event.toJSONString() , RequestDTO.class );



            responseDTO.c = 2 * requestDTO.a;
            responseDTO.d = requestDTO.b + " --- xyz";

            System.out.println( gson.toJson(responseDTO) );

        } catch(Exception ex) {
            System.out.println( ex );
        }

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write( gson.toJson(responseDTO) );
        writer.close();
    }
}