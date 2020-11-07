package spider65.ebike.tsdz2_esp32.data;

import android.util.Log;

import static spider65.ebike.tsdz2_esp32.TSDZConst.PERIODIC_ADV_SIZE;

public class TSDZ_Status {

    private static final String TAG = "TSDZ_Status";

    public int assistLevel;
    public float speed;
    public int cadence;
    public float motorTemperature;
    public int pPower;
    public float volts;
    public float amperes;
    public int status;
    public boolean brake;
    public int wattHour;
    public boolean streetMode;

    /*
    #pragma pack(1)
    typedef struct _tsdz_status
    {
      volatile uint8_t ui8_riding_mode;
      volatile uint8_t ui8_assist_level;
      volatile uint16_t ui16_wheel_speed_x10;
      volatile uint8_t ui8_pedal_cadence_RPM;
      volatile uint16_t ui16_motor_temperaturex10;
      volatile uint16_t ui16_pedal_power_x10;
      volatile uint16_t ui16_battery_voltage_x1000;
      volatile uint8_t ui8_battery_current_x10;
      volatile uint8_t ui8_controller_system_state;
      volatile uint8_t ui8_braking;
      volatile uint16_t ui16_battery_wh;
    } struct_tsdz_status;
    */


    public boolean setData(byte[] data) {
        if (data.length != PERIODIC_ADV_SIZE) {
            Log.e(TAG, "Wrong Status BT message size!");
            return false;
        }
        this.assistLevel = (data[1] & 255);
        this.speed = (float)(((data[3] & 255) << 8) + (data[2] & 255)) / 10;
        this.cadence = (data[4] & 255);
        short s = (short) ((data[5] & 0xff) | (data[6] << 8));
        this.motorTemperature = (float)(s) / 10;
        this.pPower = ((data[8] & 255) << 8) + ((data[7] & 255));
        this.pPower = (this.pPower+5)/10;
        this.volts = (float)(((data[10] & 255) << 8) + (data[9] & 255)) / 1000;
        this.amperes = (float)(data[11] & 255) / 10;
        this.status = (data[12] & 255);
        this.brake = (data[13] & 255) != 0;
        this.wattHour = ((data[15] & 255) << 8) + ((data[14] & 255));
        this.streetMode = (data[16] & 255) != 0;
        return true;
    }
}
