package tmp_jcs.pi.MyThread.call__; 

public class Response {
    public java.lang.Long returnValue;
    public StackTraceElement [] exceptionStackTrace; 
    public Response ( Object returnValue ) {
        this.returnValue = (java.lang.Long) returnValue;
    }
    public Response ( StackTraceElement [] exceptionStackTrace ) {
        this.exceptionStackTrace = exceptionStackTrace;
    }
}