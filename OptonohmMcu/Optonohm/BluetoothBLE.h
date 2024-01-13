#ifndef OPTONOHM_CIPEDSERVICE_H_
#define OPTONOHM_CIPEDSERVICE_H_

#include <string>
#include <BLEDevice.h>
#include "Timer.h"

namespace optonohm
{

//b1fbda81-f607-42a1-827d-f84ae6bdf20a created UUID

#define OPTONOHM_SERVICE_UUID  "b1fb1816-f607-42a1-827d-f84ae6bdf20a"  //2000 Optonohm 
#define OPTONOHM_CLIENT_CONF_DESC_UUID "b1fb2902-f607-42a1-827d-f84ae6bdf20a" //Client Characteristic Configuration  2902

//Control Point
#define OPTONOHM_CONTROL_POINT_CHARACTER_UUID "b1fb0002-f607-42a1-827d-f84ae6bdf20a"  //0002


typedef struct __attribute__((packed, aligned(1))) OptonohmColorData_t
{
	uint8_t red;
  uint8_t green;
  uint8_t blue;
}OptonohmColor;

typedef struct __attribute__((packed, aligned(1))) OptonohmData_t
{
  uint16_t Version;
  uint16_t BPM;
  uint8_t BeatDivisor;
  uint8_t BeatMultiplicator;
  uint8_t BeatPulseTimeMs;
  OptonohmColorData_t BeatColor;
  uint8_t BeatBrightness;
  uint8_t StartBeatPulseTimeMs;
  OptonohmColorData_t StartBeatColor;
  uint8_t StartBeatBrightness;
  uint8_t State;
}OptonohmData ;

typedef struct __attribute__((packed, aligned(1))) CipedControlPoint_t
{
  uint8_t OpRequestCode;
  uint8_t OpResultCode;
  OptonohmData Parameter;
}OptonohmControlPoint ;

enum class OptonohmControlPointOpCodes_e:uint8_t
{
  None,
  SetConfig,
  GetConfig,
  Start,
  Stop,
  Version,
  Max
};


enum class OptonohmControlPointResultCodes_e:uint8_t
{
  Reserved = 0,
  Success,
  NotSupported,
  InvalidParameter,
  OperationFailed  
};

class OptonohmServiceEvents
{
  public:
  virtual uint8_t ControlPointWritten(optonohm::OptonohmControlPointOpCodes_e opCode,optonohm::OptonohmControlPoint* data) = 0;
};

class OptonohmService:public BLECharacteristicCallbacks,public BLEDescriptorCallbacks,public BLEServerCallbacks
{
  private:
  BLECharacteristic* _OptonohmControlPointCharacteristic;
  BLEServer *_Server;
  BLEService *_Service;
  OptonohmServiceEvents* _Listener;
 
  bool _DeviceConnected;
  uint32_t _State;
  optonohm::OptonohmControlPoint _Cpframe = {0};

  void processOptonohmControlPoint(uint8_t* data);
  void processOptonohmControlPointResponse(OptonohmControlPoint* data);


public:
  OptonohmService( BLEServer *pServer,OptonohmServiceEvents* listener);
  ~OptonohmService(); 
  void start();
  void stop();
  void process();
  void sendConfig(optonohm::OptonohmControlPoint* data);
  const bool getConnected()
  {
    return _DeviceConnected;
  }
//Implements

void onConnect(BLEServer* pServer);
void onDisconnect(BLEServer* pServer);

void onWrite(BLECharacteristic *pCharacteristic);
void onStatus(BLECharacteristic* pCharacteristic, Status s, uint32_t code) ;
void onRead(BLECharacteristic* pCharacteristic, esp_ble_gatts_cb_param_t* param);
void onNotify(BLECharacteristic* pCharacteristic);

void onRead(BLEDescriptor* pDescriptor);
void onWrite(BLEDescriptor* pDescriptor);

void TimerElapsed(int32_t id);
    
};

}//namespace CipedTronic

#endif //CIPEDTRONIC_CIPEDSERVICE_H_
