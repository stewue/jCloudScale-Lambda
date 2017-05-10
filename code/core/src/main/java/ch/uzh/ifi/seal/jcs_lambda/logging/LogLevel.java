package ch.uzh.ifi.seal.jcs_lambda.logging;

public enum LogLevel {
    OFF(-1),
    FATAL(0),
    ERROR(1),
    WARN(2),
    INFO(3),
    DEBUG(4);

    private final int level;

    LogLevel(int level) {
        this.level = level;
    }

    public int getLevel(){
        return level;
    }
}
