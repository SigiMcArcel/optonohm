package Optonohm.products.optonohmapp.mcu.BLE;

import java.time.Clock;

public class BLETimeOut {
    private long _StartTick = -1;
    Clock _Clock = Clock.systemDefaultZone();

    public void reset()
    {
        _StartTick = -1;
    }

    public boolean check(long millisecond)
    {
        long tick = 0;
        long diff = 0;
        if(_StartTick == -1)
        {
            _StartTick = _Clock.millis();
        }
        else
        {
            diff =  _Clock.millis() - _StartTick;
            if(diff > millisecond)
            {
                _StartTick = -1;
                return true;
            }
        }
        return false;
    }
}
