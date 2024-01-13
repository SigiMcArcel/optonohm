#ifndef OPTONOHM_ENCODER_H_
#define OPTONOHM_ENCODER_H_

#include <string>
#include "Arduino.h"
#include "Timer.h"

namespace optonohm
{	
  typedef enum class EncoderDirection_t
  {
      Up,
      Down
  }EncoderDirection;

  class EncoderCallBack
  {
    public:
    virtual void EncoderChanged(long count,EncoderDirection dir);
    virtual void EncoderButtonDown();
    virtual void EncoderButtonLongDown();
    virtual void EncoderButtonLongLongDown();
    virtual void EncoderButtonUp();
    virtual void EncoderButtonClick();
    virtual void EncoderButtonLongClick();
  };

	class Encoder: public ESPTimer::TimerCallback
	{
	private:
		int _PinA;
		int _PinB;
		int _PinSwitch;
    ESPTimer::Timer _Timer;
    long _Counter;
    long _LastCounter;
    EncoderCallBack* _Callback;
    int _Threshold;
    int _CycleTime;
    int _LongButtonDownTime;
    int _LongLongButtonDownTime;

    bool _LastButtonState;
    bool _ButtonFlagUp;
    bool _ButtonFlagDown;
    bool _ButtonFlagLongDown;
    bool _ButtonFlagLongLongDown;
    int _ButtonTimeOut;

	public:
    Encoder(int pinA,int pinB,int pinSwitch,int threshold,int cycleTime,int longButtonDownTime,optonohm::EncoderCallBack* callback);
    int pinA();
    int pinB();
    long counter();
    void resetCounter();
    bool getButtonState();
     void processEncoder();
    void processButton();
    void process();

    virtual void TimerElapsed(int32_t id);

  };
}
#endif //OPTONOHM_ENCODER_H_


 

