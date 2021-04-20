package com.fastchar.sms.entity;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastEntity;
import com.fastchar.core.FastMapWrap;
import com.fastchar.utils.FastDateUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FinalSmsEntity extends FastEntity<FinalSmsEntity> {
    private static final long serialVersionUID = 1L;

    public static FinalSmsEntity dao() {
        return FastChar.getOverrides().singleInstance(FinalSmsEntity.class);
    }

    public static FinalSmsEntity newInstance() {
        return FastChar.getOverrides().newInstance(FinalSmsEntity.class);
    }

    protected FinalSmsEntity() {
    }

    @Override
    public String getTableName() {
        return "final_sms";
    }

    @Override
    public String getTableDetails() {
        return "短信发送记录";
    }

    @Override
    public void setDefaultValue() {
        set("sendDateTime", FastDateUtils.getDateString());
    }

    @Override
    public void convertValue() {
        super.convertValue();
    }


    public String getSendPhone() {
        return getString("sendPhone");
    }

    public String getSendContent() {
        return getString("sendContent");
    }

    public Map<?, ?> getSendParams() {
        if (isEmpty("sendParams")) {
            return new HashMap<String, Object>();
        }
        return FastChar.getJson().fromJson(getString("sendParams"), Map.class);
    }

    public FastMapWrap getSendParamsByWrap() {
        return FastMapWrap.newInstance(getSendParams());
    }

    public Date getSendDateTime() {
        return getDate("sendDateTime");
    }

    public String getSendType() {
        return getString("sendType");
    }


    public FinalSmsEntity getSmsEntity(String type, String phone) {
        String sqlStr = "select * from final_sms where sendType = ? and sendPhone = ? order by sendDateTime desc ";
        return selectFirstBySql(sqlStr, type, phone);
    }


    /**
     * 检测用户单日发送短信次数
     */
    public int countDaySend(String userPhone, String type) {
        String sqlStr = "select count(1) as c from final_sms" +
                " where sendPhone=? and sendDateTime >= ? " +
                " and sendType=?";

        FinalSmsEntity result = selectFirstBySql(sqlStr, userPhone,
                FastDateUtils.getDateString("yyyy-MM-dd"), type);
        if (result != null) {
            return result.getInt("c");
        }

        return 0;
    }


}
