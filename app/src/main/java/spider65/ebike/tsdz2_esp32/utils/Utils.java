package spider65.ebike.tsdz2_esp32.utils;

import android.widget.EditText;

import java.math.BigDecimal;

import spider65.ebike.tsdz2_esp32.MyApp;
import spider65.ebike.tsdz2_esp32.R;

public class Utils {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    public static Integer checkRange(EditText et, int min, int max) {
        int val = Integer.parseInt(et.getText().toString());
        if (val < min || val > max) {
            et.setError(MyApp.getInstance().getString(R.string.range_error, min, max));
            return null;
        }
        return val;
    }

    public static float intX10toFloat (int input){
        double number = new Double(input);
        number = number/10;
        return (float) number;
    }

    public static long longX10toFloat (long input){
        long number = new Long(input);
        number = number/10;
        return number;
    }

    public static float intDiv1000toFloat (int input){
        double number = new Double(input);
        number = number/1000;
        return (float) number;
    }
}
