package spider65.ebike.tsdz2_esp32.data;


import android.util.Log;

import java.math.BigDecimal;

import static spider65.ebike.tsdz2_esp32.TSDZConst.CONFIGURATIONS_ADV_SIZE;
import static spider65.ebike.tsdz2_esp32.utils.Utils.intX10toFloat;

public class TSDZ_Config {

    private static final String TAG = "TSDZ_Config";
    public int ui8_assist_level;
    public int ui8_wheel_max_speed;
    public int ui16_wheel_speed_x10;
    public float f_wheel_speed;
    public int ui16_wheel_perimeter;

    public boolean setData(byte[] data) {
        if (data.length != CONFIGURATIONS_ADV_SIZE) {
            Log.e(TAG, "setData: wrong data size");
            return false;
        }

        ui8_assist_level = data[1];
        ui16_wheel_perimeter = ((data[3] & 255) << 8) + (data[2] & 255);
        ui8_wheel_max_speed = data[4];

        return true;
    }

    public byte[] toByteArray() {
        byte[] data = new byte[CONFIGURATIONS_ADV_SIZE];
//        data[0] = (byte)(ui8_wheel_max_speed & 0xff);
        data[1] = (byte)ui16_wheel_perimeter;
        data[2] = (byte)(ui16_wheel_perimeter >>> 8);

        return data;
    }
}
