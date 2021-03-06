package com.bizvisionsoft.pms.workpackage.action;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.UniversalCommand;
import com.bizvisionsoft.annotations.UniversalResult;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.UniversalDataService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.DocuSetting;
import com.bizvisionsoft.service.model.DocuTemplate;
import com.bizvisionsoft.service.model.Folder;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateDocuOfPackage {

	@Inject
	private IBruiService br;

	// 已经把模板和项目的分开，没有必要用Behavior设置，所以注释以下代码
	// @Behavior("输出文档/创建文档")
	// private boolean behaviour(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT)
	// Object input) {
	// return !(input instanceof ProjectTemplate);
	// }

	@Execute
	public void execute(final @MethodParam(Execute.CONTEXT) IBruiContext context) {
		// 检查是否存在文档设置
		WorkPackage wp = (WorkPackage) context.getInput();
		List<DocuSetting> setting = Services.get(DocumentService.class).listDocumentSetting(wp.get_id(),
				br.getDomain());
		if (setting.isEmpty()) {
			createGenaricDocument(context);
		} else {
			ActionMenu am = new ActionMenu(br);
			List<Action> actions = new ArrayList<>();

			actions.add(new ActionFactory().name("create").text("文档").normalStyle()
					.exec((e, c) -> createGenaricDocument(context)).get());
			setting.forEach(ds -> actions.add(new ActionFactory().name("create_" + ds.get_id()).text(ds.getName())
					.normalStyle().exec((e, c) -> createDocumentFromSetting(context, ds)).get()));

			am.setActions(actions).open();
		}
	}

	private void createDocumentFromSetting(IBruiContext context, DocuSetting ds) {
		WorkPackage wp = (WorkPackage) context.getInput();
		GridPart gridPart = (GridPart) context.getContent();
		ObjectId dt_id = ds.getDocuTemplate_id();
		DocuTemplate dt = Services.get(DocumentService.class).getDocumentTemplate(dt_id, br.getDomain());
		if (dt == null)
			throw new RuntimeException("无法获得文档模板，文档设置为：" + ds.getName());
		Docu docu = br.newInstance(Docu.class)//
				.setCreationInfo(br.operationInfo())//
				.addWorkPackageId(wp.get_id())//
				.setFolder_id(ds.getFolder_id())//
				.setName(wp.description)//
				.setTag(dt.getTag())//
				.setCategory(dt.getCategory());//

		String editorName = dt.getEditorName();
		if (Check.isAssigned(editorName)) {
			docu.setEditorName(editorName);// 保存编辑器名称
			Document encodeDocument = docu.encodeDocument();
			Editor.open(editorName, context, encodeDocument, true, (r, t) -> {
//				gridPart.insert(Services.get(DocumentService.class).createDocument(t, br.getDomain()));
				//TODO 产生文档编号
//				String id = docu.generateId();
//				t.append("id", id);
				insert(gridPart, t);
			});
		} else {
			Editor.open("通用文档编辑器.editorassy", context, docu, (r, t) -> {
				gridPart.insert(Services.get(DocumentService.class).createDocument(t, br.getDomain()));
			});
		}

	}

	private void insert(GridPart gridPart, Document t) {
		UniversalCommand command = new UniversalCommand().setTargetClassName(Docu.class.getName())
				.addParameter(MethodParam.OBJECT, t).setTargetCollection("docu");
		UniversalResult ur = Services.get(UniversalDataService.class).insert(command, br.getDomain());
		gridPart.insert(ur.getValue());
	}

	private void createGenaricDocument(final IBruiContext context) {
		WorkPackage wp = (WorkPackage) context.getInput();
		GridPart gridPart = (GridPart) context.getContent();
		ObjectId project_id = Services.get(WorkService.class).getProjectId(wp.getWork_id(), br.getDomain());
		// 选择文件夹
		Selector.open("项目文件夹选择.selectorassy", context, br.newInstance(Project.class).set_id(project_id), em -> {
			Docu docu = br.newInstance(Docu.class)//
					.setCreationInfo(br.operationInfo())//
					.addWorkPackageId(wp.get_id())//
					.setFolder_id(((Folder) em.get(0)).get_id())//
					.setName(wp.description);

			Editor.open("通用文档编辑器.editorassy", context, docu, (r, t) -> {
				gridPart.insert(Services.get(DocumentService.class).createDocument(t, br.getDomain()));
			});
		});
	}

}
