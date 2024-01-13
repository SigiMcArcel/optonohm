#include "Timer.h"
#include "TimerCore.h"

ESPTimer::Timer::Timer(int32_t interval,int32_t id, TimerCallback* callback,bool noThread)
:_Interval(interval)
,_Id(id)
,_Callback(callback)
,_LastTick(0)
,_TimeoutStartValue(0)
,_NoThread(noThread)
{
  if(!_NoThread)
  {
	  TimerCore::Instance()->start();
  }
}

void ESPTimer::Timer::start()
{
	_Start = true;
}

void ESPTimer::Timer::stop()
{
	_Start = false;
}
	  
 void ESPTimer::Timer::startAll()
 {
   if(!_NoThread)
  {
    TimerCore::Instance()->start();
  }
 }
  void ESPTimer::Timer::stopAll()
  {
    if(!_NoThread)
    {
      TimerCore::Instance()->stop();
    }
  }

bool ESPTimer::Timer::timeOut(int32_t timeOutValue)
{
  if(_TimeoutStartValue == 0)
  {
    if(!_NoThread)
    {
      _TimeoutStartValue = TimerCore::Instance()->getTick();
    }
    else
    {
        _TimeoutStartValue = millis();
    }
  }  
  else
  {
    if(!_NoThread)
    {
      if((TimerCore::Instance()->getTick() - _TimeoutStartValue) > timeOutValue)
      {
        _TimeoutStartValue = 0;
          return true;
      }      
    }
    else
    {
      if((millis() - _TimeoutStartValue) > timeOutValue)
      {
        _TimeoutStartValue = 0;
          return true;
      }
    }
  }
   return false;
}

void ESPTimer::Timer::timeOutReset()
{
  _TimeoutStartValue = 0;
}
//check interrupt prio
void ESPTimer::Timer::process()
{
   uint32_t tick = 0;
	if(_Start)
	{
    if(!_NoThread)
    {
      tick = TimerCore::Instance()->getTick();   
    }
    else
    {
      tick = millis();

    } 
    if(_LastTick != tick)//prevent double entry
    {
        if(tick - _LastTick >= _Interval)
        {
          if(_Callback != NULL)
          {
            _Callback->TimerElapsed(_Id);
          }      
          _LastTick = tick;
        }
    }
    
	}
}
