package com.letsvpn.pay.service.cn;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.letsvpn.pay.dto.IPayNotifyHandle;
import com.letsvpn.pay.dto.IPayThirdRequest;
import com.letsvpn.pay.entity.MerchantInfo;
import com.letsvpn.pay.entity.PayConfigChannel;
import com.letsvpn.pay.entity.PayConfigInfo;
import com.letsvpn.pay.entity.PayConfigParameter;
import com.letsvpn.pay.exception.WanliException;
import com.letsvpn.pay.third.BaseThirdService;
import com.letsvpn.pay.third.util.ThirdUtil;
import com.letsvpn.pay.util.PayCallMethod;
import com.letsvpn.pay.vo.PayResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 百利支付 id 1342
 *
 * @author xiaoyu
 */
@Service
@Slf4j
public class BailinewPayService extends BaseThirdService implements IPayThirdRequest, IPayNotifyHandle {


    @Override
    public PayResultData executeReq(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, PayConfigChannel channel, Map<String, String> param, List<PayConfigParameter> payConfigParameters) {
        StringBuffer logText = new StringBuffer();
        logText.append(JSONUtil.toJsonStr(merchantInfo)).append("\n");
        PayCallMethod callmethod = PayCallMethod.valueOf(payConfigInfo.getCallMethod());
        PayResultData result = new PayResultData(callmethod);
        Map<String, String> formData = new HashMap<String, String>();
        ThirdUtil.extracted2(param, payConfigParameters, formData, logText);
        Map<String, Object> formData1 = new HashMap<String, Object>();
        formData1.putAll(formData);
        String appId = merchantInfo.getAppId();    //商户号
        String reqLink = payConfigInfo.getUrl();                     //网关

        long now = System.currentTimeMillis();
        String resultText = null;
        try {
            StringBuffer sb = new StringBuffer();
            for (Map.Entry<String, String> payReqParam : formData.entrySet()) {
                sb.append(payReqParam.getKey() + "=" + payReqParam.getValue() + "&");
            }
            result.setLink(resultText);
            resultText = "";
            resultText = HttpRequest.post(result.getLink()).timeout(ThirdUtil.HTTP_REQUEST_TIMEOUT).executeAsync().body();
            JSONObject json = JSONUtil.parseObj(resultText);
            String[] field = channel.getExtractField().split("\\.");
            String redirect = null;
            if (field.length == 1) {
                redirect = json.getStr(field[0]);
            } else if (field.length == 2) {
                redirect = json.getJSONObject(field[0]).getStr(field[1]);
            } else if (field.length == 3) {
                redirect = json.getJSONObject(field[0]).getJSONObject(field[1]).getStr(field[2]);
            } else {
                redirect = "null?";
            }
            if (StrUtil.isEmpty(redirect)) {
                throw new WanliException(resultText);
            }
            result.setLink(redirect);
        } catch (Exception e) {
            add(merchantInfo.getPlatformId(), payConfigInfo.getId(), channel.getId(),
                    channel.getTitle(), e.getMessage(), resultText, this.getClass().getSimpleName());
            long reqTime = System.currentTimeMillis() - now;
            extracted(merchantInfo, payConfigInfo, channel, reqLink, formData1, resultText, reqTime, e.getMessage(),
                    logText, 1);
            log.error(e.getMessage(), e);
            throw new WanliException(5012,"该支付异常2,请换其他支付");
        }
        long reqTime = System.currentTimeMillis() - now;
        extracted(merchantInfo, payConfigInfo, channel, reqLink, formData1, resultText, reqTime, "", logText, 0);

        return result;
    }

    @Override
    public void paramExtraction(Map<String, String> paramMap, MerchantInfo merchantInfo) {
        String jsonMap = paramMap.get("data");
        if (!StrUtil.isEmpty(jsonMap)) {
            JSONObject jsonObject = JSONUtil.parseObj(jsonMap);
            paramMap.put("amount", jsonObject.getStr("amount"));
            paramMap.put("appKey", jsonObject.getStr("appKey"));
            paramMap.put("createTime", jsonObject.getStr("createTime"));
            paramMap.put("title", jsonObject.getStr("title"));
            paramMap.put("transId", jsonObject.getStr("transId"));
            paramMap.put("transTime", jsonObject.getStr("transTime"));
            paramMap.put("type", jsonObject.getStr("transTime"));
        }

    }

    @Override
    public boolean signCheck(Map<String, String> paramsMap, List<PayConfigParameter> payConfigParameters, StringBuffer logText) {
        boolean md5true = false;
        String md5msg = "";

        md5msg = SecureUtil.md5(paramsMap.get("data") + paramsMap.get("__privateKey"));
        log.debug("paramsMap{}", paramsMap);
        log.info("md5msg：" + md5msg);
        logText.append("md5msg：" + md5msg);
        log.info("接收sig：" + paramsMap.get("sign"));
        logText.append("data：" + paramsMap.get("data"));
        if (paramsMap.get("sign").equals(md5msg)) {
            md5true = true;
        } else {
            md5true = false;
        }
        return md5true;

    }


}
