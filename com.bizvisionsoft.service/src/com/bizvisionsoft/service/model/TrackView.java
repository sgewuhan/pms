package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("trackView")
public class TrackView {

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String catagory;

	public String getCatagory() {
		return catagory;
	}

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue("text")
	public String getName() {
		return name;
	}

	@ReadValue
	@WriteValue
	private String viewAssembly;

	public String getViewAssembly() {
		return viewAssembly;
	}

	@ReadValue
	@WriteValue
	private String packageAssembly;

	public String getPackageAssembly() {
		return packageAssembly;
	}

	@ReadValue
	@WriteValue
	private String editAssembly;

	public String getEditAssembly() {
		return editAssembly;
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "视图和工作包";

	@Override
	@Label
	public String toString() {
		return catagory + "/" + name;
	}

	public ObjectId get_id() {
		return _id;
	}
	
}
