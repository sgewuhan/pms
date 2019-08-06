package com.bizvisionsoft.pms.vault;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.IFolder;

public class VaultACT {

	// TODO ���ǵ�Ȩ����Ҫ���÷���ʹ�ò�������Action��Ϊ

	@Inject
	private IBruiService br;

	@Inject
	private IBruiContext context;

	@Execute
	public void execute(@MethodParam(Execute.EVENT) Event e, @MethodParam(Execute.ACTION) Action action,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) IFolder folder) {
		VaultExplorer explorer = (VaultExplorer) context.getParentContext().getContent();
		explorer.handleAction(folder, action);
	}

	// TODO ��Ҫ��AUtil����Behaviorʱ����action���뵽�����С�������Щ�����Ϳ��Խ��кϲ�
	@Behavior("openFolder")
	private boolean enableOpenFolder(@MethodParam(Execute.CONTEXT_SELECTION_1ST) IFolder folder) {
		VaultExplorer explorer = (VaultExplorer) context.getParentContext().getContent();
		return explorer.enableAction(folder, "openFolder");
	}

	@Behavior("moveFolder")
	private boolean enableMoveFolder(@MethodParam(Execute.CONTEXT_SELECTION_1ST) IFolder folder) {
		VaultExplorer explorer = (VaultExplorer) context.getParentContext().getContent();
		return explorer.enableAction(folder, "moveFolder");
	}

	@Behavior("deleteFolder")
	private boolean enableDeleteFolder(@MethodParam(Execute.CONTEXT_SELECTION_1ST) IFolder folder) {
		VaultExplorer explorer = (VaultExplorer) context.getParentContext().getContent();
		return explorer.enableAction(folder, "deleteFolder");
	}

	@Behavior("renameFolder")
	private boolean enableRenameFolder(@MethodParam(Execute.CONTEXT_SELECTION_1ST) IFolder folder) {
		VaultExplorer explorer = (VaultExplorer) context.getParentContext().getContent();
		return explorer.enableAction(folder, "renameFolder");
	}
}