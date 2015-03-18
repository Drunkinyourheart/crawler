package com.yeepay.bigdata.crawler.manager.validator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

class CompositeValidator implements ExtractorValidator {

	public List<ExtractorValidator> validators = new CopyOnWriteArrayList<ExtractorValidator>();

	public void addValidator(ExtractorValidator validator) {
		validators.add(validator);
	}

	@Override
	public ValidateResult validate(Map<String, Object> map) {
		ValidateResult result = new ValidateResult();
		for (ExtractorValidator validator : validators) {
			result = validator.validate(map);
			if (!result.isValid()) {
				return result;
			}
		}
		return result;
	}

}
