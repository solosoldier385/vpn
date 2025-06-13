package com.letsvpn.pay.service.core;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.letsvpn.pay.entity.PayIpWhite;
import com.letsvpn.pay.mapper.PayIpWhiteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PayIpWhiteService extends BaseService {

	@Autowired
	PayIpWhiteMapper payIpWhiteMapper;

	public void checkPayIpWhite(String ip, Integer platformId) {

		QueryWrapper<PayIpWhite> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("platform_id", platformId);
		queryWrapper.eq("ip", ip);

		PayIpWhite ipwhite = payIpWhiteMapper.selectOne(queryWrapper);
		if (ipwhite == null) {
			ipwhite = new PayIpWhite();
			ipwhite.setIpAddress(ip);
			ipwhite.setPlatformId(platformId);
			ipwhite.setStatus(2);
			ipwhite.setCreateTime(new Date());
			payIpWhiteMapper.insert(ipwhite);
		} else {
			Date now = new Date();

			Date ctime = ipwhite.getCreateTime();
			if (ctime != null) {
				if (ctime.before(DateUtil.offsetDay(now, -1))) {
					PayIpWhite re = new PayIpWhite();
					re.setIpAddress(ipwhite.getIpAddress());
					re.setPlatformId(ipwhite.getPlatformId());
					re.setCreateTime(now);

					UpdateWrapper<PayIpWhite> updateWrapper = new UpdateWrapper<>();
					updateWrapper.eq("platform_id", ipwhite.getIpAddress());
					updateWrapper.eq("ip_address", ipwhite.getIpAddress());
					payIpWhiteMapper.update(re,updateWrapper);
				}

			}

		}
		validateParam(ipwhite.getStatus() != 1, 1003, platformId + ":" + ip);
	}

}
