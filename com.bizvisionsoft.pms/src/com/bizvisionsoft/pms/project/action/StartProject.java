package com.bizvisionsoft.pms.project.action;

import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class StartProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "项目启动", "请确认启动项目" + project + "。<br/>系统将标记当前时间为项目完工日期。<br/>已启动的项目将无法删除。");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(ProjectService.class)
				.startProject(brui.command(project.get_id(), new Date()));
		boolean hasError = false;
		boolean hasWarning = false;

		String message = "";
		if (!result.isEmpty()) {
			for (Result r : result)
				if (Result.TYPE_ERROR == r.type) {
					hasError = true;
					message += "错误：" + r.message + "<br>";
				} else if (Result.TYPE_WARNING == r.type) {
					hasError = true;
					message += "警告：" + r.message + "<br>";
				} else {
					message += "信息：" + r.message + "<br>";
				}
		}

		if (message.isEmpty()) {
			Layer.message("项目已启动");
		} else {
			if (hasError) {
				MessageDialog.openError(shell, "项目启动", message);
				return;
			} else if (hasWarning) {
				MessageDialog.openWarning(shell, "项目启动", "项目已启动，请注意以下提示信息：<br>" + message);
			} else {
				MessageDialog.openInformation(shell, "项目启动", "项目已启动，请注意以下提示信息：<br>" + message);
			}
		}

		brui.switchPage("项目首页（执行）", ((Project) project).get_id().toHexString());

	}

}
