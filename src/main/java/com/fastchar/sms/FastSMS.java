package com.fastchar.sms;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastHandler;
import com.fastchar.sms.entity.FinalSmsEntity;
import com.fastchar.sms.exception.FastSMSException;
import com.fastchar.sms.interfaces.IFastSMSInterface;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastSMS {

    public static FastSMS getInstance() {
        return FastChar.getOverrides().newInstance(FastSMS.class);
    }

    private FastSMS() {}

    private String configOnlyCode;
    private String phone;
    private String type;
    private Map<String, Object> params = new LinkedHashMap<String, Object>();
    private FinalSmsEntity smsEntity;

    public String getConfigOnlyCode() {
        return configOnlyCode;
    }

    public FastSMS setConfigOnlyCode(String configOnlyCode) {
        this.configOnlyCode = configOnlyCode;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public FastSMS setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getType() {
        return type;
    }

    public FastSMS setType(String type) {
        this.type = type;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public FinalSmsEntity getSmsEntity() {
        return smsEntity;
    }

    public FastSMS addParams(String name, String value) {
        if (FastStringUtils.isEmpty(value)||FastStringUtils.isEmpty(name)) {
            return this;
        }
        this.params.put(name, value);
        return this;
    }

    private void validPhone() {
        if (FastStringUtils.isEmpty(this.phone)) {
            throw new FastSMSException("手机号码phone不可为空！");
        }
    }

    private void validType() {
        if (FastStringUtils.isEmpty(this.type)) {
            throw new FastSMSException("短信类型type不可为空！");
        }
    }

    private void validTemplate() {
        FastSMSConfig config = FastChar.getConfig(getConfigOnlyCode(),FastSMSConfig.class);
        if (FastStringUtils.isEmpty(config.getTemplate(this.type))) {
            throw new FastSMSException(MessageFormat.format("短信类型{0}的模板不存在！", this.type));
        }
    }


    private List<String> getKeys(String content) {
        List<String> keys = new ArrayList<String>();
        String regStr = "\\{([^{}]*)}";
        Pattern compile = Pattern.compile(regStr);
        Matcher matcher = compile.matcher(content);
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
        return keys;
    }

    private String replaceHolder(String content, Map<String, Object> params) {
        List<String> keys = getKeys(content);
        for (String key : keys) {
            String value = "";
            if (params.containsKey(key)) {
                value = String.valueOf(params.get(key));
            }
            String keyModule = "\\{" + key + "}";
            content = content.replaceAll(keyModule, value);
        }
        return content;
    }


    private FastHandler validMaxCount() {
        FastHandler handler = new FastHandler();
        handler.setCode(0);

        FastSMSConfig config = FastChar.getConfig(getConfigOnlyCode(),FastSMSConfig.class);
        if (FastChar.getDatabases().hasDatabase() && !config.isDebug()) {
            int countDaySend = FinalSmsEntity.dao().countDaySend(this.phone, this.type);
            if (config.getMaxCountByDay(this.type) > 0) {
                if (countDaySend >= config.getMaxCountByDay(this.type)) {
                    handler.setCode(-1);
                    handler.setError("您操作太频繁了，请明天重新尝试！");
                }
            }
        }
        return handler;
    }

    public FastHandler sendCode() {
        FastSMSConfig config = FastChar.getConfig(getConfigOnlyCode(),FastSMSConfig.class);
        StringBuilder maxBuilder = new StringBuilder("9");
        StringBuilder minBuilder = new StringBuilder("1");
        for (int i = 0; i < config.getCodeLength() - 1; i++) {
            maxBuilder.append("9");
            minBuilder.append("0");
        }

        int max = FastNumberUtils.formatToInt(maxBuilder.toString());
        int min = FastNumberUtils.formatToInt(minBuilder.toString());

        Random random = new Random();
        int code = random.nextInt(max) % (max - min + 1) + min;

        this.params.put("code", String.valueOf(code));
        return send();
    }

    public FastHandler send() {
        this.validPhone();
        this.validType();
        this.validTemplate();

        FastSMSConfig config = FastChar.getConfig(getConfigOnlyCode(),FastSMSConfig.class);
        FastHandler validMaxCount = this.validMaxCount();
        if (validMaxCount.getCode() != 0) {
            return validMaxCount;
        }

        String template = config.getTemplate(this.type);

        String content = replaceHolder(template, this.params);
        smsEntity= FinalSmsEntity.newInstance();
        if (params != null && params.size() > 0) {
            smsEntity.putAll(params);
        }
        smsEntity.put("sendPhone", phone);
        smsEntity.put("sendType", type);
        smsEntity.put("sendContent", content);
        smsEntity.put("sendParams", FastChar.getJson().toJson(this.params));

        if (config.isDebug()) {
            smsEntity.put("sendResult", "调试模式，默认短信发送成功！");
            smsEntity.save();
            FastChar.getLog().info(smsEntity.toJson());
            if (this.params.containsKey("code")) {
                return new FastHandler().setCode(0).setError("调试模式，默认短信发送成功！" + this.params.get("code"));
            }else{
                return new FastHandler().setCode(0).setError("调试模式，默认短信发送成功！" );
            }
        }else{
            IFastSMSInterface iFastSMSInterface = FastChar.getOverrides().singleInstance(IFastSMSInterface.class);
            FastHandler handler = iFastSMSInterface.postSMS(smsEntity);
            if (handler.getCode() == 0) {
                if (FastChar.getDatabases().hasDatabase()) {
                    smsEntity.put("sendResult", handler.getError());
                    smsEntity.save();
                }
            }
            return handler;
        }
    }


    public FastHandler validCode(int code) {
        this.validPhone();
        this.validType();

        FastHandler handler = new FastHandler();
        FastSMSConfig config = FastChar.getConfig(getConfigOnlyCode(),FastSMSConfig.class);
        smsEntity = FinalSmsEntity.dao().getSmsEntity(this.type, this.phone);
        if (smsEntity == null) {
            handler.setCode(-1);
            handler.setError("请您先获取验证码！");
            return handler;
        }

        Map sendParams = smsEntity.getSendParams();
        if (!sendParams.containsKey("code")) {
            handler.setCode(-1);
            handler.setError("请您先获取验证码！");
            return handler;
        }
        int sendCode = FastNumberUtils.formatToInt(sendParams.get("code"));
        int maxSecond = config.getMaxSecondByValid(this.type);
        if (maxSecond > -1) {
            Date sendDateTime = smsEntity.getSendDateTime();
            long timeSpan = System.currentTimeMillis() - sendDateTime.getTime();
            if (timeSpan > maxSecond * 1000) {
                handler.setCode(-2);
                handler.setError("验证码已失效，请您重新获取！");
                return handler;
            }
        }
        if (sendCode != code) {
            handler.setCode(-3);
            handler.setError("验证码错误，请您重新输入！");
            return handler;
        }

        if (FastChar.getDatabases().hasDatabase()) {
            if (smsEntity.delete("sendPhone", "sendType")) {
                handler.setCode(0);
                handler.setError("有效验证码！");
            } else {
                handler.setCode(-9);
                handler.setError(smsEntity.getError());
            }
        }else{
            handler.setCode(0);
            handler.setError("有效验证码！");
        }
        return handler;
    }


}
