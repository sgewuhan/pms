package com.bizvisionsoft.pms.problem;

import java.util.Date;
import java.util.List;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class ConfirmD3ICAACT {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.PAGE_CONTEXT_INPUT_OBJECT) Problem problem) {
		if (br.confirm("客户确认", "请确认所有临时处理措施已经经客户确认。")) {
			List<Result> result = Services.get(ProblemService.class)
					.confirmProblem(br.command(problem.get_id(), new Date(), ICommand.D3ICA_Confirm));
			if (result.isEmpty()) {
				Layer.message("临时处理措施已确认");
			}
		}
	}
}
