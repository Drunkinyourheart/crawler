package com.yeepay.bigdata.crawler.manager.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class RequiredValidator implements ExtractorValidator {

	private static final Set<String> REQUIRED_FIELDS = new HashSet<String>();
	static {
		REQUIRED_FIELDS.add("title");
		REQUIRED_FIELDS.add("content");
		REQUIRED_FIELDS.add("url");
	}

	@Override
	public ValidateResult validate(Map<String, Object> map) {
		for (String key : REQUIRED_FIELDS) {
			Object value = map.get(key);
			if (value == null) {
				return new ValidateResult(false, key + " is null");
			}
			if (StringUtils.isBlank(value.toString())) {
				return new ValidateResult(false, key + " is empty");
			}
		}
		return new ValidateResult();
	}
}
