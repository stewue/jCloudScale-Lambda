package com.test;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

class Request2 {
    public String a;
    public String b;
}

class Response2 {
    public String c;
}

public class LambdaFunctionHandler2 implements RequestHandler<Request2, Response2> {
    @Override
    public Response2 handleRequest(Request2 request, Context context){
        // do something

        Response2 response = new Response2();
        response.c = "xyz:::" + request.b;

        return response;
    }
}