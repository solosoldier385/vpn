package com.letsvpn.pay.dto;


import com.letsvpn.pay.vo.EmailPollResult;
import com.letsvpn.pay.vo.PayConfigEmailPoll;

public interface IEmailPollRequest {

	EmailPollResult executeEmailPollReq(String transactionId, PayConfigEmailPoll payConfigEmailPoll);

}
