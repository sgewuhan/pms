package com.bizvisionsoft.service.model;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Strict;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadOptions;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.EPSService;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ProjectSetService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.sn.ProjectGenerator;
import com.bizvisionsoft.service.sn.WorkOrderGenerator;
import com.mongodb.BasicDBObject;

/**
 * 项目基本模型，用于创建和编辑
 * 
 * @author hua
 *
 */
@Strict
@PersistenceCollection("project")
public class Project implements IOBSScope, ICBSScope, IWBSScope {

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 标识属性
	/**
	 * _id
	 */
	@SetValue
	@GetValue
	private ObjectId _id;

	/**
	 * 编号
	 */
	@ReadValue
	@Label(Label.ID_LABEL)
	@WriteValue
	@Persistence
	@Generator(name = Generator.DEFAULT_NAME, key = Generator.DEFAULT_KEY, generator = ProjectGenerator.class, callback = Generator.NONE_CALLBACK)
	private String id;

	@Override
	public String getProjectNumber() {
		return id;
	}

	/**
	 * 工作令号
	 */
	@ReadValue
	@WriteValue
	@Persistence
	@Generator(name = Generator.DEFAULT_NAME, key = "project", generator = WorkOrderGenerator.class, callback = Generator.NONE_CALLBACK)
	private String workOrder;

	/**
	 * 项目集Id
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private ObjectId projectSet_id;

	@SetValue
	@ReadValue
	private ProjectSet projectSet;

	/**
	 * 父项目Id
	 */
	@Persistence
	private ObjectId parentProject_id;

	@SetValue
	@ReadValue
	private Project parentProject;

	public ObjectId getParentProject_id() {
		return parentProject_id;
	}

	/**
	 * WBS上级Id
	 */
	@Persistence
	private ObjectId wbsParent_id;

	/**
	 * EPS节点Id
	 */
	@Persistence
	private ObjectId eps_id;

	@SetValue
	@ReadValue
	private EPS eps;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 描述属性
	/**
	 * 名称
	 */
	@ReadValue
	@WriteValue
	@Persistence
	@Label(Label.NAME_LABEL)
	private String name;

	@Override
	public String getProjectName() {
		return name;
	}

	/**
	 * 描述
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String description;

	/**
	 * 类别：预研、科研(下级：新研、改性、适应性改造)、CBB
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String catalog;

	public String getCatalog() {
		return catalog;
	}

	/**
	 * 项目等级 A, B, C
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String classfication;

	/**
	 * 密级
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String securityLevel;

	@ReadValue
	@WriteValue
	@Persistence
	private String status;
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 计划属性
	/**
	 * 计划开始
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private Date planStart;
	/**
	 * 计划完成
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private Date planFinish;

	/**
	 * 计划工期
	 **/
	@ReadValue
	@GetValue("planDuration")
	public int getPlanDuration() {
		if (planFinish != null && planStart != null) {
			return (int) ((planFinish.getTime() - planStart.getTime()) / (1000 * 3600 * 24));
		} else {
			return 0;
		}
	}

	/**
	 * 计划工时
	 */
	@SetValue
	private double summaryPlanWorks;

	@ReadValue("planWorks")
	private double getPlanWorks() {
		return summaryPlanWorks;
	}

	/**
	 * 实际开始
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private Date actualStart;

	/**
	 * 实际完成
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private Date actualFinish;

	/**
	 * 启动时间
	 */
	@ReadValue
	@SetValue
	private Date startOn;

	/**
	 * 完工时间
	 */
	@ReadValue
	@SetValue
	private Date finishOn;

	@ReadValue("start")
	public Date getStart_date() {
		return startOn;
	}

	@ReadValue("finish")
	public Date getEnd_date() {
		return finishOn;
	}

	/**
	 * 计划工期 ///TODO 根据计划开始和完成自动计算
	 */
	@ReadValue
	@GetValue("actualDuration")
	public int getActualDuration() {
		if (actualFinish != null && actualStart != null) {
			return (int) ((actualFinish.getTime() - actualStart.getTime()) / (1000 * 3600 * 24));
		} else if (actualFinish == null && actualStart != null) {
			return (int) (((new Date()).getTime() - actualStart.getTime()) / (1000 * 3600 * 24));
		} else {
			return 0;
		}
	}

	/**
	 * 计划工时 //TODO 计划工时的计算
	 */

	@SetValue
	private double summaryActualWorks;

	@ReadValue("actualWorks")
	private double getActualWorks() {
		return summaryActualWorks;
	}

	/**
	 * 启用阶段管理
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private boolean stageEnable;

	/**
	 * 阶段
	 */
	@ReadValue
	@SetValue
	private Work stage;

	@Persistence
	private ObjectId projectTemplate_Id;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 项目经理
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String pmId;

	@SetValue
	@ReadValue
	private String pmInfo;

	@WriteValue("pm")
	private void setPM(User pm) {
		this.pmId = Optional.ofNullable(pm).map(o -> o.getUserId()).orElse(null);
	}

	@ReadValue("pm")
	private User getPM() {
		return Optional.ofNullable(pmId).map(id -> ServicesLoader.get(UserService.class).get(id)).orElse(null);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 承担单位
	 */
	@Persistence // 数据库存取
	private ObjectId impUnit_id;

	@SetValue // 查询服务设置
	@ReadValue // 表格用
	private String impUnitOrgFullName;

	public ObjectId getImpUnit_id() {
		return impUnit_id;
	}

	@WriteValue("impUnit") // 编辑器用
	public void setOrganization(Organization org) {
		this.impUnit_id = Optional.ofNullable(org).map(o -> o.get_id()).orElse(null);
	}

	@ReadValue("impUnit") // 编辑器用
	public Organization getOrganization() {
		return Optional.ofNullable(impUnit_id).map(_id -> ServicesLoader.get(OrganizationService.class).get(_id))
				.orElse(null);
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ReadValue
	@SetValue
	private Date settlementDate;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 客户化基本属性
	/**
	 * 分为：纵向、横向、争取、自主
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String type1;

	/**
	 * 分为：独立、联合、部分委托
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String type2;

	/**
	 * 分为：其它
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String type3;

	@ReadValue("type2_3")
	private String getType2_3() {
		String str = "";
		if (type2 != null)
			str += type2 + " ";
		if (type3 != null)
			str += type3;
		return str.trim();
	}

	/**
	 * 军兵种
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private List<String> arms;

	/**
	 * 战区
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private List<String> area;

	/**
	 * 客户
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String customer;

	@WriteValue("eps_or_projectset_id")
	public void setEPSorProjectSet(Object element) {
		if (element instanceof EPS)
			this.eps_id = ((EPS) element).get_id();
		if (element instanceof ProjectSet)
			this.projectSet_id = ((ProjectSet) element).get_id();
	}

	@ReadValue("eps_or_projectset_id")
	public Object getEPSOrProjectSet() {
		if (eps_id != null)
			return ServicesLoader.get(EPSService.class).get(eps_id);
		if (projectSet_id != null)
			return ServicesLoader.get(ProjectSetService.class).get(projectSet_id);
		return null;
	}

	@ReadOptions("catalog")
	public Map<String, Object> getCatalogOptions() {
		LinkedHashMap<String, Object> options = new LinkedHashMap<String, Object>();
		options.put("预研", "预研");
		options.put("科研-新研", "科研-新研");
		options.put("科研-改性", "科研-改性");
		options.put("科研-适应性改造", "科研-适应性改造");
		options.put("CBB", "CBB");
		return options;
	}

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@ImageURL("id")
	private String logo = "/img/project_c.svg";

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "项目";

	@Structure("我的项目（首页小组件）/list")
	private List<ProjectBoardInfo> getProjectBoardInfo() {
		return Arrays.asList(new ProjectBoardInfo().setProject(this));
	}

	@Structure("我的项目（首页小组件）/count")
	private long countProjectBoardInfo() {
		return 1;
	}

	@Behavior("EPS浏览/打开") // 控制action
	private boolean enableOpen() {
		return true;// TODO 考虑权限
	}

	// @Behavior("我的项目/编辑项目") // 控制action
	// private boolean enableEdit() {
	// return true;// TODO 考虑权限
	// }
	//
	// @Behavior("我的项目/删除项目") // 控制action
	// private boolean enableDelete() {
	// return true;// TODO 考虑权限
	// }

	@Persistence
	private CreationInfo creationInfo;

	@Persistence
	private ObjectId obs_id;

	@Persistence
	private ObjectId cbs_id;

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public Date getPlanStart() {
		return planStart;
	}

	public Date getPlanFinish() {
		return planFinish;
	}

	public Project setStageEnable(boolean stageEnable) {
		this.stageEnable = stageEnable;
		return this;
	}

	public Project setCreationInfo(CreationInfo creationInfo) {
		this.creationInfo = creationInfo;
		return this;
	}

	public ObjectId getProjectTemplate_id() {
		return projectTemplate_Id;
	}

	public String getPmId() {
		return pmId;
	}

	public Project setOBS_id(ObjectId obs_id) {
		this.obs_id = obs_id;
		return this;
	}

	public Project setCBS_id(ObjectId cbs_id) {
		this.cbs_id = cbs_id;
		return this;
	}

	public ObjectId getOBS_id() {
		return obs_id;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public ObjectId getProjectSet_id() {
		return projectSet_id;
	}

	@Override
	public ObjectId getScope_id() {
		return _id;
	}

	@Override
	public String getScopeName() {
		return name;
	}

	@Override
	public ObjectId getCBS_id() {
		return cbs_id;
	}

	@Override
	public Date[] getCBSRange() {
		return new Date[] { planStart, planFinish };
	}

	public boolean isStageEnable() {
		return stageEnable;
	}

	public String getStatus() {
		return status;
	}

	public Project setStatus(String status) {
		this.status = status;
		return this;
	}

	@Override
	public OBSItem newOBSScopeRoot() {

		ObjectId obsParent_id = Optional.ofNullable(projectSet_id)
				.map(pjset_id -> ServicesLoader.get(ProjectSetService.class).get(pjset_id)).map(ps -> ps.getOBS_id())
				.orElse(null);

		OBSItem obsRoot = new OBSItem()// 创建本项目的OBS根节点
				.set_id(new ObjectId())// 设置_id与项目关联
				.setScope_id(_id)// 设置scope_id表明该组织节点是该项目的组织
				.setParent_id(obsParent_id)// 设置上级的id
				.setName(getName() + "项目组")// 设置该组织节点的默认名称
				.setRoleId(OBSItem.ID_PM)// 设置该组织节点的角色id
				.setRoleName(OBSItem.NAME_PM)// 设置该组织节点的名称
				.setManagerId(getPmId()) // 设置该组织节点的角色对应的人
				.setScopeRoot(true);// 区分这个节点是范围内的根节点

		return obsRoot;

	}

	@Override
	public void updateOBSRootId(ObjectId obs_id) {
		ServicesLoader.get(ProjectService.class).update(new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
				.set(new BasicDBObject("obs_id", obs_id)).bson());
		this.obs_id = obs_id;
	}

	@Persistence
	private String checkoutBy;

	@Persistence
	private ObjectId space_id;

	@Override
	public Workspace getWorkspace() {
		return ServicesLoader.get(ProjectService.class).getWorkspace(_id);
	}

	@Override
	public ObjectId getParent_id() {
		return null;
	}

	@Override
	public ObjectId getProject_id() {
		return _id;
	}

	@Override
	public List<WorkLink> createGanttLinkDataSet() {
		return ServicesLoader.get(WorkService.class).createProjectLinkDataSet(_id);
	}

	@Override
	public List<Work> createGanttTaskDataSet() {
		return ServicesLoader.get(WorkService.class).createProjectTaskDataSet(_id);
	}

	public String getPmInfo() {
		return pmInfo;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工期完成率 百分比
	@ReadValue("dar")
	public Object getDAR() {
		int planDuration = getPlanDuration();
		if (planDuration != 0) {
			return 1d * getActualDuration() / planDuration;
		}
		return "--";
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工作量完成率 百分比
	@ReadValue("war")
	public Object getWAR() {
		if (getPlanWorks() != 0) {
			return 1d * getActualWorks() / getPlanWorks();
		}
		return "--";
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工时完成率 百分比
	@ReadValue("sar")
	public Object getSAR() {
		int planDuration = getPlanDuration();
		if (planDuration != 0) {
			return 1d * getActualDuration() / planDuration;
		}
		return "--";
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	private CBSItem cbsItem;

	@ReadValue("cost")
	public Double getCost() {
		if (cbsItem == null) {
			cbsItem = ServicesLoader.get(CBSService.class).getCBSItemCost(cbs_id);
		}
		return Optional.ofNullable(cbsItem.cbsSubjectCost).orElse(0d);
	}

	@ReadValue("budget")
	public Double getBudget() {
		if (cbsItem == null) {
			cbsItem = ServicesLoader.get(CBSService.class).getCBSItemCost(cbs_id);
		}
		return Optional.ofNullable(cbsItem.cbsSubjectBudget).orElse(0d);
	}

	@ReadValue("car")
	public Object getCAR() {
		Double budget = getBudget();
		if (budget != null && budget != 0) {
			return getCost() / budget;
		}
		return "--";
	}

	@ReadValue("bdr")
	public Object getBDR() {
		Double budget = getBudget();
		if (budget != null && budget != 0) {
			return (getCost() - budget) / budget;
		}
		return "--";
	}

	@SetValue
	private Boolean overdue;

	public String getOverdue() {
		if (Boolean.TRUE.equals(overdue))
			return "超期";
		else
			return "";
	}

}
