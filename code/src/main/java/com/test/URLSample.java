package com.test;

import com.aws.RequestDTO;
import com.aws.ResponseDTO;
import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class URLSample {

    static Gson gson = new Gson();

    public static void main(String[] args) throws Exception {

        RequestDTO requestDTO = new RequestDTO();
        requestDTO.a = 7;
        requestDTO.b = "stefan";

        String url = "https://wcfhgyglqg.execute-api.eu-central-1.amazonaws.com/prod/TEST";

        ResponseDTO responseDTO = gson.fromJson( doRequest( url,  gson.toJson(requestDTO) ), ResponseDTO.class );

        System.out.println( responseDTO.c );
        System.out.println( responseDTO.d );
    }

    private static String doRequest ( String url, String urlParameters ){
        try {
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

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
        catch ( MalformedURLException e ){}
        catch ( IOException e ){}

        return null;
    }
}