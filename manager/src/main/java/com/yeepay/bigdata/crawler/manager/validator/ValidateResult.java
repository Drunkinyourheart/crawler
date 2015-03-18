package com.yeepay.bigdata.crawler.manager.validator;

public class ValidateResult {

	private boolean isValid = true;

	private String msg = "ok";

	public ValidateResult() {
		super();
	}

	public ValidateResult(boolean isValid, String msg) {
		super();
		this.isValid = isValid;
		this.msg = msg;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "ValidateResult [isValid=" + isValid + ", msg=" + msg + "]";
	}

}
