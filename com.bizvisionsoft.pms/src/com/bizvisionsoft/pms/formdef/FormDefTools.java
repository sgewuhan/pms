package com.bizvisionsoft.pms.formdef;

import java.util.List;
import java.util.Map;

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
			Layer.error("�޷���ȡ����������ѡ�༭��");
			return false;
		}
		Map<String, String> formDFieldMap = ModelLoader.getEditorAssemblyFieldNameMap(formDAssy);

		List<Result> result = exportDocRule.check(formDFieldMap);

		return showResult(br, result, "����������", "����������������");
	}

	public static boolean checkFormDef(IBruiService br, FormDef formDef) {
		Assembly formDAssy = Model.getAssembly(formDef.getEditorId());
		if (formDAssy == null) {
			Layer.error("�޷���ȡ����������ѡ�༭��");
			return false;
		}
		Map<String, String> formDFieldMap = ModelLoader.getEditorAssemblyFieldNameMap(formDAssy);

		List<Result> result = formDef.check(formDFieldMap);

		return showResult(br, result, "�����ĵ�������", "��鵼���ĵ�����������");
	}

	private static boolean showResult(IBruiService br, List<Result> results, String title, String message) {
		
		//TODO ������ʾ
		InformationDialog.openConfirm(br.getCurrentShell(), title, message, results);

		// boolean error = false;
		// boolean warning = false;
		// Map<String, String> map = new HashMap<String, String>();
		// for (Result r : result) {// ѭ�������
		// BasicDBObject data = r.data;
		// switch (r.type) {
		// case Result.TYPE_ERROR:
		// error = true;
		// if ("nullField".equals(data.getString("type"))) {
		// String message = map.get("nullField");
		// if (message == null)
		// message = "";
		// else
		// message += ",";
		// message += data.getString("editorId");
		// map.put("nullField", message);
		// } else if ("errorSameField".equals(data.getString("type"))) {
		// String message = map.get("errorSameField");
		// if (message == null)
		// message = "";
		// else
		// message += ",";
		// message += data.getString("editorId");
		// map.put("errorSameField", message);
		// } else if ("errorField".equals(data.getString("type"))) {
		// String message = map.get("errorField");
		// if (message == null)
		// message = "";
		// else
		// message += ",";
		// message += data.getString("editorId");
		// map.put("errorField", message);
		// } else if ("errorExportableField".equals(data.getString("type"))) {
		// String message = map.get("errorExportableField");
		// if (message == null)
		// message = "";
		// else
		// message += ",";
		// message += data.getString("editorId");
		// map.put("errorExportableField", message);
		// }
		// break;
		// case Result.TYPE_WARNING:
		// warning = true;
		// String message = map.get("warning");
		// if (message == null)
		// message = "";
		// else
		// message += ",";
		// message += data.getString("editorId");
		// map.put("errorExportableField", message);
		// break;
		// }
		// }
		// if (error) {
		// StringBuffer sb = new StringBuffer();
		// sb.append("<span class='layui-badge'>����</span><br/>");
		// for (String key : map.keySet()) {
		// if ("nullField".equals(key)) {
		// sb.append("�ĵ���������: ");
		// sb.append(map.get(key));
		// sb.append(" �д����ֶ���Ϊ�յ��ֶΡ�<br/>");
		// } else if ("errorSameField".equals(key)) {
		// sb.append("�ĵ���������: ");
		// sb.append(map.get(key));
		// sb.append(" �����������ֶ����á�<br/>");
		// } else if ("errorField".equals(key)) {
		// sb.append("�ĵ���������: ");
		// sb.append(map.get(key));
		// sb.append(" �ֶ������д��ڱ���������û�е��ֶΣ��޷������ĵ���<br/>");
		// } else if ("errorExportableField".equals(key)) {
		// sb.append("�ĵ���������: ");
		// sb.append(map.get(key));
		// sb.append(" �ֶ������뵼�����ò�һ��,�޷��������ļ���<br/>");
		// }
		// }
		// br.error("����������", sb.toString());
		// return false;
		// } else if (warning) {
		// StringBuffer sb = new StringBuffer();
		// sb.append("<span class='layui-badge layui-bg-blue'>����</span><br/>�ĵ���������: ");
		// sb.append(map.get("warning"));
		// sb.append(" ���������е��ֶ�û����ȫӳ�䵽�ĵ���");
		// return br.confirm("����������", sb.toString() + "<br>�Ƿ������");
		// }

		return true;
	}

}