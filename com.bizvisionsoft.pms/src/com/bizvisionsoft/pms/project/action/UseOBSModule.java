package com.bizvisionsoft.pms.project.action;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.OBSModule;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class UseOBSModule {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Project project = (Project) context.getRootInput();
		if (br.confirm("套用组织模板", "套用组织模板将<span class='layui-badge'>替换</span>项目团队，请确认套用组织模板。")) {
			Selector.open("组织模板选择器.selectorassy", context, project, l -> {
				Services.get(ProjectTemplateService.class).useOBSModule(((OBSModule) l.get(0)).get_id(), project.get_id(), br.getDomain());
				Layer.message("组织模板已套用到本项目，");
			});

		}
	}
}
