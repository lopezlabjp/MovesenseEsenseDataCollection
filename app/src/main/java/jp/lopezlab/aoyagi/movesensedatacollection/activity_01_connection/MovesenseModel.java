package jp.lopezlab.aoyagi.movesensedatacollection.activity_01_connection;

public class MovesenseModel {

    private final String serial;
    private final String address;

    public MovesenseModel(String serial, String address) {
        this.serial = serial;
        this.address = address;
    }

    public String getSerial() {
        return serial;
    }

    public String getAddress() {
        return address;
    }
}
