package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.model.VaultFolder;
import com.bizvisionsoft.serviceconsumer.Services;

public class ContainerExplorer extends VaultExplorer {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	public ContainerExplorer() {
	}

	@Init
	protected void init() {
		setContext(context);
		setBruiService(br);
		super.init();
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}

	@Override
	public IFolder getInitialFolder() {
		return IFolder.Null;
	}

	@Override
	protected IFolder[] getPath(IFolder folder) {
		if (IFolder.Null.equals(folder)) {
			return new IFolder[0];
		} else {
			List<VaultFolder> result = Services.get(DocumentService.class).getPath(folder.get_id(), br.getDomain());
			return result.toArray(new IFolder[0]);
		}
	}

	@Override
	protected Assembly getNavigatorAssembly() {
		return (Assembly) br.getAssembly("vault/目录导航.gridassy").clone();
	}

	@Override
	protected Assembly getFileTableAssembly() {
		return (Assembly) br.getAssembly("vault/文档列表.gridassy").clone();
	}

	@Override
	protected Assembly getSearchFolderAssembly() {
		return (Assembly) br.getAssembly("vault/目录查询结果.gridassy").clone();
	}

	@Override
	protected Assembly getSearchFileAssembly() {
		return (Assembly) br.getAssembly("vault/文档查询结果.gridassy").clone();
	}

	@Override
	protected List<List<Action>> createToolbarActions() {
		List<List<Action>> result = new ArrayList<>();

		List<Action> actions = new ArrayList<Action>();
		actions.add(VaultActions.create(VaultActions.createSubFolder, true, true));
		actions.add(VaultActions.create(VaultActions.createDocument, true, true));
		result.add(actions);

		actions = new ArrayList<Action>();
		actions.add(VaultActions.create(VaultActions.searchFolder, true, true));
		actions.add(VaultActions.create(VaultActions.findDocuments, true, true));
		actions.add(VaultActions.create(VaultActions.search, true, false));
		result.add(actions);

		actions = new ArrayList<Action>();
		actions.add(VaultActions.create(VaultActions.sortDocuments, true, false));
//		actions.add(VaultActions.create(VaultActions.addFavour, true, false));
//		actions.add(VaultActions.create(VaultActions.setFolderProperties, true, false));
		result.add(actions);
		return result;
	}

	@Override
	protected int getStyle() {
		return super.getStyle() | SEARCH_FOLDER | SEARCH_FILE;
	}

}
