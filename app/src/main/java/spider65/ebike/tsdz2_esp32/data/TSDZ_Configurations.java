package spider65.ebike.tsdz2_esp32.data;


import android.util.Log;

import java.math.BigDecimal;

import static spider65.ebike.tsdz2_esp32.TSDZConst.CONFIGURATIONS_ADV_SIZE;
import static spider65.ebike.tsdz2_esp32.utils.Utils.intX10toFloat;

public class TSDZ_Configurations {

    private static final String TAG = "TSDZ_Configurations";
    public int ui8_configurations_version;
    public int ui8_assist_level;
    public int ui8_wheel_max_speed;
    public int ui8_units_type;
    public long ui32_wh_x10_offset;
    public long ui32_wh_x10_100_percent;



    public int ui16_wheel_perimeter;

    public boolean setData(byte[] data) {
        if (data.length != CONFIGURATIONS_ADV_SIZE) {
            Log.e(TAG, "setData: wrong data size");
            return false;
        }

        if ((data[0] & 0xFF) == 0xA2) {
            ui8_assist_level = data[1];
            ui16_wheel_perimeter = ((data[3] & 255) << 8) + (data[2] & 255);
            ui8_wheel_max_speed = data[4];
            ui8_units_type = data[5];
            ui32_wh_x10_offset = (data[6] & 255) +
                    ((data[7] & 255) << 8) +
                    ((data[8] & 255) << 16) +
                    ((data[9] & 255) << 32);
            ui32_wh_x10_100_percent = (data[10] & 255) +
                    ((data[11] & 255) << 8) +
                    ((data[12] & 255) << 16) +
                    ((data[13] & 255) << 32);
            return true;
        }

        Log.e(TAG, "setData: wrong configurations version");
        return false;
    }

    public byte[] toByteArray() {
        byte[] data = new byte[CONFIGURATIONS_ADV_SIZE];
        data[0] = (byte) 0xA2; // configurations version
        data[1] = (byte) ui8_assist_level;
        data[2] = (byte) (ui16_wheel_perimeter & 0xff);
        data[3] = (byte) (ui16_wheel_perimeter >>> 8);
        data[4] = (byte) ui8_wheel_max_speed;
        data[5] = (byte) ui8_units_type;
        data[6] = (byte) (ui32_wh_x10_offset & 0xff);
        data[7] = (byte) (ui32_wh_x10_offset >>> 8);
        data[8] = (byte) (ui32_wh_x10_offset >>> 16);
        data[9] = (byte) (ui32_wh_x10_offset >>> 24);
        data[10] = (byte) (ui32_wh_x10_100_percent & 0xff);
        data[11] = (byte) (ui32_wh_x10_100_percent >>> 8);
        data[12] = (byte) (ui32_wh_x10_100_percent >>> 16);
        data[13] = (byte) (ui32_wh_x10_100_percent >>> 24);

        return data;
    }
}
