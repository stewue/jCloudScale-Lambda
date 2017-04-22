package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.dto;

public class QueueItem {
    public String senderId;
    public String receiverId;
    public QueueType queueType;
    public InvokeType invokeType;
    public String variable;
    public String variableType;
    public String body;

    public String toString(){
        return "senderId: " + senderId + ", queueType: " + queueType + ", invokeType: " + invokeType + ", variable: " + variable + ", body: " + body;
    }
}
