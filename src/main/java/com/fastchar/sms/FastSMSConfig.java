package com.fastchar.sms;

import com.fastchar.interfaces.IFastConfig;
import com.fastchar.utils.FastStringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FastChar-SMS短信配置
 */
public class FastSMSConfig implements IFastConfig {

    private Map<String, String> template = new LinkedHashMap<String, String>();
    private Map<String, Integer> maxCountByDay = new LinkedHashMap<String, Integer>();
    private Map<String, Integer> maxSecondByValid = new LinkedHashMap<String, Integer>();
    private int codeLength = 6;
    private boolean debug;

    /**
     * 获取短信模板
     *
     * @param type 短信类型
     * @return 模板
     */
    public String getTemplate(String type) {
        for (String key : template.keySet()) {
            if (FastStringUtils.matches(key, type)) {
                return template.get(key);
            }
        }
        return template.get(type);
    }

    /**
     * 设置短信模板
     *
     * @param typePattern 短信类型，支持匹配符 *
     * @param template    模板 例如：感谢您注册，您此次的验证码为：{code}
     * @return 当前对象
     */
    public FastSMSConfig setTemplate(String typePattern, String template) {
        this.template.put(typePattern, template);
        return this;
    }

    /**
     * 设置单日短信最大允许发送的次数
     *
     * @param typePattern 短信类型，支持匹配符*
     * @param maxCount    次数
     * @return 当前对象
     */
    public FastSMSConfig setMaxCountByDay(String typePattern, int maxCount) {
        this.maxCountByDay.put(typePattern, maxCount);
        return this;
    }

    /**
     * 获取单日短信最大允许发送的次数
     *
     * @param type 类型
     * @return 最大次数， -1：代表不限制
     */
    public int getMaxCountByDay(String type) {
        for (String key : maxCountByDay.keySet()) {
            if (FastStringUtils.matches(key, type)) {
                return maxCountByDay.get(key);
            }
        }
        return -1;
    }


    /**
     * 获取短信最大有效时间，一般针对验证码使用，默认：-1 不限制
     *
     * @param type 短信类型
     * @return 时间 单位：秒
     */
    public int getMaxSecondByValid(String type) {
        for (String key : maxSecondByValid.keySet()) {
            if (FastStringUtils.matches(key, type)) {
                return maxSecondByValid.get(key);
            }
        }
        return -1;
    }

    /**
     * 设置短信最大有效时间，一般针对验证码使用，默认：-1 不限制
     *
     * @param typePattern 短信类型，支持匹配符 *
     * @param maxSecond   时间 单位：秒
     * @return 当前对象
     */
    public FastSMSConfig setMaxSecondByValid(String typePattern, int maxSecond) {
        this.maxSecondByValid.put(typePattern, maxSecond);
        return this;
    }

    /**
     * 获取验证码的长度
     * @return 验证码长度 默认长度6
     */
    public int getCodeLength() {
        return codeLength;
    }

    /**
     * 设置验证码的长度 默认长度6
     * @param codeLength 长度
     * @return 当前对象
     */
    public FastSMSConfig setCodeLength(int codeLength) {
        this.codeLength = codeLength;
        return this;
    }

    /**
     * 是否是调试模式
     * @return 布尔值
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * 设置是否为调试模式，调试模式不会请求短信接口，在控制台会打印短信内容
     * @param debug 布尔值
     * @return 当前对象
     */
    public FastSMSConfig setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
}
