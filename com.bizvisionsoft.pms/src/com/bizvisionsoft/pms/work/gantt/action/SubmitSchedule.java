package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubmitSchedule {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		IWBSScope rootInput = (IWBSScope) context.getRootInput();
		if (rootInput != null) {
			if (MessageDialog.openConfirm(brui.getCurrentShell(), "提交计划", "请确认提交当前计划。")) {
				Workspace workspace = rootInput.getWorkspace();
				if (workspace != null) {
					Boolean checkManageItem = true;
					if (rootInput instanceof Project) {
						checkManageItem = false;
					}
					Result result = Services.get(WorkSpaceService.class).schedulePlanCheck(workspace, checkManageItem);

					if (Result.CODE_WORK_SUCCESS == result.code) {
						result = Services.get(WorkSpaceService.class).checkin(workspace);

						if (Result.CODE_WORK_SUCCESS == result.code) {
							MessageDialog.openFinished(brui.getCurrentShell(), "提交计划", result.message);
							brui.switchContent("项目甘特图", null);
						}
					} else {
						MessageDialog.openError(brui.getCurrentShell(), "提交计划",
								"管理节点 <b style='color:red;'>" + result.data + "</b> 完成时间超过限定。");
					}
				}
			}
		}
	}
}
