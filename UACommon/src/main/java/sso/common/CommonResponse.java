package sso.common;

public class CommonResponse {

	private String code;

	private String msg;

	private Object data;

	private String referUrl;

	public static CommonResponse fail() {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setCode("0");
		commonResponse.setMsg("操作失败");
		commonResponse.setData(null);
		return commonResponse;
	}

	public static CommonResponse fail(String msg) {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setCode("0");
		commonResponse.setMsg(msg);
		commonResponse.setData(null);
		return commonResponse;
	}
	
	public static CommonResponse fail(Object data) {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setCode("0");
		commonResponse.setMsg("操作失败");
		commonResponse.setData(data);
		return commonResponse;
	}
	
	public static CommonResponse failTokenIsBlank() {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setCode(CommonConstant.CODE_TOKEN_IS_BLANK);
		commonResponse.setMsg("令牌为空");
		return commonResponse;
	}
	
	public static CommonResponse failTokenNotExists() {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setCode(CommonConstant.CODE_TOKEN_NOT_EXIST);
		commonResponse.setMsg("令牌不存在");
		return commonResponse;
	}
	

	
	public static CommonResponse failTokenExpires() {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setCode(CommonConstant.CODE_TOKEN_EXPIRES);
		commonResponse.setMsg("令牌过期");
		return commonResponse;
	}
	
	public static CommonResponse failTokenExpires(String referUrl) {
		CommonResponse commonResponse = failTokenExpires();
		commonResponse.setReferUrl(referUrl);
		return commonResponse;
	}
	
	public static CommonResponse failNoAccess() {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setCode(CommonConstant.CODE_NO_ACCESS);
		commonResponse.setMsg("无权限");
		return commonResponse;
	}

	public static CommonResponse success(Object data) {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setCode("1");
		commonResponse.setMsg("操作成功");
		commonResponse.setData(data);
		return commonResponse;
	}
	
	public static CommonResponse success() {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setCode("1");
		commonResponse.setMsg("操作成功");
		commonResponse.setData(null);
		return commonResponse;
	}

	public static CommonResponse build(String code, String msg, Object data, String referUrl) {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.msg = msg;
		commonResponse.data = data;
		commonResponse.code = code;
		commonResponse.referUrl = referUrl;
		return commonResponse;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Object getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getReferUrl() {
		return referUrl;
	}

	public void setReferUrl(String referUrl) {
		this.referUrl = referUrl;
	}

}
