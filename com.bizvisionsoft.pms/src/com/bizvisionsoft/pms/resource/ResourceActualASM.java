package com.bizvisionsoft.pms.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizivisionsoft.widgets.gantt.GanttEventCode;
import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.ResourceTransfer;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class ResourceActualASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private GanttPart gantt;

	private EditResourceASM grid;

	private Work work;

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		StickerTitlebar bar = new StickerTitlebar(parent, null, null).setText("资源用量")
				.setActions(context.getAssembly().getActions());
		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 48;

		Composite content = UserSession.bruiToolkit().newContentPanel(parent);
		fd = new FormData();
		content.setLayoutData(fd);
		fd.left = new FormAttachment(0, 8);
		fd.top = new FormAttachment(bar, 8);
		fd.right = new FormAttachment(100, -8);
		fd.bottom = new FormAttachment(100, -8);
		content.setLayout(new FillLayout(SWT.VERTICAL));

		gantt = (GanttPart) new AssemblyContainer(content, context).setAssembly(brui.getAssembly("项目甘特图（资源实际分配）"))
				.setServices(brui).create().getContext().getContent();
		ResourceTransfer rt = new ResourceTransfer();
		rt.setType(ResourceTransfer.TYPE_ACTUAL);
		rt.setShowType(ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE);
		rt.setCheckTime(true);
		rt.setCanAdd(false);
		rt.setCanDelete(true);
		rt.setCanClose(false);
		rt.setShowResActual(true);
		rt.setShowResPlan(true);
		rt.setShowResTypeInfo(true);
		rt.setShowConflict(false);
		rt.setShowFooter(true);

		grid = (EditResourceASM) new AssemblyContainer(content, context).setAssembly(brui.getAssembly("编辑资源情况"))
				.setInput(rt).setServices(brui).create().getContext().getContent();
		// 侦听gantt的selection
		gantt.addGanttEventListener(GanttEventCode.onTaskSelected.name(), l -> select((Work) ((GanttEvent) l).task));

		bar.addListener(SWT.Selection, l -> {
			Action action = ((Action) l.data);
			if ("添加资源用量".equals(action.getName())) {
				if (this.work == null) {
					Layer.message("请先选择将要添加资源用量的工作");
					return;
				} else if (this.work.isSummary()) {
					Layer.message("无需对总成型工作添加资源用量");
					return;
				} else if (this.work.isMilestone()) {
					Layer.message("无需对里程碑添加资源用量");
					return;
				}
				allocateResource();
			} else {
				UserSession.bruiToolkit().runAction(action, l, brui, context);
			}
		});

		gantt.addGanttEventListener(GanttEventCode.onTaskDblClick.name(), l -> {
			Work work = (Work) ((GanttEvent) l).task;
			if (work != null && !work.isSummary() && !work.isMilestone()) {
				allocateResource();
			}
		});

	}

	private void allocateResource() {
		// 显示资源选择框
		Action hrRes = new Action();
		hrRes.setName("hr");
		hrRes.setText("人力资源");
		hrRes.setImage("/img/team_w.svg");
		hrRes.setStyle("normal");

		Action eqRes = new Action();
		eqRes.setName("eq");
		eqRes.setText("设备资源");
		eqRes.setImage("/img/equipment_w.svg");
		eqRes.setStyle("normal");

		Action typedRes = new Action();
		typedRes.setName("tr");
		typedRes.setText("资源类型");
		typedRes.setImage("/img/resource_w.svg");
		typedRes.setStyle("info");

		// 弹出menu
		new ActionMenu(brui).setActions(Arrays.asList(hrRes, eqRes, typedRes)).handleActionExecute("hr", a -> {
			addResource("人力资源选择器");
			return false;
		}).handleActionExecute("eq", a -> {
			addResource("设备设施选择器");
			return false;
		}).handleActionExecute("tr", a -> {
			addResource("资源类型选择器");
			return false;
		}).open();
	}

	private void addResource(String selectorId) {
		// TODO
		Selector.open(selectorId, context, null, l -> {
			List<ResourceAssignment> resa = new ArrayList<ResourceAssignment>();
			l.forEach(o -> {
				ResourceAssignment ra = new ResourceAssignment().setTypedResource(o).setWork_id(work.get_id());
				ra.from = work.getStart_date();
				ra.to = work.getEnd_date();
				resa.add(ra);
			});
			Services.get(WorkService.class).addResourceActual(resa);
			grid.doRefresh();
		});
	}

	private void select(Work work) {
		if (this.work != null && this.work.get_id().equals(work.get_id())) {
			return;
		}
		this.work = work;
		// 查询
		ResourceTransfer rt = new ResourceTransfer();
		rt.addWorkIds(work.get_id());
		rt.setType(ResourceTransfer.TYPE_ACTUAL);
		rt.setShowType(ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE);
		rt.setFrom(work.getStart_date());
		rt.setTo(work.getEnd_date());
		rt.setCanAdd(work.getActualStart() != null);
		rt.setCanDelete(true);
		rt.setCanClose(false);
		rt.setShowResActual(true);
		rt.setShowResPlan(true);
		rt.setShowResTypeInfo(true);
		rt.setShowConflict(false);
		rt.setShowFooter(true);
		rt.setTitle(work.getFullName() + "工作资源实际用量");

		grid.setResourceTransfer(rt);
	}

}
