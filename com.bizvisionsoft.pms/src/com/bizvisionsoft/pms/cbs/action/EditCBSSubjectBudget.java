package com.bizvisionsoft.pms.cbs.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.pms.cbs.assembly.BudgetSubject;
import com.bizvisionsoft.service.model.AccountItem;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSSubject;
import com.bizvisionsoft.service.model.ICBSScope;

public class EditCBSSubjectBudget {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(parent -> {
			AccountItem account = (AccountItem) parent;
			CBSSubject period;
			CBSItem cbs = (CBSItem) context.getInput();
			if (cbs == null) {
				ICBSScope rootInput = (ICBSScope) context.getRootInput();
				period = new CBSSubject().setCBSItem_id(rootInput.getCBS_id()).setSubjectNumber(account.getId());
			} else {
				period = new CBSSubject().setCBSItem_id(cbs.get_id()).setSubjectNumber(account.getId());
			}

			Util.ifInstanceThen(context.getRootInput(), ICBSScope.class, r -> period.setRange(r.getCBSRange()));

			Editor.create("�ڼ�Ԥ��༭��", context, period, true).setTitle("�༭��Ŀ�ڼ�Ԥ��").ok((r, o) -> {
				BudgetSubject grid = (BudgetSubject) context.getContent();
				grid.updateCBSSubjectBudget(account, o);
			});

		});
	}

}
