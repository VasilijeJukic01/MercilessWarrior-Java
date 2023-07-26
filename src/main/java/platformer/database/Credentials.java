package platformer.database;

public enum Credentials {

    MYSQL_IP("52.29.239.198"),
    MYSQL_DATABASE("sql7635176"),
    MYSQL_USERNAME("sql7635176"),
    MYSQL_PASSWORD("S4Iz7nuhYZ");

    private final String value;

    Credentials(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
