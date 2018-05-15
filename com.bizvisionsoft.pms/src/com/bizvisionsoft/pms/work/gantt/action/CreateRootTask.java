package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.WorkInfo;

public class CreateRootTask {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		IWBSScope rootInput = (IWBSScope) context.getRootInput();
		String title;
		Assembly editor;
		WorkInfo workInfo;
		if (rootInput instanceof Project) {
			workInfo = WorkInfo.newInstance(rootInput, null);
		} else {
			workInfo = WorkInfo.newInstance(rootInput, rootInput.getScope_id());
		}
		if ((rootInput instanceof Project) && ((Project) rootInput).isStageEnable()) {
			title = "创建阶段";
			editor = bruiService.getAssembly("甘特图阶段工作编辑器");
			workInfo.setManageLevel("1").setStage(true).setStatus(ProjectStatus.Created);
		} else {
			title = "创建工作";
			editor = bruiService.getAssembly("甘特图工作编辑器");
		}

		new Editor<WorkInfo>(editor, context).setTitle(title).setInput(workInfo).ok((r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.addTask(wi, wi.index());
		});
	}

}
