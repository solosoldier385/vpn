package com.letsvpn.pay.service.cn;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
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
import com.letsvpn.pay.util.PayConfigChannelExtractType;
import com.letsvpn.pay.util.PayHttpType;
import com.letsvpn.pay.vo.PayResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A8支付
 *
 * @author Daniel
 */
@Service
@Slf4j
public class A8PayService extends BaseThirdService implements IPayThirdRequest, IPayNotifyHandle {


    @SuppressWarnings("deprecation")
    @Override
    public PayResultData executeReq(MerchantInfo merchantInfo, PayConfigInfo payConfigInfo, PayConfigChannel channel,
                                    Map<String, String> param, List<PayConfigParameter> payConfigParameters) {
        if (payConfigInfo == null)
            return null;

        log.debug("-----------------------");
        StringBuffer logText = new StringBuffer();
        logText.append(JSONUtil.toJsonStr(merchantInfo)).append("\n");
        PayCallMethod callmethod = PayCallMethod.valueOf(payConfigInfo.getCallMethod());
        PayResultData result = new PayResultData(callmethod);

        Map<String, String> formData = new HashMap<String, String>();
        ThirdUtil.extracted2(param, payConfigParameters, formData, logText);
        log.debug("---------------------1-");
        for (Map.Entry<String, String> payReqParam : formData.entrySet()) {
            log.debug("{}", payReqParam);
        }
        log.debug("--------------------2--");
        String reqLink = payConfigInfo.getUrl();

        Map<String, Object> formData1 = new HashMap<String, Object>();
        for (Map.Entry<String, String> payReqParam : formData.entrySet()) {
            formData1.put(payReqParam.getKey(), payReqParam.getValue());
        }

        long now = System.currentTimeMillis();
        String resultText = null;
        try {
            log.debug("reqLink:{}", reqLink);
            if (StrUtil.isEmpty(payConfigInfo.getHttpType())) {
                payConfigInfo.setHttpType(PayHttpType.form_data.name());
            }
            PayHttpType httpType = PayHttpType.valueOf(payConfigInfo.getHttpType());

            HttpRequest hr = HttpRequest.post(reqLink).timeout(ThirdUtil.HTTP_REQUEST_TIMEOUT);// 超时，毫秒

            hr = hr.form(formData1);
            resultText = hr.executeAsync().body();
            log.debug("resultText:{}", resultText);
            String etype = channel.getExtractType();
            if (StrUtil.isEmpty(etype)) {
                etype = PayConfigChannelExtractType.html.name();
            }
            result.setMethod(PayCallMethod.valueOf(payConfigInfo.getCallMethod()));
            result.setHtml(resultText);
        } catch (WanliException e) {
            log.error(e.getMessage(), e);
           add(merchantInfo.getPlatformId(), payConfigInfo.getId(), channel.getId(),
                    channel.getTitle(), e.getMessage(), resultText, this.getClass().getSimpleName());
            if (payConfigInfo.getTest() == 1) {
                throw e;
            } else {
                long reqTime = System.currentTimeMillis() - now;
                extracted(merchantInfo, payConfigInfo, channel, reqLink, formData1, resultText, reqTime, e.getMessage(),
                        logText, 1);
                throw new WanliException(5011,"该支付异常1,请换其他支付");
            }
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

    }

    @Override
    public boolean signCheck(Map<String, String> paramsMap, List<PayConfigParameter> payConfigParameters,
                             StringBuffer logText) {
        Map<String, String> formData = new HashMap<String, String>();
        ThirdUtil.extracted2(paramsMap, payConfigParameters, formData, logText);
        logText.append(formData).append("\n\n");

        for (PayConfigParameter payConfigParameter : payConfigParameters) {
            log.debug("payConfigParameters{}", JSONUtil.toJsonStr(payConfigParameter));
            String payName = payConfigParameter.getPayName();
            String paramValue = paramsMap.get(payName);
            log.debug("payName:{}, paramValue:{}", payName, paramValue);
            log.debug("formData:{}", formData);
            logText.append(payName).append(":").append(paramValue).append("\n\n");

            if (!MapUtil.getStr(formData, payName, "").equalsIgnoreCase(paramValue)) {
                return false;
            }
        }
        log.debug("paramsMap{}", paramsMap);
        return true;

    }
}