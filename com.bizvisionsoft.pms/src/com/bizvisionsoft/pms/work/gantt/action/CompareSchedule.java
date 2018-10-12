package com.bizvisionsoft.pms.work.gantt.action;

import java.util.List;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLinkInfo;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class CompareSchedule {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) Event event) {
		GanttPart ganttPart = (GanttPart) context.getContent();
		if (ganttPart.isDirty()) {
			Layer.message("当前的项目计划还未保存", Layer.ICON_CANCEL);
		} else {
			IWBSScope root = (IWBSScope) context.getRootInput();
			Workspace ws = root.getWorkspace();
			List<WorkInfo> workSet = Services.get(WorkSpaceService.class).createComparableWorkDataSet(ws.getSpace_id());
			List<WorkLinkInfo> linkSet = Services.get(WorkSpaceService.class)
					.createLinkDataSet(new BasicDBObject("space_id", ws.getSpace_id()));
			brui.openContent(brui.getAssembly("比较甘特图"), new Object[] { workSet, linkSet });
		}
	}
}
