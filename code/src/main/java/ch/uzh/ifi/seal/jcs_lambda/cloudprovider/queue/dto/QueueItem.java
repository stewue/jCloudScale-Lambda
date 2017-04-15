package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.queue.dto;

public class QueueItem {
    public String senderId;
    public String recieverId;
    public QueueType queueType;
    public InvokeType invokeType;
    public String variable;
    public String variableType;
    public String body;
}
