package jcs_lambda.matrix_multiplication;

import com.google.gson.Gson;
import lambda.Send;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class Calculator
{
    public static void main ( String [] args ) throws Exception {
        int m = 1000;
        int n = 1000;
        int o = 1000;

        for(int i=0; i<5; i++) {
            Matrix matrixA = new Matrix(m, n);
            matrixA.setRandomValues();
            //matrixA.print();

            Matrix matrixB = new Matrix(n, o);
            matrixB.setRandomValues();
            //matrixB.print();

            long startTime = System.currentTimeMillis();

            Gson gson = new Gson();

            String url = "https://7b7atpud79.execute-api.eu-central-1.amazonaws.com/prod/jcs-lambda-1a59c8835138459ff2c1f5175605e643da5bdddf";
            Send send = new Send();
            send.a = matrixA;
            send.b = matrixB;

            String result = doRequest(url, gson.toJson(send));
            Matrix matrixResult = gson.fromJson(result, Matrix.class);

            System.out.println((System.currentTimeMillis() - startTime) );

            //matrixResult.print();
        }
    }

    public static String doRequest ( String url, String urlParameters ) throws Exception {
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");

        // set timeout to timeout from aws
        con.setConnectTimeout( 5 * 60 * 1000 );

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream( con.getOutputStream() );
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

            return response.toString();
    }

    public static Matrix matrixMultiplication( Matrix a, Matrix b ){
        int aY = a.getY();
        int aX = a.getX();
        int bY = b.getY();
        int bX = b.getX();

        if( aX != bY ){
            throw new IllegalArgumentException();
        }

        Matrix result = new Matrix( aY, bX );

        for( int y=0; y<aY; y++ ){
            for( int x=0; x<bX; x++ ){
                int sum = 0;

                for( int c = 0; c<aX; c++ ){
                    sum += a.getElement(y, c) * b.getElement(c, x);
                }

                result.setElementValue( y, x, sum );
            }
        }

        return result;
    }
}
