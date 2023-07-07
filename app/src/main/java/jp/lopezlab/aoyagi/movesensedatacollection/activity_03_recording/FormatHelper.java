package jp.lopezlab.aoyagi.movesensedatacollection.activity_03_recording;

public class FormatHelper {
    private FormatHelper() {}

    public static String formatContractToJson(String serial, String uri) {
        StringBuilder sb = new StringBuilder();
        return sb.append("{\"Uri\": \"").append(serial).append("/").append(uri).append("\"}").toString();
    }

}
