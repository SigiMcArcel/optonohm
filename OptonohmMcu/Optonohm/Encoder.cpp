#include "Encoder.h"


namespace{
  typedef struct isrInfo_t
  {
      long Counter;
      long LastCounter;
      int PinA;
      int PinB;
  }IsrInfo;

	static IsrInfo _IsrInfo;
	static void EncoderPinIsr()
	{
			if(digitalRead(_IsrInfo.PinA))
			{
				_IsrInfo.Counter++;
			}
			else
			{
				_IsrInfo.Counter--;
			}
      
  }
}

optonohm::Encoder::Encoder(int pinA,int pinB,int pinSwitch,int threshold,int cycleTime,int longButtonDownTime, optonohm::EncoderCallBack* callback)
	:_PinA(pinA)
	,_PinB(pinB)
	,_PinSwitch(pinSwitch)
  ,_Timer(cycleTime,1,this,true)
  ,_Counter(0)
  ,_LastCounter(0)
  ,_Callback(callback)
  ,_Threshold(threshold)
  ,_CycleTime(cycleTime)
  ,_LongButtonDownTime(longButtonDownTime)
  ,_LongLongButtonDownTime(longButtonDownTime * 2)
  ,_LastButtonState(false)
  ,_ButtonFlagDown(false)
  ,_ButtonFlagLongDown(false)
  ,_ButtonFlagLongLongDown(false)
  ,_ButtonTimeOut(0)
	{
    _IsrInfo.PinA = _PinA;
    _IsrInfo.PinB = _PinB;
		pinMode(_PinA, INPUT_PULLUP);
		pinMode(_PinB, INPUT_PULLUP);
		pinMode(_PinSwitch, INPUT_PULLUP);
		
		_IsrInfo.Counter = 0;
		attachInterrupt(digitalPinToInterrupt(_PinB), EncoderPinIsr, RISING );
  _Timer.start();
	}

  int optonohm::Encoder::pinA()
	{
		return _PinA;
	}
	
	int optonohm::Encoder::pinB()
	{
		return _PinB;
	}
	
	long optonohm::Encoder::counter()
	{
		return _Counter;
	}
	
	void optonohm::Encoder::resetCounter()
	{
		_Counter = 0;
    _IsrInfo.Counter = 0;
	}
	
	bool optonohm::Encoder::getButtonState()
	{
		return (bool)digitalRead(_PinSwitch);
	}

  void optonohm::Encoder::processEncoder()
  {
    long diff = 0;  
    long diff2 = 0;  
    long tmp = _IsrInfo.Counter;
    EncoderDirection dir = EncoderDirection::Up;
    if(_IsrInfo.LastCounter > tmp)
    {
        diff = _IsrInfo.LastCounter - tmp;
        _Counter -= diff;
        dir = EncoderDirection::Down;
    }
    else if (_IsrInfo.LastCounter < tmp)
    {
      diff = tmp - _IsrInfo.LastCounter;
      _Counter -= diff;
      dir = EncoderDirection::Up;
    }
    _IsrInfo.LastCounter = tmp;

    if(_LastCounter < _Counter)
    {
      diff2 = _Counter - _LastCounter;
      if(diff2 > (long)_Threshold)
      {
           _LastCounter = _Counter;
      }
    }else  if(_LastCounter > _Counter)
    {
      diff2 = _LastCounter - _Counter;
       if(diff2 > (long)_Threshold)
      {
           _LastCounter = _Counter;
            if(_Callback != NULL)
            {
              _Callback->EncoderChanged(_Counter,dir);
            }
      }
    }
  }

  void optonohm::Encoder::processButton()
  {
      bool tmp = !getButtonState();
     // Serial.printf("getButtonState %d %d \n",tmp,_LastButtonState);
      //Button down
      if((_LastButtonState == tmp) && (tmp == false) && !_ButtonFlagUp)
      {
         if(_ButtonFlagDown)
         {
          if(_Callback != NULL)
          {
            if(!_ButtonFlagLongDown)
            {
            _Callback->EncoderButtonClick();
            }
            else
            {
               _Callback->EncoderButtonLongClick();
            }
          }
         }

        _ButtonFlagUp = true;
        _ButtonFlagDown = false;
        _ButtonFlagLongDown = false;
        _ButtonFlagLongLongDown = false;
        if(_Callback != NULL)
        {
          _Callback->EncoderButtonUp();   
        }
      }

      if((_LastButtonState == tmp) && (tmp == true) && !_ButtonFlagDown)
      {
         _ButtonFlagUp = false;
         _ButtonFlagDown = true;
        if(_Callback != NULL)
        {
          _Callback->EncoderButtonDown();
        }
      }

      _LastButtonState = tmp;
      if(_ButtonFlagDown)
      {
         _ButtonTimeOut++;
        if(!_ButtonFlagLongDown)
        {
          
           if(_ButtonTimeOut >= (_LongButtonDownTime/_CycleTime))
           {
             _ButtonFlagLongDown = true;
            if(_Callback != NULL)
            {
              _Callback->EncoderButtonLongDown();
            }
           }
        }
        if(!_ButtonFlagLongLongDown)
        {
           if(_ButtonTimeOut >= (_LongLongButtonDownTime/_CycleTime))
           {
             _ButtonFlagLongLongDown = true;
            if(_Callback != NULL)
            {
              _Callback->EncoderButtonLongLongDown();
            }
           }
        }
      }
      else
      {
        _ButtonTimeOut = 0;
      }



     
  }

void optonohm::Encoder::TimerElapsed(int32_t id)
{
 
    processEncoder();
   processButton();
}
void optonohm::Encoder::process()
{
  _Timer.process();
}

