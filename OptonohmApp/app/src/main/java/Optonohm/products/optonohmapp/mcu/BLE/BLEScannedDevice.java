package Optonohm.products.optonohmapp.mcu.BLE;

public class BLEScannedDevice {
    public String Name;
    public String Address;

    @Override
    public String toString()
    {
        return Address + " " + Name;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof BLEScannedDevice) {
            BLEScannedDevice s = (BLEScannedDevice) object;
            return this.Address.equals(s.Address);
        }
        return false;
    }

    public String getAddress()
    {
        return Address;
    }

    @Override
    public int hashCode() {
        return Address.hashCode();
    }
}
