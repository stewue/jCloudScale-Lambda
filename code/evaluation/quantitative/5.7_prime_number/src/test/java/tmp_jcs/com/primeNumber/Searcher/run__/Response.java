package tmp_jcs.com.primeNumber.Searcher.run__; 

public class Response {
    public long returnValue;
    public StackTraceElement [] exceptionStackTrace; 
    public Response ( Object returnValue ) {
        this.returnValue = (long) returnValue;
    }
    public Response ( StackTraceElement [] exceptionStackTrace ) {
        this.exceptionStackTrace = exceptionStackTrace;
    }
}