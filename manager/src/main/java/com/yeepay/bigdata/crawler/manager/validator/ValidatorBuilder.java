package com.yeepay.bigdata.crawler.manager.validator;

public class ValidatorBuilder {

	public static CompositeValidator detailValidator = new CompositeValidator();
	static {
		initDetailValidator();
	}

	static void initDetailValidator() {
		detailValidator.addValidator(new RequiredValidator());
	}

	public static ExtractorValidator getDetailValidator() {
		return detailValidator;
	}

}
