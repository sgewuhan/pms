package com.bizvisionsoft.pms.formdef;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.Model;
import com.bizvisionsoft.bruiengine.ui.InformationDialog;
import com.bizvisionsoft.service.model.ExportDocRule;
import com.bizvisionsoft.service.model.FormDef;
import com.bizvisionsoft.service.model.Result;

public class FormDefTools {

	public static boolean checkExportDocRule(IBruiService br, ExportDocRule exportDocRule) {
		Assembly formDAssy = Model.getAssembly(exportDocRule.getFormDef().getEditorId());
		if (formDAssy == null) {
			Layer.error("无法获取表单定义所选编辑器");
			return false;
		}
		Map<String, String> formDFieldMap = ModelLoader.getEditorAssemblyFieldNameMap(formDAssy);

		List<Result> result = exportDocRule.check(formDFieldMap);

		return showResult(br, result, "表单定义检查", "表单定义");
	}

	public static boolean checkFormDef(IBruiService br, FormDef formDef) {
		Assembly formDAssy = Model.getAssembly(formDef.getEditorId());
		if (formDAssy == null) {
			Layer.error("无法获取表单定义所选编辑器");
			return false;
		}
		Map<String, String> formDFieldMap = ModelLoader.getEditorAssemblyFieldNameMap(formDAssy);

		List<Result> result = formDef.check(formDFieldMap);

		return showResult(br, result, "导出文档规则检查", "导出文档规则的");
	}

	private static boolean showResult(IBruiService br, List<Result> results, String title, String message) {
		if (results.size() > 0) {

			boolean error = false;
			boolean warning = false;
			for (Result result : results) {
				switch (result.type) {
				case Result.TYPE_ERROR:
					error = true;
					break;
				case Result.TYPE_WARNING:
					warning = true;
					break;
				}
			}
			if (error) {
				InformationDialog.openInfo(br.getCurrentShell(), title, message + " 存在以下问题需要解决。", results);
				return false;
			}
			if (warning)
				return IDialogConstants.OK_ID == InformationDialog.openConfirm(br.getCurrentShell(), title,
						message + " 存在以下问题。<br>是否继续？", results);
		}
		return true;
	}

}
