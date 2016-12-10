package runners.msband.runnerss;
import android.os.CountDownTimer;

import com.microsoft.band.BandClient;

public class Band {
    private static Band instance;

    private BandClient band;

    private Band(){}

    public void setData(BandClient _band){
        this.band = _band;
    }
    public BandClient getData(){
        return this.band;
    }

    public static synchronized Band getInstance(){
        if(instance==null){
            instance=new Band();
        }
        return instance;
    }
}
