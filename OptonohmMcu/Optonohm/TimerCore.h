#ifndef CIPEDTRONIC_TIMERCORE_H_
#define CIPEDTRONIC_TIMERCORE_H_
#include "esp32-hal-timer.h"
#include <string>
#include "Arduino.h"

namespace ESPTimer
{	
	class TimerCore
	{
	  public:
	  static TimerCore* _Instance;
	  uint32_t _TimerTick;
	  hw_timer_t* _Timer;
    portMUX_TYPE _TimerMux;    
	  TimerCore();

	  static void IRAM_ATTR onTimer();
	public:
	 // deleting copy constructor
	  TimerCore(const TimerCore& obj) = delete;
	  static TimerCore* Instance();
	  uint32_t getTick();
    void start();
    void stop();
	};
}
#endif //CIPEDTRONIC_TIMERCORE_H_


 

