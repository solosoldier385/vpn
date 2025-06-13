package com.letsvpn.pay.mapper.ext;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface ExtPayConfigLimitMapper {

    @Select("select * from PayConfigLimit where  payConfigId = #{payConfigId} and status=0")
    Map<String, Object> searchPayConfigLimitById(@Param("payConfigId") Integer payConfigId);

    @Update("UPDATE PayConfigLimit SET  status = #{status}, endTime = #{endTime}, totalAmount = #{totalAmount}, remark = #{remark} WHERE payConfigId = #{payConfigId}")
    int updatePayConfigLimitById(Map<String, Object> limitMap);

    @Update("UPDATE PayConfigLimit SET totalAmount = totalAmount + #{realAmount} WHERE payConfigId = #{payConfigId}")
    int updatePayConfigLimitAddAmount(Map<String, Object> limitMap);

    @Select({ "<script>", "SELECT * FROM  PayConfigLimit ",
            "<where> ",
            "<if test='payConfigId != null and payConfigId != \"\" ' > and payConfigId=#{payConfigId}</if>",
            "</where>", "order by beginTime desc", "</script>" })
    List<Map<String, Object>> searchtPayConfigLimitByParams(Map<String, String> body);

    @Select({ "<script>", "SELECT * FROM  PayConfigLimitRecord ",
            "<where> ",
            "<if test='payConfigId != null and payConfigId != \"\" ' > and payConfigId=#{payConfigId}</if>",
            "</where>", "order by id desc", "</script>" })
    List<Map<String, Object>> searchtLimitRecordByParams(Map<String, String> body);

    @Insert("INSERT INTO PayConfigLimit (payConfigId, limitAmount, beginTime, remark) VALUES "
            + "(#{payConfigId}, #{limitAmount}, #{beginTime}, #{remark});")
    int addPayConfigLimit(Map<String, Object> body);

    @Select("select * from PayConfigLimit where status in (-1, 1)")
    List<Map<String, Object>> searchPayConfigLimitByWarning();

    @Insert("INSERT INTO PayConfigLimitRecord (payConfigId, limitAmount, totalAmount, status, beginTime, endTime, remark) VALUES "
            + "(#{payConfigId}, #{limitAmount}, #{totalAmount}, #{status}, #{beginTime}, #{endTime}, #{remark});")
    @SelectKey(keyProperty = "id", resultType = Long.class, before = false, statement = "SELECT LAST_INSERT_ID()")
    int addPayConfigLimitRecord(Map<String, Object> body);

    @Delete("DELETE FROM PayConfigLimit WHERE payConfigId = #{payConfigId}")
    int deletePayConfigLimit(Integer payConfigId);
}
