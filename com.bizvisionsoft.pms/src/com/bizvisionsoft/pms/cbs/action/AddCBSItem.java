package com.bizvisionsoft.pms.cbs.action;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.pms.cbs.assembly.BudgetCBS;
import com.bizvisionsoft.service.model.CBSItem;
/**
 * 取消了CBS可分解功能，该操作暂时无效。
 * @author gdiyang
 * @date 2018/10/27
 *
 */
@Deprecated
public class AddCBSItem {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(parent -> {
			Editor.create("成本项编辑器.editorassy", context, CBSItem.getInstance((CBSItem) parent,br.getDomain()), true).setTitle("添加子项")
					.ok((r, o) -> {
						BudgetCBS cbsGrid = (BudgetCBS) context.getContent();
						AUtil.simpleCopy(cbsGrid, o);
						cbsGrid.addCBSItem((CBSItem) parent, o);
					});

		});
	}
}
