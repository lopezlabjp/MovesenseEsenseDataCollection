package jp.lopezlab.aoyagi.movesensedatacollection.activity_03_recording;

public class AccDataModel {

    public final float x;
    public final float y;
    public final float z;
    public final float norm;
    public final long sensorTime;
    public final long systemTime;

    public AccDataModel(float x, float y, float z, long sensorTime, long systemTime) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.norm = (float) (Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        this.sensorTime = sensorTime;
        this.systemTime = systemTime;
    }
}
