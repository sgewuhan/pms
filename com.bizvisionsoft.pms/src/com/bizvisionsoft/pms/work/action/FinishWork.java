package com.bizvisionsoft.pms.work.action;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class FinishWork {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(elem -> {
			Work work = (Work) elem;
			Shell shell = brui.getCurrentShell();

			boolean ok = MessageDialog.openConfirm(shell, "��ɹ���", "��ȷ����ɹ���" + work + "��\nϵͳ����¼����ʱ��Ϊ������ʵ�����ʱ�䡣");
			if (!ok) {
				return;
			}
			List<Result> result = Services.get(WorkService.class).finishWork(work.get_id());
			if (result.isEmpty()) {
				MessageDialog.openInformation(shell, "��ɹ���", "��������ɡ�");
				GridPart grid = (GridPart) context.getContent();
				grid.remove(elem);
			}
		});
	}

}