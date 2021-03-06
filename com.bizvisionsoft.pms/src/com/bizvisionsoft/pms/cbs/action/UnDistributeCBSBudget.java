package com.bizvisionsoft.pms.cbs.action;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.cbs.assembly.BudgetCBS;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.serviceconsumer.Services;

@Deprecated
public class UnDistributeCBSBudget {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(c -> {
			CBSItem item = (CBSItem) c;
			ObjectId parentItemId = item.getParent_id();
			if (item.isScopeRoot()) {
				CBSItem cbsItem = Services.get(CBSService.class).unallocateBudget(item.get_id(), parentItemId, br.getDomain());
				BudgetCBS grid = (BudgetCBS) context.getContent();
				CBSItem parent = item.getParent();
				grid.replaceItem(item, cbsItem);
				item.setParent(parent);
				grid.refresh(AUtil.simpleCopy(cbsItem, item));
			}

		});
	}

}
