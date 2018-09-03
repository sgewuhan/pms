package com.bizvisionsoft.pms.workpackage.dataset;

import java.util.ArrayList;
import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ScheduleMonitoringDateset {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private Work work;

	private Project project;

	private String userid;

	@Init
	private void init() {
		Object rootInput = context.getRootInput();
		if (rootInput instanceof Project) {
			project = (Project) rootInput;
		} else if (rootInput instanceof Work) {
			work = (Work) rootInput;
		} else {
			userid = brui.getCurrentUserId();
		}
	}

	@DataSet("�з����ȼ��/" + DataSet.LIST)
	public List<Work> listDesign() {
		String catagory = "�з�";
		if (project != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return new ArrayList<Work>();
		}
	}

	@DataSet("�з����ȼ��/" + DataSet.COUNT)
	public long countDesign() {
		String catagory = "�з�";
		if (project != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return 0;
		}
	}

	@DataSet("�ɹ��ƻ����/" + DataSet.LIST)
	public List<Work> listPurchase() {
		String catagory = "�ɹ�";
		if (project != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return new ArrayList<Work>();
		}
	}

	@DataSet("�ɹ��ƻ����/" + DataSet.COUNT)
	public long countPurchase() {
		String catagory = "�ɹ�";
		if (project != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return 0;
		}
	}

	@DataSet("�����ƻ����/" + DataSet.LIST)
	public List<Work> listProduce() {
		String catagory = "����";
		if (project != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return new ArrayList<Work>();
		}
	}

	@DataSet("�����ƻ����/" + DataSet.COUNT)
	public long countProduce() {
		String catagory = "����";
		if (project != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return 0;
		}
	}

	@DataSet("������ȼ��/" + DataSet.LIST)
	public List<Work> listInspection() {
		String catagory = "����";
		if (project != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).listWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return new ArrayList<Work>();
		}
	}

	@DataSet("������ȼ��/" + DataSet.COUNT)
	public long countInspection() {
		String catagory = "����";
		if (project != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInProject(project.get_id(), catagory);
		} else if (work != null) {
			return Services.get(WorkService.class).countWorkPackageForScheduleInStage(work.get_id(), catagory);
		} else {
			return 0;
		}
	}

	@DataSet("�ɹ��ƻ���أ���Ŀ������/" + DataSet.LIST)
	public List<Work> listPurchaseForManagement(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		String catagory = "�ɹ�";
		List<Work> listWorkPackageForSchedule = Services.get(WorkService.class).listWorkPackageForSchedule(condition, userid, catagory);
		return listWorkPackageForSchedule;
	}

	@DataSet("�ɹ��ƻ���أ���Ŀ������/" + DataSet.COUNT)
	public long countPurchaseForManagement(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		String catagory = "�ɹ�";
		return Services.get(WorkService.class).countWorkPackageForSchedule(filter, userid, catagory);
	}

	@DataSet("�����ƻ���أ���Ŀ������/" + DataSet.LIST)
	public List<Work> listProduceForManagement(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		String catagory = "����";
		return Services.get(WorkService.class).listWorkPackageForSchedule(condition, userid, catagory);
	}

	@DataSet("�����ƻ���أ���Ŀ������/" + DataSet.COUNT)
	public long countProduceForManagement(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		String catagory = "����";
		return Services.get(WorkService.class).countWorkPackageForSchedule(filter, userid, catagory);
	}
}