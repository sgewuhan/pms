package com.bizvisionsoft.pms.projecttemplate;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.OBSModule;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.WBSModule;

public class OpenProjectTemplateACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(em -> {
			if (em instanceof WBSModule) {
				br.openContent(br.getAssembly("项目模板甘特图.ganttassy"), em);
			} else if (em instanceof OBSModule) {
				// 当前选择为组织模板时，打开OBS模板组织结构图进行组织模板的编辑
				br.openContent(br.getAssembly("OBS模板组织结构图.treeassy"), em, e -> {
					OBSModule obs = ServicesLoader.get(ProjectTemplateService.class)
							.getOBSModule(((OBSModule) em).get_id(), br.getDomain());
					GridPart viewer = (GridPart) context.getContent();
					AUtil.simpleCopy(obs, em);
					viewer.refresh(em);
				});
			} else {
				br.switchPage("项目模板", ((ProjectTemplate) em).get_id().toHexString());
			}
		});
	}

}
