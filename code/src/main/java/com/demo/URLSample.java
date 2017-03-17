package com.demo;

import com.aws.Request;
import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

public class URLSample {

    static Gson gson = new Gson();

    public static void main(String[] args) throws Exception {

        Request request = new Request();
        request.a = 7;
        request.b = "stefan";

        String url = "https://wcfhgyglqg.execute-api.eu-central-1.amazonaws.com/prod/TEST";

        //Response response = gson.fromJson( doRequest( url,  gson.toJson(request) ), Response.class );
    }

    private static String doRequest ( String url, String urlParameters ){
        try {
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream( con.getOutputStream() );
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            // int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        }
        catch ( Exception e ){}

        return null;
    }
}