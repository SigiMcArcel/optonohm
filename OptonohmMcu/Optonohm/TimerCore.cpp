#include "esp32-hal-timer.h"
#include "TimerCore.h"

ESPTimer::TimerCore* ESPTimer::TimerCore::_Instance = NULL;

void IRAM_ATTR ESPTimer::TimerCore::onTimer(){
   portENTER_CRITICAL_ISR(&_Instance->_TimerMux);
    _Instance->_TimerTick++;
    portEXIT_CRITICAL_ISR(&_Instance->_TimerMux);
}



ESPTimer::TimerCore::TimerCore()
:_TimerMux(portMUX_INITIALIZER_UNLOCKED)
{
  
  _Timer = timerBegin(80);
  timerAttachInterrupt(_Timer, &onTimer);
  timerWrite(_Timer, 100);
   
}
  
ESPTimer::TimerCore* ESPTimer::TimerCore::Instance()
{
  if(ESPTimer::TimerCore::_Instance == NULL)
  {
      ESPTimer::TimerCore::_Instance = new ESPTimer::TimerCore; 
  }
  return ESPTimer::TimerCore::_Instance;
}

uint32_t ESPTimer::TimerCore::getTick()
{
  return _TimerTick;
}
  void ESPTimer::TimerCore::start()
  {
    timerStart(_Timer);   
  }
  void ESPTimer::TimerCore::stop()
  {
      timerStop(_Timer);
  }



