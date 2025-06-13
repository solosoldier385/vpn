package com.letsvpn.pay.mapper.core;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ExtOrderPayPushPanMapper {

	@Insert("INSERT INTO `OrderPayPushPan` (`id`, `createTime`) VALUES (#{id}, now())")
	int addOrderPayPushPan(@Param("id") Long id);

	@Select("SELECT id FROM `OrderPayPushPan` group by id  ")
	List<Long> groupOrderPayPushPan();

	@Update("delete from OrderPayPushPan where id=#{id}")
	int delOrderPayPushPan(@Param("id") Long id);
}
