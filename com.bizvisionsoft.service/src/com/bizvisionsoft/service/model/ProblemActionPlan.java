package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.SelectionValidation;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.annotations.ui.common.MethodParam;

@PersistenceCollection("problemActionPlan")
public class ProblemActionPlan {

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "�ж�Ԥ��";

	@ReadValue
	@WriteValue
	private ObjectId _id;
	
	@ReadValue
	@WriteValue
	private String id;
	
	@ReadValue
	@WriteValue
	private List<String> stage;
	
	@ReadValue("stageInfo")
	private String readStageInfo() {
		if(stage!=null) {
			return stage.stream().map(s->"<span class='layui-badge layui-bg-blue'>"+s.charAt(0)+"</span>&nbsp;").reduce(String::concat).orElse("");
		}else {
			return "";
		}
		
	}
	
	@ReadValue
	@WriteValue
	private List<ClassifyProblem> classifyProblems;
	
	@ReadValue
	@WriteValue
	private String usage;
	
	@ReadValue
	@WriteValue
	private boolean applicable;

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue
	@WriteValue
	private String action;

	@ReadValue
	@WriteValue
	private String detail;

	@ReadValue
	@WriteValue
	private String objective;

	public String getAction() {
		return action;
	}

	public String getDetail() {
		return detail;
	}

	public String getObjective() {
		return objective;
	}
	
	@SelectionValidation("classifyProblems")
	private boolean classifyProblemsSelectable(@MethodParam(MethodParam.OBJECT) ClassifyProblem elem) {
		return elem.isLeaf;
	}

}
