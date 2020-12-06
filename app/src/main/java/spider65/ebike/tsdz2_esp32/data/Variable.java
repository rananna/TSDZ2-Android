package spider65.ebike.tsdz2_esp32.data;

import spider65.ebike.tsdz2_esp32.MyApp;
import spider65.ebike.tsdz2_esp32.R;

public class Variable {
    public DataType dataType;
    public int labelTV;
    public int valueTV;

    public Variable (int labelTV, int valueTV, DataType dataType) {
        this.dataType = dataType;
        this.labelTV = labelTV;
        this.valueTV = valueTV;
    }

    public enum DataType {
        batteryVoltage,
        batteryCurrent,
        batterySOC,
        batteryUsedEnergy,
        batteryADCCurrent,
        motorCurrent,
        motorTemperature,
        motorSpeed,
        speed,
        hallSensors,
        pedalSide,
        throttle,
        throttleADC,
        torqueSensorADC,
        pedalWeight,
        pedalWeightWithOffset,
        pedalCadence,
        dutyCyle,
        focAngle,
        humanPower,
        odometer;

        public String getName() {
            switch (this) {
                case batteryVoltage:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[0];
                case batteryCurrent:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[1];
                case batterySOC:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[2];
                case batteryUsedEnergy:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[3];
                case batteryADCCurrent:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[4];
                case motorCurrent:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[5];
                case motorTemperature:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[6];
                case motorSpeed:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[7];
                case speed:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[8];
                case hallSensors:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[9];
                case pedalSide:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[10];
                case throttle:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[11];
                case throttleADC:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[12];
                case torqueSensorADC:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[13];
                case pedalWeight:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[14];
                case pedalWeightWithOffset:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[15];
                case pedalCadence:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[16];
                case dutyCyle:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[17];
                case focAngle:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[18];
                case humanPower:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[19];
                case odometer:
                    return MyApp.getInstance().getResources().getStringArray(R.array.variables)[20];

            }
            return "";
        }

        public static DataType fromInteger(int x) {
            switch(x) {
                case 0:
                    return batteryVoltage;
                case 1:
                    return batteryCurrent;
                case 2:
                    return batterySOC;
                case 3:
                    return batteryUsedEnergy;
                case 4:
                    return batteryADCCurrent;
                case 5:
                    return motorCurrent;
                case 6:
                    return motorTemperature;
                case 7:
                    return motorSpeed;
                case 8:
                    return speed;
                case 9:
                    return hallSensors;
                case 10:
                    return pedalSide;
                case 11:
                    return throttle;
                case 12:
                    return throttleADC;
                case 13:
                    return torqueSensorADC;
                case 14:
                    return pedalWeight;
                case 15:
                    return pedalWeightWithOffset;
                case 16:
                    return pedalCadence;
                case 17:
                    return dutyCyle;
                case 18:
                    return focAngle;
                case 19:
                    return humanPower;
                case 20:
                    return odometer;
            }
            return null;
        }
    }
}
