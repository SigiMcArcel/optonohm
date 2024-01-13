#include "Thread.h"

namespace
{
    static void task( void * pvParameters )
    {
      Serial.println("TASK");
      esputil::ESPThread* param = (esputil::ESPThread*)pvParameters;
      if(param != NULL)
      {
        param->proc();
      }
      
    }
}


esputil::ESPThread::ESPThread(int priority,ESPThreadCallback* callback,void* param,const std::string& name)
:_Priority(priority)
,_Handle(NULL)
,_Callback(callback)
,_Param(param)
,_Name(name)
{
  BaseType_t xReturned;
  Serial.println("TASK create");
xReturned = 	xTaskCreate(
            task,     
            "OPT",         
            40000,     
            (void*)this,    
            1,
            &_Handle); 

if( xReturned != pdPASS )
    {
        /* The task was created.  Use the task's handle to delete the task. */
       Serial.println("TASK create failed");
       return;
    }

    //vTaskResume( _Handle );
    Serial.println("TASK create --");
}

esputil::ESPThread::~ESPThread()
{
	 vTaskDelete( _Handle );
}

int esputil::ESPThread::proc()
{
  Serial.println("TASK");
	if(_Callback != NULL)
	{
    Serial.println("TASK 2");
		_Callback->callback(_Param);
	}
}

void esputil::ESPThread::sleep(int ms)
{
  const TickType_t xDelay = ms / portTICK_PERIOD_MS;
   vTaskDelay( xDelay );
}
