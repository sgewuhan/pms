package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.Baseline;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLinkInfo;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.model.WorkspaceGanttData;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;

public class WorkSpaceServiceImpl extends BasicServiceImpl implements WorkSpaceService {

	@Override
	public int nextWBSIndex(BasicDBObject condition) {
		// yangjun 2018/10/31
		Document doc = c("workspace").find(condition).sort(new BasicDBObject("index", -1).append("_id", -1))
				.projection(new BasicDBObject("index", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index", 0)).orElse(0) + 1;
	}

	public WorkInfo getWorkInfo(ObjectId _id) {
		// yangjun 2018/10/31
		List<WorkInfo> list = createTaskDataSet(new BasicDBObject("_id", _id));
		if (list.size() > 0)
			return list.get(0);

		return null;
	}

	@Override
	public List<WorkInfo> createTaskDataSet(BasicDBObject condition) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(condition));
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));

		appendUserInfo(pipeline, "chargerId", "chargerInfo");

		List<Field<?>> fields = new ArrayList<Field<?>>();
		fields.add(new Field<String>("projectName", "$project.name"));
		fields.add(new Field<String>("projectNumber", "$project.id"));
		pipeline.add(Aggregates.addFields(fields));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
		// yangjun 2018/10/31
		pipeline.add(Aggregates.sort(new BasicDBObject("index", 1).append("_id", -1)));
		return c(WorkInfo.class).aggregate(pipeline).into(new ArrayList<WorkInfo>());
	}

	@Override
	public List<WorkLinkInfo> createLinkDataSet(BasicDBObject condition) {
		return c(WorkLinkInfo.class).find(condition).into(new ArrayList<WorkLinkInfo>());
	}

	@Override
	public WorkInfo insertWork(WorkInfo work) {
		return insert(work, WorkInfo.class);
	}

	@Override
	public WorkLinkInfo insertLink(WorkLinkInfo link) {
		return insert(link, WorkLinkInfo.class);
	}

	@Override
	public long updateWork(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkInfo.class);
	}

	@Override
	public long updateLink(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkLinkInfo.class);
	}

	@Override
	public long deleteWork(ObjectId _id) {
		return delete(_id, WorkInfo.class);
	}

	@Override
	public long deleteLink(ObjectId _id) {
		return delete(_id, WorkLinkInfo.class);
	}

	@Override
	public Result checkout(Workspace workspace, String userId, Boolean cancelCheckoutSubSchedule) {
		// 获取所有需检出的工作ID inputIds为需要复制到Workspace中的工作。inputIdHasWorks为需要进行校验的工作
		List<ObjectId> inputIds = new ArrayList<ObjectId>();
		List<ObjectId> inputIdHasWorks = new ArrayList<ObjectId>();
		// 判断是否为项目
		if (workspace.getWork_id() == null) {
			// 获取项目下所有工作
			inputIds = c(Work.class).distinct("_id", new BasicDBObject("project_id", workspace.getProject_id()), ObjectId.class)
					.into(new ArrayList<ObjectId>());
			inputIdHasWorks.addAll(inputIds);
		} else {
			// 获取检出工作及其下级工作
			inputIdHasWorks.add(workspace.getWork_id());
			inputIdHasWorks = getDesentItems(inputIdHasWorks, "work", "parent_id");
			// 获取检出工作的下级工作
			inputIds.addAll(inputIdHasWorks);
			inputIds.remove(workspace.getWork_id());
		}
		// 判断是否需要检查本计划被其他人员检出
		if (Boolean.TRUE.equals(cancelCheckoutSubSchedule)) {
			// 获取要检出工作及该工作下级工作的工作区id，并进行清除
			List<ObjectId> spaceIds = c("work")
					.distinct("space_id", new BasicDBObject("_id", new BasicDBObject("$in", inputIdHasWorks)), ObjectId.class)
					.into(new ArrayList<ObjectId>());
			spaceIds.add(workspace.getSpace_id());
			cleanWorkspace(spaceIds);
		} else {
			// 获取被其他人员检出的计划
			List<Bson> pipeline = new ArrayList<Bson>();
			pipeline.add(Aggregates.match(new BasicDBObject("_id", new BasicDBObject().append("$in", inputIdHasWorks)).append("checkoutBy",
					new BasicDBObject("$ne", null))));
			pipeline.add(Aggregates.lookup("user", "checkoutBy", "userId", "user"));
			pipeline.add(Aggregates.unwind("$user"));
			pipeline.add(Aggregates.project(new BasicDBObject("name", Boolean.TRUE).append("username", "$user.name")));

			BasicDBObject checkout = c("work").aggregate(pipeline, BasicDBObject.class).first();
			if (checkout != null) {
				// 如被其他人员检出,则提示给当前用户
				Result result = Result.checkoutError("计划正在进行计划编辑。", Result.CODE_HASCHECKOUTSUB);
				result.setResultDate(checkout);
				return result;
			}
		}

		// 生成工作区标识
		ObjectId space_id = new ObjectId();

		// 检出项目时，给项目标记检出人和工作区标记
		if (workspace.getWork_id() == null) {
			c("project").updateOne(new BasicDBObject("_id", workspace.getProject_id()),
					new BasicDBObject("$set", new BasicDBObject("checkoutBy", userId).append("space_id", space_id)));
		}
		// 给work集合中检出的工作增加检出人和工作区标记
		c("work").updateMany(new BasicDBObject("_id", new BasicDBObject("$in", inputIdHasWorks)),
				new BasicDBObject("$set", new BasicDBObject("checkoutBy", userId).append("space_id", space_id)));

		// 获取需检出到工作区的Work到List中,并为获取的工作添加工作区标记
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new BasicDBObject("_id", new BasicDBObject().append("$in", inputIds))));
		pipeline.add(Aggregates.addFields(new Field<ObjectId>("space_id", space_id)));
		pipeline.add(Aggregates.project(new BasicDBObject("checkoutBy", false)));
		List<Document> works = c("work").aggregate(pipeline).into(new ArrayList<Document>());
		if (works.size() > 0) {

			// 将检出的工作存入workspace集合中
			c("workspace").insertMany(works);

			// 获取检出的工作搭接关系，并存入worklinksspace集合中
			pipeline = new ArrayList<Bson>();
			pipeline.add(Aggregates.match(
					new BasicDBObject("source", new BasicDBObject("$in", inputIds)).append("target", new BasicDBObject("$in", inputIds))));
			pipeline.add(Aggregates.addFields(new Field<ObjectId>("space_id", space_id)));

			List<Document> workLinkInfos = c("worklinks").aggregate(pipeline).into(new ArrayList<Document>());

			if (workLinkInfos.size() > 0) {
				c("worklinksspace").insertMany(workLinkInfos);
			}
		}
		return Result.checkoutSuccess("检出成功。");
	}

	/**
	 * 一级管理节点负责人
	 */
	private static String CHECKIN_SETTING_FIELD_CHARGER_L1 = "asgnL1";
	/**
	 * 二级管理节点负责人
	 */
	private static String CHECKIN_SETTING_FIELD_CHARGER_L2 = "asgnL2";
	/**
	 * 三级管理节点负责人
	 */
	private static String CHECKIN_SETTING_FIELD_CHARGER_L3 = "asgnL3";
	/**
	 * 所有工作
	 */
	private static String CHECKIN_SETTING_FIELD_CHARGER_ALL = "asgnAll";

	/**
	 * 一级管理节点计划完成时间
	 */
	private static String CHECKIN_SETTING_FIELD_SCHEDULE_L1 = "scheduleL1";
	/**
	 * 二级管理节点计划完成时间
	 */
	private static String CHECKIN_SETTING_FIELD_SCHEDULE_L2 = "scheduleL2";
	/**
	 * 三级管理节点计划完成时间
	 */
	private static String CHECKIN_SETTING_FIELD_SCHEDULE_L3 = "scheduleL3";
	/**
	 * 里程碑计划完成时间
	 */
	private static String CHECKIN_SETTING_FIELD_MILESTONE = "milestone";
	/**
	 * 所有工作计划完成时间
	 */
	private static String CHECKIN_SETTING_FIELD_SCHEDULE_ALL = "scheduleAll";
	/**
	 * 项目计划开始时间
	 */
	private static String CHECKIN_SETTING_FIELD_PROJECT_START = "projectStart";

	/**
	 * 项目计划完成时间
	 */
	private static String CHECKIN_SETTING_FIELD_PROJECT_FINISH = "projectFinish";

	/**
	 * 要求、禁止
	 */
	private static String CHECKIN_SETTING_VALUE_REQUIREMENT = "1";
	/**
	 * 警告、询问
	 */
	private static String CHECKIN_SETTING_VALUE_WARNING = "2";
	/**
	 * 忽略、允许
	 */
	private static String CHECKIN_SETTING_VALUE_ALLOW = "3";
	/**
	 * 自动修改
	 */
	private static String CHECKIN_SETTING_VALUE_AUTO = "4";

	@Override
	public List<Result> schedulePlanCheck(Workspace workspace, Boolean checkManageItem) {
		ArrayList<Result> results = new ArrayList<Result>();

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// TODO 优化以下代码 整合成为一个查询
		List<ObjectId> milestones = c("workspace")
				.distinct("_id", new Document("space_id", workspace.getSpace_id()).append("milestone", true), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		if (milestones.size() > 0) {
			List<ObjectId> sources = c("worklinksspace").distinct("source",
					new Document("space_id", workspace.getSpace_id()).append("source", new Document("$in", milestones)), ObjectId.class)
					.into(new ArrayList<ObjectId>());

			List<ObjectId> targets = c("worklinksspace").distinct("target",
					new Document("space_id", workspace.getSpace_id()).append("target", new Document("$in", milestones)), ObjectId.class)
					.into(new ArrayList<ObjectId>());

			milestones.removeAll(sources);
			milestones.removeAll(targets);
			if (milestones.size() > 0) {
				ArrayList<String> name = c("workspace").distinct("fullName",
						new Document("space_id", workspace.getSpace_id()).append("_id", new Document("$in", milestones)), String.class)
						.into(new ArrayList<String>());
				results.add(Result.error("里程碑:" + Formatter.getString(name) + " 必须存在工作关联关系."));
			}
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		Project project = new ProjectServiceImpl().get(workspace.getProject_id());

		Document systemSetting = Optional.ofNullable(getSystemSetting(CHECKIN_SETTING_NAME + "@" + workspace.getProject_id().toString()))
				.orElse(new Document());

		Object setting;
		// 增加项目如果不批准，则可随意修改进度计划
		if (checkManageItem && project.getStartApproved()) {
			// 获取所有工作设置，默认为：警告
			setting = getSettingValue(systemSetting, CHECKIN_SETTING_FIELD_CHARGER_ALL, CHECKIN_SETTING_VALUE_WARNING);
			// 获取未设置负责人和参与者的节点名称
			List<Document> work = c("workspace").find(new Document("space_id", workspace.getSpace_id()).append("milestone", false)
					.append("assignerId", null).append("chargerId", null)).into(new ArrayList<>());
			if (CHECKIN_SETTING_VALUE_WARNING.equals(setting) && work.size() > 0)// 添加警告提示
				results.add(Result
						.warning("工作：" + Formatter.getString(work.stream().map(d -> d.getString("fullName")).collect(Collectors.toList()))
								+ " 没有指定负责人和指派者."));
			else if (CHECKIN_SETTING_VALUE_REQUIREMENT.equals(setting) && work.size() > 0)// 添加错误提示
				results.add(Result
						.error("工作：" + Formatter.getString(work.stream().map(d -> d.getString("fullName")).collect(Collectors.toList()))
								+ " 没有指定负责人和指派者."));
			else if (CHECKIN_SETTING_VALUE_ALLOW.equals(setting) && work.size() > 0) {// 忽略时，根据各级监控节点设置进行判断
				Map<String, Object> mLvls = new HashMap<String, Object>();
				// 获取一级监控节点设置
				setting = getSettingValue(systemSetting, CHECKIN_SETTING_FIELD_CHARGER_L1, CHECKIN_SETTING_VALUE_WARNING);
				if (!CHECKIN_SETTING_VALUE_ALLOW.equals(setting))
					mLvls.put("1", setting);

				// 获取二级监控节点设置
				setting = getSettingValue(systemSetting, CHECKIN_SETTING_FIELD_CHARGER_L2, CHECKIN_SETTING_VALUE_WARNING);
				if (!CHECKIN_SETTING_VALUE_ALLOW.equals(setting))
					mLvls.put("2", setting);

				// 获取三级监控节点设置
				setting = getSettingValue(systemSetting, CHECKIN_SETTING_FIELD_CHARGER_L3, CHECKIN_SETTING_VALUE_WARNING);
				if (!CHECKIN_SETTING_VALUE_ALLOW.equals(setting))
					mLvls.put("3", setting);

				List<String> chargerWarning = new ArrayList<String>();
				List<String> chargerError = new ArrayList<String>();
				work.forEach((Document d) -> {
					// 根据类型获取节点设置，并将其添加到相应的集合中。
					if (CHECKIN_SETTING_VALUE_WARNING.equals(mLvls.get(d.getString("manageLevel"))))
						chargerWarning.add(d.getString("fullName"));
					else if (CHECKIN_SETTING_VALUE_REQUIREMENT.equals(mLvls.get(d.getString("manageLevel"))))
						chargerError.add(d.getString("fullName"));

				});
				// 添加警告提示
				if (chargerWarning.size() > 0)
					results.add(Result.warning("工作：" + Formatter.getString(chargerWarning) + " 没有指定负责人和指派者."));
				// 添加错误提示
				if (chargerError.size() > 0)
					results.add(Result.error("工作：" + Formatter.getString(chargerError) + " 没有指定负责人和指派者."));

			}

			// 增加所有工作的判断，默认为允许
			setting = getSettingValue(systemSetting, CHECKIN_SETTING_FIELD_SCHEDULE_ALL, CHECKIN_SETTING_VALUE_ALLOW);

			List<Bson> pipeline = new ArrayList<Bson>();
			pipeline.add(Aggregates.match(new BasicDBObject("space_id", workspace.getSpace_id())));
			pipeline.add(Aggregates.lookup("work", "_id", "_id", "work"));
			pipeline.add(Aggregates.unwind("$work"));
			pipeline.add(Aggregates.project(new BasicDBObject("fullName", true).append("manageLevel", true).append("wpf",
					new BasicDBObject("$gt", new String[] { "$planFinish", "$work.planFinish" }))));
			pipeline.add(Aggregates.match(new BasicDBObject("wpf", true)));
			work = c("workspace").aggregate(pipeline).into(new ArrayList<>());
			if (CHECKIN_SETTING_VALUE_REQUIREMENT.equals(setting) && work.size() > 0)// 添加错误提示
				results.add(Result
						.error("工作：" + Formatter.getString(work.stream().map(d -> d.getString("fullName")).collect(Collectors.toList()))
								+ " 完成时间超过限定的计划完成日期."));
			else if (CHECKIN_SETTING_VALUE_WARNING.equals(setting) && work.size() > 0) {// 询问时，根据各级监控节点设置进行判断
				Map<String, Object> mLvls = new HashMap<String, Object>();
				List<String> scheduleQuestion = new ArrayList<String>();
				List<String> scheduleError = new ArrayList<String>();
				// 获取一级监控节点设置
				setting = getSettingValue(systemSetting, CHECKIN_SETTING_FIELD_SCHEDULE_L1, CHECKIN_SETTING_VALUE_ALLOW);
				if (!CHECKIN_SETTING_VALUE_ALLOW.equals(setting))
					mLvls.put("1", setting);

				// 获取二级监控节点设置
				setting = getSettingValue(systemSetting, CHECKIN_SETTING_FIELD_SCHEDULE_L2, CHECKIN_SETTING_VALUE_ALLOW);
				if (!CHECKIN_SETTING_VALUE_ALLOW.equals(setting))
					mLvls.put("2", setting);
				// 获取三级监控节点设置
				setting = getSettingValue(systemSetting, CHECKIN_SETTING_FIELD_SCHEDULE_L3, CHECKIN_SETTING_VALUE_ALLOW);
				if (!CHECKIN_SETTING_VALUE_ALLOW.equals(setting))
					mLvls.put("3", setting);
				// 获取里程碑设置
				setting = getSettingValue(systemSetting, CHECKIN_SETTING_FIELD_MILESTONE, CHECKIN_SETTING_VALUE_ALLOW);
				if (!CHECKIN_SETTING_VALUE_ALLOW.equals(setting))
					mLvls.put("milestone", setting);

				work.forEach((Document d) -> {
					Object set;
					if (d.getBoolean("milestone", false)) {
						set = mLvls.get("milestone");
					} else {
						set = mLvls.get(d.getString("manageLevel"));
					}
					// 根据类型获取节点设置，并将其添加到相应的集合中。
					if (CHECKIN_SETTING_VALUE_WARNING.equals(set))
						scheduleQuestion.add(d.getString("fullName"));
					else if (CHECKIN_SETTING_VALUE_REQUIREMENT.equals(set))
						scheduleError.add(d.getString("fullName"));
				});

				// 添加询问提示
				if (scheduleQuestion.size() > 0)
					results.add(Result.question("工作：" + Formatter.getString(scheduleQuestion) + " 完成时间超过限定的计划完成日期。"));
				// 添加错误提示
				if (scheduleError.size() > 0)
					results.add(Result.error("工作：" + Formatter.getString(scheduleError) + " 完成时间超过限定的计划完成日期。"));
			}
		}

		// 获取工作最早计划开始时间和最晚计划完成时间
		Document doc = c("workspace").aggregate(Arrays.asList(new Document("$match", new Document("space_id", workspace.getSpace_id())),
				new Document("$group", new Document("_id", null).append("finish", new Document("$max", "$planFinish")).append("start",
						new Document("$min", "$planStart")))))
				.first();
		if (doc != null) {
			Date planFinish;
			Date planStart;
			ObjectId work_id = workspace.getWork_id();
			if (work_id != null) {
				Work work = new WorkServiceImpl().getWork(work_id);
				planFinish = work.getPlanFinish();
				planStart = work.getPlanStart();
			} else {
				planFinish = project.getPlanFinish();
				planStart = project.getPlanStart();
			}
			// 获取早于项目计划开始的设置，默认为禁止
			setting = Optional.ofNullable(systemSetting.get(CHECKIN_SETTING_FIELD_PROJECT_START)).orElse(CHECKIN_SETTING_VALUE_REQUIREMENT);
			Date _start = doc.getDate("start");
			if (planStart.after(_start)) {
				if (CHECKIN_SETTING_VALUE_AUTO.equals(setting)) {
					if (workspace.getWork_id() != null) {
						c("work").updateOne(new BasicDBObject("_id", workspace.getWork_id()),
								new BasicDBObject("$set", new BasicDBObject("planStart", _start)));
					} else {
						c("project").updateOne(new BasicDBObject("_id", workspace.getProject_id()),
								new BasicDBObject("$set", new BasicDBObject("planStart", _start)));
					}
				} else if (CHECKIN_SETTING_VALUE_REQUIREMENT.equals(setting)) {
					results.add(Result.error("工作最早计划开始时间早于项目计划开始时间。"));
				} else if (CHECKIN_SETTING_VALUE_WARNING.equals(setting)) {
					results.add(Result.question("工作最早计划开始时间早于项目计划开始时间。"));
				}
			}
			// 获取晚于项目计划完成的设置，默认为禁止
			setting = Optional.ofNullable(systemSetting.get(CHECKIN_SETTING_FIELD_PROJECT_FINISH))
					.orElse(CHECKIN_SETTING_VALUE_REQUIREMENT);
			Date _finish = doc.getDate("finish");
			if (planFinish.before(_finish)) {
				if (CHECKIN_SETTING_VALUE_AUTO.equals(setting)) {
					if (workspace.getWork_id() != null) {
						c("work").updateOne(new BasicDBObject("_id", workspace.getWork_id()),
								new BasicDBObject("$set", new BasicDBObject("planFinish", _finish)));
					} else {
						c("project").updateOne(new BasicDBObject("_id", workspace.getProject_id()),
								new BasicDBObject("$set", new BasicDBObject("planFinish", _finish)));
					}
				} else if (CHECKIN_SETTING_VALUE_REQUIREMENT.equals(setting)) {
					results.add(Result.error("工作最完计划完成时间晚于项目计划完成时间。"));
				} else if (CHECKIN_SETTING_VALUE_WARNING.equals(setting)) {
					results.add(Result.question("工作最完计划完成时间晚于项目计划完成时间。"));
				}
			}
		}
		return results;

	}

	@Override
	public Result checkin(Workspace workspace) {
		if (workspace.getSpace_id() == null) {
			return Result.checkoutError("提交失败。", Result.CODE_ERROR);
		}

		ProjectServiceImpl projectServiceImpl = new ProjectServiceImpl();
		ObjectId project_id = workspace.getProject_id();
		projectServiceImpl.createBaseline(new Baseline().setProject_id(project_id).setCreationDate(new Date()).setName("修改进度计划"));

		List<ObjectId> workIds = c(Work.class).distinct("_id", new BasicDBObject("space_id", workspace.getSpace_id()), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		workIds.remove(workspace.getWork_id());

		List<ObjectId> workspaceIds = c(WorkInfo.class)
				.distinct("_id", new BasicDBObject("space_id", workspace.getSpace_id()), ObjectId.class).into(new ArrayList<ObjectId>());

		// 获取插入集合
		List<ObjectId> insertIds = new ArrayList<ObjectId>();
		insertIds.addAll(workspaceIds);
		insertIds.removeAll(workIds);

		// 获取删除集合
		List<ObjectId> deleteIds = new ArrayList<ObjectId>();
		deleteIds.addAll(workIds);
		deleteIds.removeAll(workspaceIds);

		deleteIds = getDesentItems(deleteIds, "work", "parent_id");

		// 获取修改集合
		List<ObjectId> updateIds = new ArrayList<ObjectId>();
		updateIds.addAll(workspaceIds);
		updateIds.removeAll(insertIds);
		updateIds.removeAll(deleteIds);

		if (!deleteIds.isEmpty()) {

			List<Work> stages = c(Work.class).find(new Document("_id", new Document("$in", deleteIds)).append("stage", true))
					.into(new ArrayList<Work>());
			if (!stages.isEmpty()) {
				stages.forEach(stage -> {
					ObjectId obs_id = stage.getOBS_id();
					if (obs_id != null)
						new OBSServiceImpl().delete(obs_id);

					ObjectId cbs_id = stage.getCBS_id();
					if (cbs_id != null)
						new CBSServiceImpl().delete(cbs_id);
				});
			}
			// 删除风险
			c("riskEffect").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", deleteIds)));
			// 根据删除集合删除Work
			c(Work.class).deleteMany(new BasicDBObject("_id", new BasicDBObject("$in", deleteIds)));
			// 根据删除集合删除资源计划
			c("resourcePlan").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", deleteIds)));
			// 根据删除集合删除工作包计划和执行
			ArrayList<ObjectId> packageIds = c("workPackage")
					.distinct("_id", new BasicDBObject("work_id", new BasicDBObject("$in", deleteIds)), ObjectId.class)
					.into(new ArrayList<>());
			c("workPackage").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", deleteIds)));
			c("workPackageProgress").deleteMany(new BasicDBObject("package_id", new BasicDBObject("$in", packageIds)));
		}

		// 根据插入集合插入Work
		if (!insertIds.isEmpty()) {
			ArrayList<Document> insertDoc = new ArrayList<>();
			ArrayList<ObjectId> parentIds = new ArrayList<>();
			c("workspace").find(new BasicDBObject("_id", new BasicDBObject("$in", insertIds))).forEach((Document d) -> {
				insertDoc.add(d);
				parentIds.add(d.getObjectId("parent_id"));
			});
			c("work").insertMany(insertDoc);
			// 删除关联新增work的parent的风险
			c("riskEffect").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", parentIds)));
			// 删除关联新增work的parent的资源计划
			c("resourcePlan").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", parentIds)));
			// 关联新增work的parent的资源用量
			c("resourceActual").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", parentIds)));
			// 删除关联新增work的parent的工作包设定和检查项
			c("work").updateMany(new BasicDBObject("work_id", new BasicDBObject("$in", parentIds)),
					new BasicDBObject("$set", new BasicDBObject("workPackageSetting", null).append("checklist", null)));
			// 删除关联新增work的parent的工作包计划和执行
			ArrayList<ObjectId> packageIds = c("workPackage")
					.distinct("_id", new BasicDBObject("work_id", new BasicDBObject("$in", parentIds)), ObjectId.class)
					.into(new ArrayList<>());
			c("workPackage").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", parentIds)));
			c("workPackageProgress").deleteMany(new BasicDBObject("package_id", new BasicDBObject("$in", packageIds)));
		}

		Project project = c(Project.class).find(new Document("_id", project_id)).first();
		final List<Message> messages = new ArrayList<>();

		String msgSubject = "工作计划更改通知";

		c("workspace").find(new BasicDBObject("_id", new BasicDBObject("$in", updateIds))).forEach((Document d) -> {
			// 更新Work
			Object _id = d.get("_id");
			// 处理workspace中
			d.remove("_id");
			d.remove("space_id");
			// TODO 下达状态，发送修改通知
			boolean distributed = d.getBoolean("distributed", false);
			if (distributed) {
				Document doc = c("work").find(new Document("_id", _id)).first();
				// yangjun 2018/10/31 修改
				String newAssignerId = d.getString("assignerId");
				String oldAssignerId = doc.getString("assignerId");
				String newChargerId = d.getString("chargerId");
				String oldChargerId = doc.getString("chargerId");
				Date oldPlanStart = doc.getDate("planStart");
				Date newPlanStart = d.getDate("planStart");
				Date oldPlanFinish = doc.getDate("planFinish");
				Date newPlanFinish = d.getDate("planFinish");
				String checkoutBy = workspace.getCheckoutBy();

				String content = "项目：" + project.getName() + " ，工作：" + doc.getString("fullName");
				if (!Check.equals(oldAssignerId, newAssignerId)) {
					if (oldAssignerId != null) {
						messages.add(Message.newInstance(msgSubject, content + "，您已不再担任工作指派者。", checkoutBy, (String) oldAssignerId, null));
					}
					if (newAssignerId != null) {
						messages.add(Message.newInstance(msgSubject, content + "，已指定您担任工作指派者。", checkoutBy, (String) newAssignerId, null));
					}
				}

				if (!Check.equals(oldChargerId, newChargerId)) {
					if (oldChargerId != null) {
						messages.add(Message.newInstance(msgSubject, content + "，您已不再担任工作负责人。", checkoutBy, (String) oldAssignerId, null));
					}
					if (newChargerId != null) {
						messages.add(Message.newInstance(msgSubject, content + "，已指定您担任工作负责人。", checkoutBy, (String) newChargerId, null));
					}
				}

				if (!oldPlanStart.equals(newPlanStart) || !oldPlanFinish.equals(newPlanFinish)) {
					// 使用通用的下达工作计划的通知模板
					String chargerId = doc.getString("chargerId");
					messages.add(Message.distributeWorkMsg(msgSubject, project.getName(), doc, true, checkoutBy, chargerId));
					String assignerId = doc.getString("assignerId");
					messages.add(Message.distributeWorkMsg(msgSubject, project.getName(), doc, true, checkoutBy, assignerId));
				}
			}

			// 处理负责人和指派者为空的情况 yangjun 2018/11/1
			if (Check.isNotAssigned(d.getString("chargerId")))
				d.put("chargerId", null);
			if (Check.isNotAssigned(d.getString("assignerId")))
				d.put("assignerId", null);

			c("work").updateOne(new BasicDBObject("_id", _id), new BasicDBObject("$set", d));
		});
		String projectStatus = project.getStatus();
		if (messages.size() > 0 && !ProjectStatus.Created.equals(projectStatus))
			sendMessages(messages);

		List<ObjectId> deleteResourcePlanId = new ArrayList<ObjectId>();
		Set<ObjectId> updateWorksId = new HashSet<ObjectId>();

		// 删除计划时间外的资源计划
		List<? extends Bson> pipeline = Arrays.asList(new Document("$match", new Document("work_id", new Document("$in", updateIds))),
				new Document("$lookup",
						new Document("from", "workspace")
								.append("localField", "work_id").append("foreignField", "_id").append("as", "workspace")),
				new Document("$unwind", "$workspace"),
				new Document("$addFields",
						new Document("delete",
								new Document("$or",
										Arrays.asList(new Document("$lt", Arrays.asList("$id", "$workspace.planStart")),
												new Document("$gt", Arrays.asList("$id", "$workspace.planFinish")))))),
				new Document("$match", new Document("delete", true)),
				new Document("$project", new Document("_id", true).append("work_id", true)));
		c("resourcePlan").aggregate(pipeline).forEach((Document d) -> {
			deleteResourcePlanId.add(d.getObjectId("_id"));
			updateWorksId.add(d.getObjectId("work_id"));
		});

		if (!deleteResourcePlanId.isEmpty()) {
			c("resourcePlan").deleteMany(new BasicDBObject("_id", new BasicDBObject("$in", deleteResourcePlanId)));
		}

		updateWorksId.forEach(_id -> updateWorkPlanWorks(_id));

		// 获取worklinksspace中的记录
		List<Document> worklinks = c("worklinksspace").find(new BasicDBObject("space_id", workspace.getSpace_id()))
				.into(new ArrayList<Document>());

		// 删除worklinks中的记录
		if (!workIds.isEmpty()) {
			c("worklinks").deleteMany(
					new BasicDBObject("source", new BasicDBObject("$in", workIds)).append("target", new BasicDBObject("$in", workIds)));
		}

		// 将获取的worklinksspace中的记录插入worklinks
		if (!worklinks.isEmpty()) {
			c("worklinks").insertMany(worklinks);
		}

		// TODO 进度计划提交的提示，更新。
		if (Result.CODE_WORK_SUCCESS == cleanWorkspace(Arrays.asList(workspace.getSpace_id())).code) {
			if (Arrays.asList(ProjectStatus.Closing, ProjectStatus.Processing).contains(projectStatus)) {
				List<ObjectId> workids = new ArrayList<ObjectId>();
				if (project.isStageEnable()) {
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////
					// 下达阶段计划
					workids.addAll(c("work").distinct("_id", new Document("project_id", project_id)// 本项目中的所有阶段
							.append("stage", true).append("distributed", new Document("$ne", true)), ObjectId.class)
							.into(new ArrayList<ObjectId>()));

					c("work").aggregate(new JQ("查询-工作-阶段需下达的工作计划").set("project_id", project_id).set("match", new Document()).array())
							.forEach((Document w) -> workids.add(w.getObjectId("_id")));
				} else {
					workids.addAll(insertIds);
				}
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////
				// 如果没有可下达的计划，提示
				if (!workids.isEmpty()) {
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////
					// 更新下达计划的和项目，记录下达信息
					User user = workspace.getCheckoutUser();
					Document distributeInfo = new Document("date", new Date()).append("userId", user.getUserId()).append("userName",
							user.getName());
					c("work").updateMany(new Document("_id", new Document("$in", workids)), //
							new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
				}
			}

			if (ProjectStatus.Created.equals(projectStatus)) {
				sendMessage("项目进度计划编制完成", "项目：" + project.getName() + " 进度计划已更新。", workspace.getCheckoutBy(), project.getPmId(), null);
			} else {
				List<ObjectId> parentIds = c("obs").distinct("_id", new BasicDBObject("scope_id", project_id), ObjectId.class)
						.into(new ArrayList<>());
				List<ObjectId> ids = getDesentItems(parentIds, "obs", "parent_id");
				ArrayList<String> memberIds = c("obs").distinct("managerId",
						new BasicDBObject("_id", new BasicDBObject("$in", ids)).append("managerId", new BasicDBObject("$ne", null)),
						String.class).into(new ArrayList<>());

				sendMessage("项目进度计划编制完成", "项目：" + project.getName() + " 进度计划已更新。", workspace.getCheckoutBy(), memberIds, null);
			}

			return Result.checkoutSuccess("项目进度计划提交成功");
		} else {
			return Result.checkoutError("项目进度计划提交失败", Result.CODE_ERROR);
		}
	}

	private void updateWorkPlanWorks(ObjectId work_id) {
		if (work_id != null) {
			// TODO 修改计算方式
			List<? extends Bson> pipeline = Arrays.asList(new Document("$match", new Document("work_id", work_id)),
					new Document("$addFields",
							new Document("planQty", new Document("$sum", Arrays.asList("$planBasicQty", "$planOverTimeQty")))),
					new Document("$group", new Document("_id", "$work_id").append("planWorks", new Document("$sum", "$planQty"))));

			double planWorks = Optional.ofNullable(c("resourcePlan").aggregate(pipeline).first()).map(d -> (Double) d.get("planWorks"))
					.map(p -> p.doubleValue()).orElse(0d);
			c(Work.class).updateOne(new Document("_id", work_id), new Document("$set", new Document("planWorks", planWorks)));
		}
	}

	@Override
	public Result cancelCheckout(Workspace workspace) {
		if (workspace.getSpace_id() == null) {
			return Result.checkoutError("撤销失败。", Result.CODE_ERROR);
		}

		if (Result.CODE_WORK_SUCCESS == cleanWorkspace(Arrays.asList(workspace.getSpace_id())).code) {
			return Result.checkoutSuccess("已成功撤销。");
		} else {
			return Result.checkoutError("撤销失败。", Result.CODE_ERROR);
		}
	}

	private Result cleanWorkspace(List<ObjectId> spaceIds) {
		c(WorkInfo.class).deleteMany(new BasicDBObject("space_id", new BasicDBObject("$in", spaceIds)));

		c(WorkLinkInfo.class).deleteMany(new BasicDBObject("space_id", new BasicDBObject("$in", spaceIds)));

		c("project").updateOne(new BasicDBObject("space_id", new BasicDBObject("$in", spaceIds)),
				new BasicDBObject("$unset", new BasicDBObject("checkoutBy", true).append("space_id", true)));

		c("work").updateMany(new BasicDBObject("space_id", new BasicDBObject("$in", spaceIds)),
				new BasicDBObject("$unset", new BasicDBObject("checkoutBy", true).append("space_id", true)));

		return Result.checkoutSuccess("已完成撤销成功。");
	}

	@Override
	public List<WorkInfo> createComparableWorkDataSet(ObjectId space_id) {
		List<? extends Bson> pipeline = Arrays.asList(new Document().append("$match", new Document().append("space_id", space_id)),
				new Document().append("$lookup",
						new Document().append("from", "work").append("localField", "_id").append("foreignField", "_id").append("as",
								"work")),
				new Document().append("$unwind", new Document().append("path", "$work").append("preserveNullAndEmptyArrays", true)),
				new Document().append("$addFields",
						new Document().append("planStart1", "$work.planStart").append("planFinish1", "$work.planFinish")
								.append("actualStart1", "$work.actualStart").append("actualFinish1", "$work.actualFinish")),
				new Document().append("$project", new Document().append("work", false)));

		return c(WorkInfo.class).aggregate(pipeline).into(new ArrayList<WorkInfo>());
	}

	@Override
	public Result updateGanttData(WorkspaceGanttData ganttData) {
		// TODO Auto-generated method stub
		ObjectId space_id = ganttData.getSpace_id();

		c(WorkInfo.class).deleteMany(new Document("space_id", space_id));

		c(WorkLinkInfo.class).deleteMany(new Document("space_id", space_id));

		List<WorkInfo> workInfos = ganttData.getTasks();
		for (int i = 0; i < workInfos.size(); i++) {
			workInfos.get(i).setIndex(i);
			if (workInfos.get(i).getParent_id() == null && ganttData.getWork_id() != null) {
				workInfos.get(i).setParent_id(ganttData.getWork_id());
			}
		}

		if (workInfos.size() > 0)
			c(WorkInfo.class).insertMany(workInfos);

		List<WorkLinkInfo> links = ganttData.getLinks();
		if (links.size() > 0)
			c(WorkLinkInfo.class).insertMany(links);
		return new Result();
	}
}
