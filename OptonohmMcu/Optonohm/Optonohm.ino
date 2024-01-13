
#include "Optonohm.h"
#include <BLEDevice.h>
#include <BLEServer.h>
#include "BluetoothBLE.h"
#include "Encoder.h"
#include "Timer.h"

#define PIN_WS2812B  D2   // ESP32 pin that connects to WS2812B
#define NUM_PIXELS     7  // The number of LEDs (pixels) on WS2812B
#define VERSION (uint16_t)0x0001

namespace optonohmanagers
{
  class BLEManager :public optonohm::OptonohmServiceEvents
  {
  
   
    public:
    optonohm::Optonohm* _Optonohm;

    BLEManager(optonohm::Optonohm* optonohm)
    :_Optonohm(optonohm)
    {

    }

    uint8_t ControlPointWritten(optonohm::OptonohmControlPointOpCodes_e opCode,optonohm::OptonohmControlPoint* data)
    {
      switch(opCode)
      {
        case optonohm::OptonohmControlPointOpCodes_e::SetConfig:
        {
          Serial.printf("ControlPointWritten SetConfig\n");
            _Optonohm->setConfig(data->Parameter);
            return (uint8_t)optonohm::OptonohmControlPointResultCodes_e::Success;
        }
        case optonohm::OptonohmControlPointOpCodes_e::GetConfig:
        {
           Serial.printf("ControlPointWritten GetConfig\n");
            optonohm::OptonohmData config;
            _Optonohm->getConfig(config);
            ::memcpy(&data->Parameter,&config,sizeof(optonohm::OptonohmData));
            return (uint8_t)optonohm::OptonohmControlPointResultCodes_e::Success; 
        }
        case optonohm::OptonohmControlPointOpCodes_e::Start:
        {
           Serial.printf("ControlPointWritten start\n");
            _Optonohm->start();
            return (uint8_t)optonohm::OptonohmControlPointResultCodes_e::Success; 
        } 
        case optonohm::OptonohmControlPointOpCodes_e::Stop:
        {
           Serial.printf("ControlPointWritten stop\n");
            _Optonohm->stop();
            return (uint8_t)optonohm::OptonohmControlPointResultCodes_e::Success;
        }
      }
    }
  };

  class EncoderManager : public optonohm::EncoderCallBack
  {
    private:
      optonohm::Optonohm* _Optonohm;
      optonohm::OptonohmService* _Service;
      bool _BeatMode;
      int _BeatNumber;
      int _Led1;
    public:

      EncoderManager(optonohm::Optonohm* optonohm)
      :_Optonohm(optonohm)
      ,_Service(NULL)
      ,_BeatMode(false)
      ,_BeatNumber(0)
      ,_Led1(D9)
      {
           pinMode(_Led1, OUTPUT);
      }
      void setService(optonohm::OptonohmService* service)
      {
        _Service = service;
      }

  
      void setModeLed(bool val)
      {
         if(val)
         {
            digitalWrite(_Led1,HIGH);
         }
         else
         {
           digitalWrite(_Led1,LOW);
         }
      }

      void handleBeatMode()
      {
        if(_BeatMode)
        {
          _BeatMode = false;
        }
        else
        {
          _BeatMode = true;
        }
        
        Serial.printf("beatmode %d\n",_BeatMode);
      }

      void handleOnOff()
      {
        Serial.println("handleOnOff");
        if(_Optonohm->getState() == optonohm::State_e::Stopped)
        {
          _Optonohm->start();
          sendChanges(optonohm::OptonohmControlPointOpCodes_e::GetConfig);
        }
        else
        {
          _Optonohm->stop();
          sendChanges(optonohm::OptonohmControlPointOpCodes_e::GetConfig);
        }
      }
      void sendChanges(optonohm::OptonohmControlPointOpCodes_e opCode)
      {
        
        if(_Service != NULL)
        {
        // Serial.printf("sendChanges 1 - connected %d\n", _Service->getConnected());
          if(_Service->getConnected())
          {
            optonohm::OptonohmControlPoint cpframe = {0};
            _Optonohm->getConfig(cpframe.Parameter);
            Serial.printf("Baet = %d\n",cpframe.Parameter.BPM);
              cpframe.OpRequestCode = (uint8_t)opCode;
            cpframe.OpResultCode =  (uint8_t)optonohm::OptonohmControlPointResultCodes_e::Success;
            
            
            _Service->sendConfig(&cpframe) ;
          }
        }
      }

      void EncoderChanged(long count, optonohm::EncoderDirection dir)
      {
        Serial.printf("EncoderChanged\n");
        if(!_BeatMode)
        {
          if(dir == optonohm::EncoderDirection::Up)
          {
            _Optonohm->increaseBPM();
          }
          else
          {
            _Optonohm->decreaseBPM();
          }
        }
        else
        {
          _Optonohm->setBeatPreset(_BeatNumber);
          _BeatNumber++;
          if(_BeatNumber >= 4)
          {
            _BeatNumber = 0;
          }
        }
        sendChanges(optonohm::OptonohmControlPointOpCodes_e::GetConfig);
      }

      void EncoderButtonDown()
      {
          Serial.println("button down");
      }

      void EncoderButtonLongDown()
      {
         Serial.println("button long down");
         handleBeatMode();
         setModeLed(_BeatMode);
      }

      void EncoderButtonLongLongDown()
      {
        Serial.println("button long long down");
         _Optonohm->reset();
          sendChanges(optonohm::OptonohmControlPointOpCodes_e::GetConfig);
      }

      void EncoderButtonUp()
      {
        Serial.println("bt up");
      }

      void EncoderButtonClick()
      {
        handleOnOff();
      }

      void EncoderButtonLongClick()
      {
        
      }
      void TimerElapsed(int32_t id)
      {

      }
    
  };

};

optonohm::Optonohm* _Optonohm;
BLEServer* _Server;
optonohm::OptonohmService* _Service;
optonohm::Encoder* _Encoder;
optonohmanagers::BLEManager* _BLEManager;
optonohmanagers::EncoderManager* _EncoderManager;
SET_LOOP_TASK_STACK_SIZE(16*1024)
void setup() {
  Serial.begin(115200);
  vTaskDelay(1000 / portTICK_PERIOD_MS);
  _Optonohm = new optonohm::Optonohm(D2,30,VERSION);
  _BLEManager = new optonohmanagers::BLEManager(_Optonohm);
  _EncoderManager = new optonohmanagers::EncoderManager(_Optonohm);
  _Encoder = new optonohm::Encoder(D4,D5,D6,5,20,2000,_EncoderManager);

  Serial.println("The device started, now you can pair it with bluetooth!");
  
  Serial.printf("Arduino Stack was set to %d bytes", getArduinoLoopTaskStackSize());
  
   
  BLEDevice::init("OPTN");
  _Server = BLEDevice::createServer();
  if(_Server != NULL)
  {
    _Service = new optonohm::OptonohmService(_Server,_BLEManager);
    _EncoderManager->setService(_Service);
    _Service->start();
  }  
}

void loop() {
 
  if(_Encoder != NULL)
  {
    _Encoder->process();
  }
 
}

