package com.coinwallet.common.sms;
//
//import com.taobao.api.DefaultTaobaoClient;
//import com.taobao.api.TaobaoClient;
//import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
//import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;

public class SmsAlidayuUtil {
	
	 private String url="http://gw.api.taobao.com/router/rest";
	//private String url="http://gw.api.tbsandbox.com/router/rest";
	
	private String appkey = "23420431";
	//private String appkey = "1023320690";

	private String secret = "63ef006616404c82bdb4bd9f6aa5467d";
	//private String secret = "sandboxaa5bb1c7a3899434b3c46cdab";
	
	private static SmsAlidayuUtil instance = new SmsAlidayuUtil();

	private SmsAlidayuUtil() {
		
	}
	
	public static SmsAlidayuUtil getInstance() {
		if (instance != null) {
			return instance;
		}
		return new SmsAlidayuUtil();
	}
//
//	public void send(String mobile,String verifycode) throws Exception {
//		TaobaoClient client = new DefaultTaobaoClient(url, appkey, secret);
//		AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
//		//req.setExtend("123456");
//		req.setSmsType("normal");
//		req.setSmsFreeSignName("登录验证");
//		req.setSmsParamString("{\"code\":\""+verifycode+"\",\"product\":\"【旧爱勾搭】\"}");
//		req.setRecNum(mobile);
//		req.setSmsTemplateCode("SMS_11325426");
//		AlibabaAliqinFcSmsNumSendResponse rsp = client.execute(req);
//		System.out.println(rsp.getBody());
//	}
	
	//18602104515
	public static void main(String[] args) {
		try {
//			SmsAlidayuUtil.getInstance().send("18616208325", "2345352");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
