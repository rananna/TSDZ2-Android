package casainho.ebike.opensource_ebike_wireless.data;

public class Global {
    private static Global mInstance= null;

    public TSDZ_Periodic TSZD2Periodic = new TSDZ_Periodic();

    protected Global(){}

    public static synchronized Global getInstance() {
        if(null == mInstance){
            mInstance = new Global();
        }
        return mInstance;
    }
}
