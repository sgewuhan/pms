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

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.service.model.ResourceUsage;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkLink;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.service.model.Workspace;
import com.mongodb.BasicDBObject;

@Path("/work")
public interface WorkService {

	@POST
	@Path("/gantt/tasks")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> createTaskDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject condition);

	@POST
	@Path("/gantt/links")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkLink> createLinkDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject condition);

	@POST
	@Path("/task/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Work insertWork(Work work);

	@POST
	@Path("/link/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkLink insertLink(WorkLink link);

	@PUT
	@Path("/task/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我的工作/" + DataSet.UPDATE)
	public long updateWork(BasicDBObject filterAndUpdate);

	@PUT
	@Path("/link/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateLink(BasicDBObject filterAndUpdate);

	@DELETE
	@Path("/task/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteWork(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/link/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteLink(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/task/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.INPUT)
	public Work getWork(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@GET
	@Path("/link/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkLink getLink(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/project_id/{project_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> listProjectRootTask(@PathParam("project_id") ObjectId project_id);

	@GET
	@Path("/project_id/{project_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countProjectRootTask(@PathParam("project_id") ObjectId project_id);

	@GET
	@Path("/parent_id/{parent_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> listChildren(@PathParam("parent_id") ObjectId parent_id);

	@GET
	@Path("/parent_id/{parent_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countChildren(@PathParam("parent_id") ObjectId parent_id);

	@GET
	@Path("/_id/{_id}/action/startstage/{executeBy}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> startStage(@PathParam("_id") ObjectId _id, @PathParam("executeBy") String executeBy);

	@GET
	@Path("/workspace/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Workspace getWorkspace(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/parent_id/{parent_id}/ganttlinks")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkLink> createWorkLinkDataSet(@PathParam("parent_id") ObjectId parent_id);

	@POST
	@Path("/parent_id/{parent_id}/gantttasks")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> createWorkTaskDataSet(@PathParam("parent_id") ObjectId parent_id);

	@POST
	@Path("/project_id/{project_id}/ganttlinks")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkLink> createProjectLinkDataSet(@PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/project_id/{project_id}/gantttasks")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> createProjectTaskDataSet(@PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/userid/{userid}/processing/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的工作/list", "我的工作（日历牌）/list" })
	public List<Work> createProcessingWorkDataSet(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@GET
	@Path("/userid/{userid}/processing/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我的工作/count")
	public long countProcessingWorkDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/finished/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我的工作（已完成）/list")
	public List<Work> createFinishedWorkDataSet(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@GET
	@Path("/userid/{userid}/finished/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我的工作（已完成）/count")
	public long countFinishedWorkDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/package/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkPackage> listWorkPackage(BasicDBObject condition);

	@POST
	@Path("/package/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countWorkPackage(BasicDBObject filter);

	@POST
	@Path("/package/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkPackage insertWorkPackage(WorkPackage wp);

	@DELETE
	@Path("/package/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteWorkPackage(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/userid/{userid}/deptuserwork/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> createDeptUserWorkDataSet(@PathParam("userid") String userid);

	@GET
	@Path("/_id/{_id}/action/distribute/{executeBy}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> distributeWorkPlan(@PathParam("_id") ObjectId _id, @PathParam("executeBy") String executeBy);

	@GET
	@Path("/_id/{_id}/action/startwork")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> startWork(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/_id/{_id}/action/finishwork")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> finishWork(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/_id/{_id}/action/finishstage/{executeBy}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> finishStage(@PathParam("_id") ObjectId _id, @PathParam("executeBy") String executeBy);

	@GET
	@Path("/_id/{_id}/action/closestage/{executeBy}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> closeStage(@PathParam("_id") ObjectId _id, @PathParam("executeBy") String executeBy);

	@POST
	@Path("/resource/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ResourceUsage addResource(ResourceUsage res);

	
	@POST
	@Path("/_id/{_id}/resource/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ResourceUsage> listResource(@PathParam("_id")  ObjectId _id);

}
