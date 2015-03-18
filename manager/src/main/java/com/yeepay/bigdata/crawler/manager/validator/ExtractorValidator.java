package com.yeepay.bigdata.crawler.manager.validator;

import java.util.Map;

public interface ExtractorValidator {

	/**
	 * validate extract object:
	 * 
	 * @param map
	 * @return
	 */
	ValidateResult validate(Map<String, Object> map);
}
