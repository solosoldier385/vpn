package com.letsvpn.pay.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class PbankReqUtil {

    //private static final String HOST_URL = "http://127.0.0.1:8071";

    private static final String HOST_URL = "https://pbankreq.wanliserver.com";


    private static String qryUtrUrl = HOST_URL + "/api/bank/queryUTR?utr=";

    public static String queryUTR(String utr){

        String queryUrl = qryUtrUrl + utr;
        HttpRequest hr = HttpRequest.get(queryUrl).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("resultText:{}-{}", utr, resultText);

        return resultText;
    }

    private static String dealtWithRecordUrl = HOST_URL + "/api/bank/dealtWithRecord";

    public static String dealtWithRecord(Map<String, Object> paramMap){

        HttpRequest hr = HttpRequest.post(dealtWithRecordUrl).form(paramMap).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("update-resultText:{}", resultText);

        return resultText;
    }

    private static String qryAllUtrUrl = HOST_URL + "/api/bank/queryUTRAll?utr=";

    public static String queryUTRAll(String utr){

        String queryUrl = qryAllUtrUrl + utr;
        HttpRequest hr = HttpRequest.get(queryUrl).timeout(20 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("resultText:{}-{}", utr, resultText);

        return resultText;
    }

    private static String searchNotDealUrl = HOST_URL + "/api/bank/searchNotDealtListByParams";

    public static String searchNotDealtListByParams(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(searchNotDealUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("searchNotDeal-resultText:{}", resultText);

        return resultText;
    }

    private static String searchChannelInfoUrl = HOST_URL + "/api/channel/queryChannelInfoMer";

    public static String queryChannelInfoMer(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(searchChannelInfoUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("searchChannelInfoUrl-resultText:{}", resultText);

        return resultText;
    }

    private static String emailInfoUrl = HOST_URL + "/api/email/queryPakEmailConfigList";

    public static String emailInfoUrlInfoMer(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(emailInfoUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("searchChannelInfoUrl-resultText:{}", resultText);

        return resultText;
    }
    private static String addEmailInfoUrl = HOST_URL + "/api/email/addPakEmailConfig";

    public static String addEmailInfo(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(addEmailInfoUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("addCardInfo-resultText:{}", resultText);

        return resultText;
    }

    private static String emailListUrl = HOST_URL + "/api/email/queryCountryTypeList";

    public static String queryEmailConfigType(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(emailListUrl).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("searchChannelInfoUrl-resultText:{}", resultText);

        return resultText;
    }



    private static String addChannelInfoUrl = HOST_URL + "/api/channel/addChannelInfo";

    public static String addChannelInfo(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(addChannelInfoUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("addChannelInfoUrl-resultText:{}", resultText);

        return resultText;
    }

    private static String updateChannelInfoUrl = HOST_URL + "/api/channel/updateChannelInfo";

    public static String updateChannelInfo(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(updateChannelInfoUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("updateChannelInfoUrl-resultText:{}", resultText);

        return resultText;
    }

    private static String updatePakEmailUrl = HOST_URL + "/api/email/updatePakEmailStu";

    public static String updatePakEmailStu(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(updatePakEmailUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("updatePakEmailStu-resultText:{}", resultText);

        return resultText;
    }

    private static String updatePakEmailConfigStu = HOST_URL + "/api/email/updatePakEmailConfigStu";

    public static String updatePakEmailConfigStu(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(updatePakEmailConfigStu).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("updatePakEmailConfigStu-resultText:{}", resultText);

        return resultText;
    }


    private static String updateChannelAmountUrl = HOST_URL + "/api/channel/updateChannelAmount";

    public static String updateChannelAmount(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(updateChannelAmountUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("updateChannelAmountUrl-resultText:{}", resultText);

        return resultText;
    }

    private static String queryPakTrxIdAllUrl = HOST_URL + "/api/bank/queryPakTrxIdAll?trxId=";

    public static String queryPakTrxIdAll(String trxId){

        String queryUrl = queryPakTrxIdAllUrl + trxId;
        HttpRequest hr = HttpRequest.get(queryUrl).timeout(20 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("resultText:{}-{}", trxId, resultText);

        return resultText;
    }

    private static String delChannelInfoUrl = HOST_URL + "/api/channel/delChannelInfo";

    public static String delChannelInfo(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(delChannelInfoUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("delChannelInfoUrl-resultText:{}", resultText);

        return resultText;
    }

    public static String saveBankAccountPakistanInfoUrl = HOST_URL + "/api/bank/saveBankAccountPakistan";

    public static String saveBankAccountPakistan(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(saveBankAccountPakistanInfoUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("saveBankAccountPakistanInfoUrl-resultText:{}", resultText);

        return resultText;
    }

    private static String selectBankAccountInfoUrl = HOST_URL + "/api/bank/selectBankAccount";

    public static String selectBankAccountInfo(JSONObject body){

        HttpRequest hr = HttpRequest.post(selectBankAccountInfoUrl).body(body.toString()).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("selectBankAccountInfoUrl-resultText:{}", resultText);

        return resultText;
    }

    private static String selectBankPakistanInfoUrl = HOST_URL + "/api/bank/selectBankPakistan";

    public static String selectBankPakistanInfo(){

        HttpRequest hr = HttpRequest.post(selectBankPakistanInfoUrl).body(new JSONObject().toString()).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("selectBankPakistanInfoUrl-resultText:{}", resultText);

        return resultText;
    }

    private static String selectBankAccounPakistantInfoUrl = HOST_URL + "/api/bank/selectBankAccounPakistant";

    public static String selectBankAccounPakistant(){

        HttpRequest hr = HttpRequest.post(selectBankAccounPakistantInfoUrl).body(new JSONObject().toString()).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("selectBankAccounPakistantInfoUrl-resultText:{}", resultText);

        return resultText;
    }

    private static String trxIdPakistanRefOrderIdInfoUrl = HOST_URL + "/api/bank/trxIdPakistanRefOrder";

    public static String trxIdPakistanRefOrderId(JSONObject body){

        HttpRequest hr = HttpRequest.post(trxIdPakistanRefOrderIdInfoUrl).body(body.toString()).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("trxIdPakistanRefOrderIdInfoUrl-resultText:{}", resultText);

        return resultText;
    }


    private static String searchPakBankCardsUrl = HOST_URL + "/api/bank/queryPakBankCards";

    public static String searchPakBankCards(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(searchPakBankCardsUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("searchPakBankCards-resultText:{}", resultText);

        return resultText;
    }

    private static String addCardInfoUrl = HOST_URL + "/api/bank/addPakBankCard";

    public static String addCardInfo(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(addCardInfoUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("addCardInfo-resultText:{}", resultText);

        return resultText;
    }

    private static String updateCardInfoUrl = HOST_URL + "/api/bank/updateCardInfo";

    public static String updateCardInfo(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(updateCardInfoUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("updateCardInfoUrl-resultText:{}", resultText);

        return resultText;
    }

    private static String updateCardAmountUrl = HOST_URL + "/api/bank/updateCardAmount";

    public static String updateCardAmount(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(updateCardAmountUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("updateCardAmount-resultText:{}", resultText);

        return resultText;
    }



    private static String delCardInfoUrl = HOST_URL + "/api/bank/delCardInfo";

    public static String delCardInfo(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(delCardInfoUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("delChannelInfoUrl-resultText:{}", resultText);

        return resultText;
    }


    private static String queryPakTranByThirdTranIdUrl = HOST_URL + "/api/bank/queryPakTranByThirdTranId?thirdTranId=";

    public static String queryPakTranByThirdTranId(String thirdTranId){

        String queryUrl = queryPakTranByThirdTranIdUrl + thirdTranId;
        HttpRequest hr = HttpRequest.get(queryUrl).timeout(20 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("resultText:{}-{}", thirdTranId, resultText);

        return resultText;
    }

    private static String searchPakTranListByParamsUrl = HOST_URL + "/api/bank/searchPakTranListByParams";

    private static String searchSimpaisaTranListByParamsUrl = "http://52.90.79.67:9088/payout/search";

    public static String searchPakTranListByParams(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(searchPakTranListByParamsUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("searchPakTranListByParamsUrl:{}-{}", body, resultText);

        return resultText;
    }

    public static String searchSimpaisaTranListByParams(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(searchSimpaisaTranListByParamsUrl).timeout(30 * 1000).body(String.valueOf(new JSONObject(body)));// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("searchSimpaisaTranListByParamsUrl:{}-{}", body, resultText);

        return resultText;
    }


    private static String updatePakTranStatusUrl = HOST_URL + "/api/bank/updatePakTranStatus";

    public static String updatePakTranStatus(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(updatePakTranStatusUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("updatePakTranStatusUrl:{}-{}", body, resultText);

        return resultText;
    }

    private static String selectReportChannelDayInfoUrl = HOST_URL + "/api/bank/selectReportChannelDay";

    public static String selectReportChannelDayInfo(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(selectReportChannelDayInfoUrl).body(new JSONObject(body).toString()).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("selectReportChannelDayInfoUrl-resultText:{}", resultText);

        return resultText;
    }

    private static String addServerIpInfoUrl = HOST_URL + "/api/hdfc/addServerIp";

    public static String addServerIpInfo(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(addServerIpInfoUrl).body(new JSONObject(body).toString()).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("addServerIpInfo:{}-{}", body, resultText);

        return resultText;
    }

    private static String queryServerIpInfoUrl = HOST_URL + "/api/hdfc/queryServerIp";

    public static String queryServerIpInfo(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(queryServerIpInfoUrl).body(new JSONObject(body).toString()).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("queryServerIpInfo-resultText:{}", resultText);

        return resultText;
    }

    private static String searchRecordHdfcListByParamsInfoUrl = HOST_URL + "/api/bank/searchRecordHdfcListByParams";

    public static String searchRecordHdfcListByParamsInfo(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(searchRecordHdfcListByParamsInfoUrl).body(new JSONObject(body).toString()).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("searchRecordHdfcListByParamsInfo-resultText:{}", resultText);

        return resultText;
    }

    private static String forceUpScorePakistanRefOrderInfoUrl = HOST_URL + "/api/bank/forceUpScorePakistanRefOrder";

    public static String forceUpScorePakistanRefOrderInfo(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(forceUpScorePakistanRefOrderInfoUrl).body(new JSONObject(body).toString()).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("forceUpScorePakistanRefOrderInfo-resultText:{}", resultText);

        return resultText;
    }

    private static String searchPkEmailConfigTypeUrl = HOST_URL + "/api/email/queryCountryTypeList";

    public static String queryEmailConfigType(){

        HttpRequest hr = HttpRequest.post(searchPkEmailConfigTypeUrl).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("searchPkEmailConfigTypeUrl-resultText:{}", resultText);

        return resultText;
    }

    private static String delEmailConfig = HOST_URL + "/api/email/delEmailConfig";

    public static String delEmailConfig(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(delEmailConfig).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("delEmailConfig-resultText:{}", resultText);

        return resultText;
    }

    private static String searchPkEmailConfigListUrl = HOST_URL + "/api/email/queryPakEmailConfigList";

    public static String queryEmailConfigList(Map<String, Object> body){

        HttpRequest hr = HttpRequest.post(searchPkEmailConfigListUrl).form(body).timeout(30 * 1000);// 超时，毫秒
        String resultText = hr.executeAsync().body();
        log.info("searchPkEmailConfigListUrl-resultText:{}", resultText);

        return resultText;
    }


}
