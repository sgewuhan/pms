package com.bizvisionsoft.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.model.ProcessDefinition;
import com.bizvisionsoft.service.model.TaskDefinition;
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
	@ApiOperation(value = "��ѯ����������", response = Document.class, responseContainer = "List")
	@DataSet("BPMN��Դ�б�/" + DataSet.LIST)
	public List<Document> listResources(@MethodParam(MethodParam.CONDITION) BasicDBObject condition);

	@POST
	@Path("/resource/bpmn/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ��������������", response = Long.class)
	@DataSet({ "BPMN��Դ�б�/" + DataSet.COUNT })
	public long countResources(@MethodParam(MethodParam.FILTER) BasicDBObject filter);
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	@GET
	@Path("/processDef/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ù���������", response = ProcessDefinition.class)
	@DataSet(DataSet.INPUT)
	public ProcessDefinition getProcessDefinition(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/processDef/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ����������", response = ProcessDefinition.class, responseContainer = "List")
	@DataSet({ "�����������б�/list" })
	public List<ProcessDefinition> listProcessDefinitions(@MethodParam(MethodParam.CONDITION) BasicDBObject condition);

	@POST
	@Path("/processDef/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ��������������", response = Long.class)
	@DataSet({ "�����������б�/count" })
	public long countProcessDefinitions(@MethodParam(MethodParam.FILTER) BasicDBObject filter);

	@PUT
	@Path("/processDef/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "���¹���������", response = Long.class)
	@DataSet({ "�����������б�/update" })
	public long updateProcessDefinitions(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu);
	
	@PUT
	@Path("/taskDef/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "����������", response = Long.class)
	public long updateTaskDefinitions(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu);
	
	@POST
	@Path("/processDef/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "���빤��������", response = Long.class)
	@DataSet("�����������б�/insert")
	public ProcessDefinition insertProcessDefinition(@MethodParam(MethodParam.OBJECT) ProcessDefinition p);
	
	@POST
	@Path("/taskDef/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "����������", response = Long.class)
	public TaskDefinition insertTaskDefinition(TaskDefinition td);

	@DELETE
	@Path("/processDef/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "ɾ������������", response = Long.class)
	@DataSet("�����������б�/delete")
	public long deleteProcessDefinition(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);
	
	@DELETE
	@Path("/taskDef/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "ɾ��������", response = Long.class)
	public long deleteTaskDefinition(@PathParam("_id") ObjectId _id);
	
	@POST
	@Path("/processDef/{_id}/taskDef/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ�����������µ��˹���������", response = Long.class)
	public long countTaskDefinitions(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/processDef/{_id}/taskDef/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ�����������µ��˹�����", response = TaskDefinition.class, responseContainer = "List")
	public List<TaskDefinition> listTaskDefinitions(@PathParam("_id") ObjectId _id);
	
	@POST
	@Path("/taskDef/taskId/{taskId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��������id��ѯ������", response = TaskDefinition.class)
	public TaskDefinition getTaskDefinitionByTaskId(@PathParam("taskId") long taskId);
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/processDef/authorized/{userId}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ���û���Ȩ�����Ĺ���������", response = ProcessDefinition.class, responseContainer = "List")
	@DataSet("�û������������б�/list")
	public List<ProcessDefinition> listProcessDefinitionByFunctionRoles(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	@POST
	@Path("/processDef/authorized/{userId}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ���û���Ȩ�����Ĺ�������������", response = Long.class)
	@DataSet("�û������������б�/count")
	public long countProcessDefinitionByFunctionRoles(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/process/start/{processId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "����������", response = Long.class)
	public Long startProcess(Document parameter, @PathParam("processId") String processId);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/task/assigned/{userId}/card/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ�û�����������(�Ѵ�����׼���к��ѳ���)", response = Document.class, responseContainer = "List")
	@DataSet("�ҵ��������񣨴�������/list")
	public List<Document> listTasksAssignedAsPotentialOwnerCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/task/assigned/{userId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ�û���������������(�Ѵ�����׼���к��ѳ���)", response = Long.class)
	@DataSet("�ҵ��������񣨴�������/count")
	public long countTasksAssignedAsPotentialOwnerCard(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	@POST
	@Path("/task/wip/{userId}/card/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ�û�����������(������)", response = Document.class, responseContainer = "List")
	@DataSet("�ҵ��������񣨽����У�/list")
	public List<Document> listTasksInProgressCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/task/wip/{userId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ�û���������������(������)", response = Long.class)
	@DataSet("�ҵ��������񣨽����У�/count")
	public long countTasksInProgressCard(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	@POST
	@Path("/task/closed/{userId}/card/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ�û�����������(�ѹرգ���ɡ�ʧ�ܡ������˳�������)", response = Document.class, responseContainer = "List")
	@DataSet("�ҵ����������ѹرգ�/list")
	public List<Document> listTasksClosedCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/task/closed/{userId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ�û���������������(�ѹرգ���ɡ�ʧ�ܡ������˳�������)", response = Long.class)
	@DataSet("�ҵ����������ѹرգ�/count")
	public long countTasksClosedCard(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	@POST
	@Path("/task/suspended/{userId}/card/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ�û�����������(����ͣ)", response = Document.class, responseContainer = "List")
	@DataSet("�ҵ�������������ͣ��/list")
	public List<Document> listTasksSuspendedCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/task/suspended/{userId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ�û���������������(����ͣ)", response = Long.class)
	@DataSet("�ҵ�������������ͣ��/count")
	public long countTasksSuspendedCard(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	@PUT
	@Path("/task/{taskId}/resume/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "������ͣ������", response = Boolean.class)
	public boolean resumeTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId);

	@PUT
	@Path("/task/{taskId}/stop/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "ֹͣ����", response = Boolean.class)
	public boolean stopTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId);

	@PUT
	@Path("/task/{taskId}/suspend/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ͣ����", response = Boolean.class)
	public boolean suspendTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId);

	@PUT
	@Path("/task/{taskId}/forward/{userId}/{targetUserId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "�˻�����", response = Boolean.class)
	public boolean forwardTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId,
			@PathParam("targetUserId") String targetUserId);

	@PUT
	@Path("/task/{taskId}/start/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ʼ����", response = Boolean.class)
	public boolean startTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId);

	@PUT
	@Path("/task/{taskId}/claim/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��������", response = Boolean.class)
	public boolean claimTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId);

	@PUT
	@Path("/task/{taskId}/exit/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "�˳�����", response = Boolean.class)
	public boolean exitTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId);

	@PUT
	@Path("/task/{taskId}/skip/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��������", response = Boolean.class)
	public boolean skipTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId);

	@PUT
	@Path("/task/{taskId}/delegate/{userId}/{targetUserId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "ί������", response = Boolean.class)
	public boolean delegateTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId,
			@PathParam("targetUserId") String targetUserId);

	@PUT
	@Path("/task/{taskId}/complete/{userId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "�������", response = Boolean.class)
	public boolean completeTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId, Document parameters);

	@PUT
	@Path("/task/{taskId}/nominate/{userId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "ָ������", response = Boolean.class)
	public boolean nominateTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId, List<String> potentialOwnersUserId);

	@GET
	@Path("/task/{taskId}/processInstance/vars/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "�������ʵ���ı���", response = Document.class)
	public Document getProcessInstanceVariablesByTaskId(@PathParam("taskId") long taskId);

	@GET
	@Path("/processInstance/{processInstanceId}/vars/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "�������ʵ���ı���", response = Document.class)
	public Document getProcessInstanceVariables(@PathParam("processInstanceId") long processInstanceId);

	@GET
	@Path("/task/{taskId}/nodeInfo/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��������������������", response = Document.class)
	public Document getTaskNodeInfo(@PathParam("taskId") long taskId);

}