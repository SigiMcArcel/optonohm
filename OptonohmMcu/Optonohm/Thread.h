#ifndef ESP_THREAD_H_
#define ESP_THREAD_H_
#include <string>
#include "Arduino.h"

namespace esputil
{	
	class ESPThreadCallback
	{
		public:
			virtual void callback(void* param) = 0;
	};
	
	class ESPThread
	{
		private:
			TaskHandle_t _Handle;
			int _Priority;
			ESPThreadCallback* _Callback;
			void* _Param;
			std::string _Name;
			
		public:
			ESPThread(int priority,ESPThreadCallback* callback,void* param,const std::string& name);
			~ESPThread();
			int proc();
      void sleep(int ms);
			
	};
}
#endif //ESP_THREAD_H_


 

