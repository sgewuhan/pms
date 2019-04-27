package com.bizvisionsoft.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.bson.Document;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.model.ProcessDefinition;
import com.mongodb.BasicDBObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/bpm/")
@Api("/bpm/")
public interface BPMService {

	@POST
	@Path("/resource/bpmn/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询工作流定义", response = Document.class, responseContainer = "List")
	@DataSet("BPMN资源列表/" + DataSet.LIST)
	public List<Document> listResources(@MethodParam(MethodParam.CONDITION) BasicDBObject condition);

	@POST
	@Path("/resource/bpmn/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询工作流定义数量", response = Document.class, responseContainer = "List")
	@DataSet({ "BPMN资源列表/" + DataSet.COUNT })
	public long countResources(@MethodParam(MethodParam.FILTER) BasicDBObject filter);
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/resource/bpmn/authorized/{userId}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "用户工作流定义列表/" + DataSet.LIST })
	public List<ProcessDefinition> listFunctionRoles(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	@POST
	@Path("/resource/bpmn/authorized/{userId}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "用户工作流定义列表/" + DataSet.COUNT })
	public long countFunctionRoles(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/process/start/{processId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Long startProcess(Document parameter, @PathParam("processId") String processId);
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/userId/{userId}/planned/card/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我的流程任务（已分配）/list")
	public List<Document> listTaskCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userId,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/userId/{userId}/planned/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我的流程任务（已分配）/count")
	public long countTaskCard(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);
}
