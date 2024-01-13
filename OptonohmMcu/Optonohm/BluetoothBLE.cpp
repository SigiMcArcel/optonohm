#include "BluetoothBLE.h"
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLEService.h>
#include <BLE2902.h>
#include "Timer.h"


optonohm::OptonohmService::OptonohmService(BLEServer *pServer,OptonohmServiceEvents* listener)
:_OptonohmControlPointCharacteristic(
    new BLECharacteristic(OPTONOHM_CONTROL_POINT_CHARACTER_UUID,
    BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_INDICATE))
,_Server(pServer)
,_Service(NULL)
,_Listener(listener)
,_DeviceConnected(false)
,_Cpframe({0})
{
   if(_Server != NULL)
   {
      _Server->setCallbacks(this);
      _Service = pServer->createService(OPTONOHM_SERVICE_UUID);
      if(_Service != NULL)
      {
        Serial.println("Service created");
    
        _OptonohmControlPointCharacteristic->addDescriptor(new BLE2902()); 
        _Service->addCharacteristic(_OptonohmControlPointCharacteristic);
        _OptonohmControlPointCharacteristic->setCallbacks(this);   
        BLEDevice::startAdvertising();  
      }
      else
      {
         Serial.println("_Service null");
      }
   }
   else
   {
      Serial.println("Sever null");
   }
}

optonohm::OptonohmService::~OptonohmService ()
{
 
}
 
void optonohm::OptonohmService::start()
{
   if(_Service != NULL)
  {
    Serial.println("Service started");
    _Service->start();
  }
}

void optonohm::OptonohmService::stop()
{
   if(_Service != NULL)
  {
    _Service->stop();
  }
}

void optonohm::OptonohmService::sendConfig(optonohm::OptonohmControlPoint* data)
{
  _OptonohmControlPointCharacteristic->setValue((uint8_t*)data,sizeof(optonohm::OptonohmControlPoint));  
  _OptonohmControlPointCharacteristic->indicate();
}

void optonohm::OptonohmService::processOptonohmControlPoint(uint8_t* data)
{
  OptonohmControlPoint* optonohmControlPointData = (OptonohmControlPoint*)data;
  
  if(optonohmControlPointData == NULL)
  {
    return;    
  }
  if((OptonohmControlPointOpCodes_e)optonohmControlPointData->OpRequestCode >= OptonohmControlPointOpCodes_e::Max)
  {
    optonohmControlPointData->OpResultCode = static_cast<uint8_t>(OptonohmControlPointResultCodes_e::NotSupported);
    optonohmControlPointData->Parameter = {0};
    processOptonohmControlPointResponse(optonohmControlPointData);
    return;
  }
  if(_Listener)
  {
    optonohmControlPointData->OpResultCode = _Listener->ControlPointWritten((OptonohmControlPointOpCodes_e)optonohmControlPointData->OpRequestCode, optonohmControlPointData);
  }
  processOptonohmControlPointResponse(optonohmControlPointData);
}


void optonohm::OptonohmService::processOptonohmControlPointResponse(OptonohmControlPoint* data)
{
 // Serial.println("processOptonohmControlPointResponse 1");
  _OptonohmControlPointCharacteristic->setValue((uint8_t*)data,sizeof(OptonohmControlPoint));  
  _OptonohmControlPointCharacteristic->indicate();
}



void optonohm::OptonohmService::onWrite(BLECharacteristic *pCharacteristic)
{
   Serial.printf("onWrite %d \n", pCharacteristic->getLength());
  if(pCharacteristic->getUUID().equals(_OptonohmControlPointCharacteristic->getUUID()))
  {
    
    processOptonohmControlPoint(pCharacteristic->getData());
  }
}

void optonohm::OptonohmService::onStatus(BLECharacteristic* pCharacteristic, Status s, uint32_t code)
{
 
}

void optonohm::OptonohmService::onRead(BLECharacteristic* pCharacteristic, esp_ble_gatts_cb_param_t* param)
{
  
}

void optonohm::OptonohmService::onNotify(BLECharacteristic* pCharacteristic)
{
  
}

void optonohm::OptonohmService::onRead(BLEDescriptor* pDescriptor)
{
  
}

void optonohm::OptonohmService::onWrite(BLEDescriptor* pDescriptor)
{
  
}

void optonohm::OptonohmService::onConnect(BLEServer* pServer) 
{
  _DeviceConnected = true;
  BLEDevice::stopAdvertising();  
  Serial.printf("connected\r\n");
};

void optonohm::OptonohmService::onDisconnect(BLEServer* pServer) 
{
  _DeviceConnected = false;
  Serial.println("disconnected");
   BLEDevice::startAdvertising();
}
