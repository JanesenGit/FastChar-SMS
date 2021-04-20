package com.fastchar.sms.interfaces;

import com.fastchar.annotation.AFastOverrideError;
import com.fastchar.core.FastHandler;
import com.fastchar.sms.entity.FinalSmsEntity;

@AFastOverrideError("请实现IFastSMSInterface接口，对接到自身的短信接口！")
public interface IFastSMSInterface {

    FastHandler postSMS(FinalSmsEntity smsEntity);

}
