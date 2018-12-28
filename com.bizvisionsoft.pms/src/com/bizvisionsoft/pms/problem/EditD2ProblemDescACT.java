package com.bizvisionsoft.pms.problem;

import java.util.List;

import org.bson.Document;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.rap.rwt.RWT;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditD2ProblemDescACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) BruiAssemblyContext context,
			@MethodParam(Execute.PAGE_CONTEXT_INPUT_OBJECT) Problem problem) {
		ProblemService service = Services.get(ProblemService.class);
		Document d = service.getD2ProblemDesc(problem.get_id());
		Editor.create("D2-5W2H��������", context, d, true).ok((r, t) -> {
			t = service.updateD2ProblemDesc(t, RWT.getLocale().getLanguage());
			GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
			Object doc = ((List<?>) viewer.getInput()).get(0);
			AUtil.simpleCopy(t, doc);
			viewer.refresh(doc);
		});
	}

}
