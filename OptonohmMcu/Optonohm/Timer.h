#ifndef TIMER_H_
#define TIMER_H_
#include "Arduino.h"


namespace ESPTimer
{
	class TimerCallback
	{
		public:
		virtual void TimerElapsed(int32_t id) = 0;
	};
	
	class Timer
	{
	private :
		int32_t _Interval; //100 usecond
    int32_t _Id;
		TimerCallback* _Callback;
		bool _Start;
    uint32_t _LastTick;
    uint32_t _TimeoutStartValue;
    bool _NoThread;
		
	public:
  //100 usecond
	  Timer(int32_t interval,int32_t id, TimerCallback* callback,bool noThread);
	  void start();
	  void stop();
    void startAll();
    void stopAll();
    bool timeOut(int32_t timeOutValue);
    void timeOutReset();
	  void process();
	};
}
#endif //CIPEDTRONIC_TIMER_H_

 

