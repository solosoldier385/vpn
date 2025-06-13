package com.letsvpn.pay.mapper.ext;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface ExtUpiBlackMapper {

    @Insert("INSERT INTO UpiBlacklistLog (platformId, payConfigId, channelName, orderId, status, createTime, resultText, remark) VALUES "
            + "(#{platformId}, #{payConfigId}, #{channelName}, #{orderId}, #{status}, #{createTime}, #{resultText}, #{remark});")
    @SelectKey(keyProperty = "id", resultType = Long.class, before = false, statement = "SELECT LAST_INSERT_ID()")
    int addUpiBlackLog(Map<String, Object> body);

    @Select({ "<script>", "SELECT * FROM UpiBlacklistLog",
            "<where> ",
            "<if test='paramsMap.beginTime != null and paramsMap.beginTime != \"\" ' > and createTime &gt;= #{paramsMap.beginTime}</if>",
            "<if test='paramsMap.endTime != null and paramsMap.endTime != \"\" ' > and createTime &lt;= #{paramsMap.endTime}</if>",
            "</where>", "ORDER BY createTime DESC", "</script>" })
    List<Map<String, Object>> listOrderUpiBlack(@Param("paramsMap") Map<String, Object> body);

    @Insert("INSERT INTO UpiBlacklist (upi, ifsc, createTime) VALUES (#{upi}, #{ifsc},#{createTime})")
    int addIfscBlackInfo(Map<String, Object> body);

    @Select("SELECT count(*) FROM UpiBlacklist WHERE ifsc=#{ifsc}")
    Long countIfscBlackInfo(@Param("ifsc") String ifsc);

    @Select("SELECT * FROM UpiBlacklistLog WHERE payConfigId in (3873,3939) and status=0")
    List<Map<String, Object>> selectOrderUpiBlackByInit();

    @Update("UPDATE UpiBlacklistLog SET status=#{status}, updateTime=#{updateTime} WHERE id=#{id}")
    int updateUpiBlackLog(Map<String, Object> upiMap);

    @Select("SELECT * FROM ifsc_temp")
    List<Map<String, Object>> selectIfscTempAll();

    @Delete("DELETE FROM ifsc_temp")
    int deleteIfscTempAll();

    @Select("select upi from UpiBlacklist")
    List<String> allUpiBlacklist();

    @Select("select count(1) from UpiBlacklist where upi like concat(#{upiAcc}, '%')")
    Long countUpiBlack(@Param("upiAcc") String upiAcc);

    @Select("select count(1) from UpiBlacklist where ifsc=#{ifsc}")
    Long countIfscBlack(String ifsc);

    @Select("select count(1) from ifsc_info where ifsc=#{ifsc}")
    Long countIfscInfo(String ifsc);

    @Insert("INSERT INTO ifsc_info (ifsc, address, bankName, branch, city, district, phone, pincode, state) VALUES (#{ifsc}, #{address},#{bankName}," +
            "#{branch}, #{city},#{district},#{phone}, #{pincode},#{state})")
    int addIfscInfo(Map<String, Object> body);

    @Select("select * from ifsc_info where ifsc=#{ifsc}")
    Map<String, Object> getIfscInfo(String ifsc);

    @Insert("INSERT INTO ifsc_temp (ifsc) VALUES (#{ifsc})")
    int addIfscTemp(String ifsc);
}
