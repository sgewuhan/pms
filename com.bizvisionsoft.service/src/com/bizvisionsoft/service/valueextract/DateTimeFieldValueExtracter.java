package com.bizvisionsoft.service.valueextract;

import org.bson.Document;

import com.bizvisionsoft.service.exporter.ExportableFormField;

public class DateTimeFieldValueExtracter extends CommonFieldExtracter {

	public DateTimeFieldValueExtracter(Document data, ExportableFormField f) {
		super(data, f);
	}

}
