#include "Optonohm.h"
#include <EEPROM.h>

namespace 
{
  void printConfig(const optonohm::OptonohmData& config)
  {
     Serial.printf("Config BMP %d  startcolor %x %x %x beat %x %x %x\r\n"
       ,config.BPM
       , config.StartBeatColor.red
       , config.StartBeatColor.green
       , config.StartBeatColor.blue
       , config.BeatColor.red
       , config.BeatColor.green
       , config.BeatColor.blue
       );
  }
}

optonohm::Optonohm::Optonohm(int pin,int countOfLeds,uint16_t version)
:_Config({version,100,4,4,50,{0,255,0},50,70,{255,0,0},255,2})
,_BPMTicks(getBPMTicks(_Config)) //ms
,_Pin(pin)
,_Leds(7)
,_Start(false)
,_BeatCount(0)
,_CountOfLeds(countOfLeds)
,_Ws2812b(countOfLeds,pin, NEO_GRB + NEO_KHZ800)
,_Version(version)
,_ProcessReset(false)
,_BeatPresets({4,4},{4,3},{4,2},{8,6})
,_Led2(D8)
,_Led3(D7)
,_Thread(1,this,NULL,"Opt")
{
    pinMode(_Led2, OUTPUT);
    pinMode(_Led3, OUTPUT);
    _Ws2812b.begin();
    _Ws2812b.show();
    _Ws2812b.setBrightness(_Config.BeatBrightness);
    EEPROM.begin(sizeof(optonohm::OptonohmData));
    uint16_t test = EEPROM.readUShort(0);
    if(test == 0xffff)
    {
        EEPROM.put<optonohm::OptonohmData>(0,_Config);
        EEPROM.commit();
        return;
    }
    EEPROM.get<optonohm::OptonohmData>(0,_Config);
     int beatpreset = getBeatPreset(_Config.BeatDivisor,_Config.BeatMultiplicator);
  setBeatLeds(beatpreset);
    _Config.State = 2;
    _BPMTicks = getBPMTicks(_Config);
}

void optonohm::Optonohm::setBeatLeds(int beatNumber)
{
  if(beatNumber == -1)
  {
    digitalWrite(_Led2,LOW);
    digitalWrite(_Led3,LOW);
    return;
  }
  int tmp= (beatNumber & 0x01);
  digitalWrite(_Led2,(int)tmp);
  tmp = (beatNumber & 0x02) >> 1;
  digitalWrite(_Led3,(int)tmp);
}

int optonohm::Optonohm::getBeatPreset( uint8_t div,uint8_t mul)
{
    for(int i = 0; i< 4;i++)
    {
      if((_BeatPresets[i].div == div) && (_BeatPresets[i].mul == mul))
      {
          return i;
      }
    }
    return -1;
}
void optonohm::Optonohm::setBeatPreset(int preset)
{
  if(preset > 3)
  {
    return;
  }
  if(preset < 0)
  {
    return;
  }

  setBeatLeds(preset);
  setBeat(_BeatPresets[preset].div,_BeatPresets[preset].mul);
  
}

long optonohm::Optonohm::getBPMTicks(const optonohm::OptonohmData& config)
{
  return  60 * 1000 / _Config.BPM / (config.BeatDivisor /4);
}

void optonohm::Optonohm::callback(void* param)
{
  processOptonohm();
}

void optonohm::Optonohm::start()
{
  _Start = true;
  _Config.State = (uint8_t)optonohm::State_e::Started;
}

void optonohm::Optonohm::stop()
{
   _Start = false;
   _Config.State = (uint8_t)optonohm::State_e::Stopped;
}

void optonohm::Optonohm::increaseBPM()
{
  if(_Config.BPM > _MinBpm)
  {
      _Config.BPM -= _InDeCreasestep;
      _BPMTicks = getBPMTicks(_Config);
      EEPROM.put<optonohm::OptonohmData>(0,_Config);
      EEPROM.commit();
  }
}

void optonohm::Optonohm::decreaseBPM()
{
  if(_Config.BPM < _MaxBpm)
  {
    _Config.BPM += _InDeCreasestep;
    _BPMTicks = getBPMTicks(_Config);
    EEPROM.put<optonohm::OptonohmData>(0,_Config);
    EEPROM.commit();
  }
}

void optonohm::Optonohm::setBeat(uint8_t div,uint8_t mul)
{
  int beatpreset = getBeatPreset(div,mul);
  setBeatLeds(beatpreset);
  
  _Config.BeatMultiplicator = mul;
  _Config.BeatDivisor = (uint8_t)div;
  _BPMTicks = getBPMTicks(_Config);
  EEPROM.put<optonohm::OptonohmData>(0,_Config);
  EEPROM.commit();
}

void optonohm::Optonohm::setConfig(const optonohm::OptonohmData& config)
{
  ::memcpy(&_Config,&config,sizeof(optonohm::OptonohmData));
  _Config.Version = _Version;
  _BPMTicks = getBPMTicks(_Config);
  EEPROM.put<optonohm::OptonohmData>(0,_Config);
  EEPROM.commit();
   int beatpreset = getBeatPreset(_Config.BeatDivisor,_Config.BeatMultiplicator);
  setBeatLeds(beatpreset);
}

void optonohm::Optonohm::getConfig(optonohm::OptonohmData& config)
{
  ::memcpy(&config,&_Config,sizeof(optonohm::OptonohmData));
}

const optonohm::State_e optonohm::Optonohm::getState()
{
  return (optonohm::State_e)_Config.State;
}

void optonohm::Optonohm::showPixels(int startPixel, int endPixel, const optonohm::OptonohmColor& color,int brightness)
{
  _Ws2812b.setBrightness(brightness);
  for (int pixel = startPixel; pixel < endPixel; pixel++) { 
    _Ws2812b.setPixelColor(pixel, _Ws2812b.Color(color.red, color.green, color.blue));
  }
  _Ws2812b.show();
  //wait data is written
  delay(1);
}

void optonohm::Optonohm::reset()
{
  _Start = false; 
  _Config.Version = _Version;
  _Config.BPM = 100;
  _Config.BeatDivisor = 4;
  _Config.BeatMultiplicator = 4;
  _Config.BeatPulseTimeMs = 50;
  _Config.BeatColor.red = 0x00;
  _Config.BeatColor.green = 0xff;
  _Config.BeatColor.blue = 0x00;
  _Config.BeatBrightness = 100;
  _Config.StartBeatPulseTimeMs = 50;
  _Config.StartBeatColor.red  = 0xff;
  _Config.StartBeatColor.green = 0x00;
  _Config.StartBeatColor.blue = 0x00;
  _Config.StartBeatBrightness = 100;
  _Config.State = (uint8_t)optonohm::State_e::Stopped;
  EEPROM.put<optonohm::OptonohmData>(0,_Config);
  EEPROM.commit();
   int beatpreset = getBeatPreset(_Config.BeatDivisor,_Config.BeatMultiplicator);
  setBeatLeds(beatpreset);
  _ProcessReset = true; 
}

void optonohm::Optonohm::handleReset()
{
     const optonohm::OptonohmColor color = {255,255,255};
       for(int i = 0;i < 10;i++)
       {
         showPixels(0,_CountOfLeds,color,255);
          _Thread.sleep(100);
          _Ws2812b.clear();
          _Ws2812b.show();
           _Thread.sleep(100);
       }
      _ProcessReset = false;
}

void optonohm::Optonohm::processOptonohm()
{
   Serial.println("processOptonohm");
   while(1)
   {
    if(_ProcessReset)
    {
      handleReset();
    }

    if(_Start)
    {
      if(_BeatCount == 0)
      {
        showPixels(0,_CountOfLeds,_Config.StartBeatColor,_Config.StartBeatBrightness);
        _Thread.sleep(_Config.StartBeatPulseTimeMs);
        _Ws2812b.clear();
        _Ws2812b.show();
      }
      else
      {     
        showPixels(0,_CountOfLeds,_Config.BeatColor,_Config.BeatBrightness);
        _Thread.sleep(_Config.BeatPulseTimeMs);
        _Ws2812b.clear();
        _Ws2812b.show();
      }
      _BeatCount++;
      if(_BeatCount >= _Config.BeatMultiplicator)
      {
        _BeatCount = 0;
      }
    }
    else
    {
      _BeatCount = 0;
    }
      _Thread.sleep(_BPMTicks);
   }
}


