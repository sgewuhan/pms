package com.bizvisionsoft.service.valueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class HtmlFieldValueExtracter extends CommonFieldExtracter {

	public HtmlFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
