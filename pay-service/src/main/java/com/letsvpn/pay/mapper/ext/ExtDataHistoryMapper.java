package com.letsvpn.pay.mapper.ext;


import com.letsvpn.pay.entity.OrderCallback;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface ExtDataHistoryMapper {

	@Update("${sql}")
	int updateSql(@Param("sql") String sql);

	@Select("${sql}")
	List<OrderCallback> querySql(@Param("sql") String sql);

	@Select("${sql}")
	List<Map<String, Object>> querySqlObj(@Param("sql") String sql);
}
