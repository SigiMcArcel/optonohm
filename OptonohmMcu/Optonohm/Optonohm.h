#ifndef OPTONOHM_H_
#define OPTONOHM_H_

#include <Arduino.h>
#include "BlueToothBLE.h"
#include "Thread.h"
#include <Adafruit_NeoPixel.h>
#include "Encoder.h"

namespace optonohm
{  
 
  enum class State_e
  {
    Idle,
    Started,
    Stopped,
  };

  enum class divisors_t
  {
    half = 2,
    quarter = 4,
    eighth = 8
  };

  struct Beat_t
  {
    uint8_t div;
    uint8_t mul;
  };

  class Optonohm : public esputil::ESPThreadCallback 
  {
    private:

      const int _InDeCreasestep = 10;
      const int _MaxBpm = 300;
      const int _MinBpm = 10;
      
      optonohm::OptonohmData _Config;
      long _BPMTicks;
      int _Pin;
      int _Leds;
      bool _Start;
      long _BeatCount;
      int _CountOfLeds;
      Adafruit_NeoPixel _Ws2812b;
      uint16_t _Version;
      bool _ProcessReset;
      optonohm::Beat_t _BeatPresets[4];
      int _Led1;
      int _Led2;
      int _Led3;

      esputil::ESPThread _Thread;
      
    
      void processOptonohm();
      long getBPMTicks(const optonohm::OptonohmData& config);
      void handleReset();    
      void setBeatLeds(int beatNumber);
      int getBeatPreset(uint8_t div,uint8_t mul);
      
    public:
      Optonohm(int pin,int countOfLeds,uint16_t version);
      void start();
      void stop();
      void increaseBPM();
      void decreaseBPM();
      void setBeatPreset(int preset);
      void setBeat(uint8_t div,uint8_t mul);
      void setConfig(const optonohm::OptonohmData& config);
      void getConfig(optonohm::OptonohmData& config);
      const optonohm::State_e getState();
      void showPixels(int startPixel, int endPixel, const optonohm::OptonohmColor& color,int brightness);
      void reset();

      virtual void callback(void* param);

  };
}
#endif