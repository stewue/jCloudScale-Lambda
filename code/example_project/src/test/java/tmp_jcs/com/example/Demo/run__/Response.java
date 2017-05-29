package tmp_jcs.com.example.Demo.run__; 

public class Response {
    public java.lang.String returnValue;
    public StackTraceElement [] exceptionStackTrace; 
    public Response ( Object returnValue ) {
        this.returnValue = (java.lang.String) returnValue;
    }
    public Response ( StackTraceElement [] exceptionStackTrace ) {
        this.exceptionStackTrace = exceptionStackTrace;
    }
}