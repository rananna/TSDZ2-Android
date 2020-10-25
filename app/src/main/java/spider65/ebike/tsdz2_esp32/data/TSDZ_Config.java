package spider65.ebike.tsdz2_esp32.data;


import android.util.Log;

public class TSDZ_Config {

    private static final String TAG = "TSDZ_Config";
    private static final int CFG_SIZE = 3;
    public int ui8_wheel_max_speed;
    public int ui16_wheel_perimeter;

    public boolean setData(byte[] data) {
        if (data.length != CFG_SIZE) {
            Log.e(TAG, "setData: wrong data size");
            return false;
        }

        ui8_wheel_max_speed = (data[0] & 255);
        ui16_wheel_perimeter = ((data[2] & 255) << 8) + (data[1] & 255);

        return true;
    }

    public byte[] toByteArray() {
        byte[] data = new byte[CFG_SIZE];
        data[0] = (byte)(ui8_wheel_max_speed & 0xff);
        data[1] = (byte)ui16_wheel_perimeter;
        data[2] = (byte)(ui16_wheel_perimeter >>> 8);

        return data;
    }
}
