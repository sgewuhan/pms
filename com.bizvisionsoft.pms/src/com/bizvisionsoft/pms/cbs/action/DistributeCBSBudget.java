package com.bizvisionsoft.pms.cbs.action;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.pms.cbs.assembly.BudgetCBS;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.serviceconsumer.Services;

public class DistributeCBSBudget {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(parent -> {
			new Selector(brui.getAssembly("阶段选择器"), context).setInput(context.getRootInput()).setTitle("分配预算到指定阶段")
					.open(r -> {
						// TODO 在CBS节点上显示分配到哪个阶段
						// TODO 阶段选择器上显示分配情况
						// TODO 控制哪些预算可以分配
						// TODO 取消阶段的预算分配
						WorkInfo workInfo = (WorkInfo) r.get(0);
						ObjectId cbs_id = workInfo.getCBS_id();
						if (cbs_id != null) {
							MessageDialog.openError(brui.getCurrentShell(), "错误", "已经为该阶段分配预算，无法再次分配。");
						} else {
							CBSItem cbsItem = Services.get(CBSService.class).allocateBudget(((CBSItem) parent).get_id(),
									workInfo.get_id(), workInfo.toString());
							// TODO 错误返回
							// TODO 成功提示
							BudgetCBS grid = (BudgetCBS) context.getContent();
							grid.replaceItem(parent, cbsItem);
							grid.refresh(parent);
						}
					});

		});
	}

}
