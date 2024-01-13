package Optonohm.products.optonohmapp.mcu.mcu;

import kotlin.UShort;

public class OptonohmConfiguration {

    public enum  OptonohmControlPointState
    {
        Idle,
        Started,
        Stopped
    }
    public class OptonohmColor
    {
        public byte Red;
        public byte Green;
        public byte Blue;

        public int getColor()
        {
            //add alpha channel
            int iBlue = Byte.toUnsignedInt(Blue);
            int iGreen = Byte.toUnsignedInt(Green);
            int iRed = Byte.toUnsignedInt(Red);
            int iColor = ( (iRed << 16) | (iGreen << 8) | iBlue) ;
            return iColor;
        }

        public int getColorAlpha()
        {
            //add alpha channel
            return  getColor() | 0xff000000;
        }

        public void setColor(int color)
        {
            Red = (byte)((color & 0x00ff0000) >> 16);
            Green = (byte)((color & 0x0000ff00) >> 8);
            Blue =  (byte)(color & 0x000000ff);
        }



    }
    public int Version;
    public int BMP;
    public byte BeatDivisor;
    public byte BeatMultiplicator;
    public byte BeatPulseTimeMs;
    public OptonohmColor BeatColor = new OptonohmColor();
    public byte BeatBrightness;
    public byte StartBeatPulseTimeMs;
    public OptonohmColor StartBeatColor = new OptonohmColor();
    public byte StartBeatBrightness;
    public byte State;



    public OptonohmConfiguration()
    {
    }

}
