package com.bizvisionsoft.pms.revenue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.Brui;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.PermissionUtil;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.RevenueService;
import com.bizvisionsoft.service.model.AccountIncome;
import com.bizvisionsoft.service.model.IRevenueForecastScope;
import com.bizvisionsoft.service.model.RevenueForecastItem;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

/**
 * ��Ŀ����Ԥ��
 * 
 * @author hua
 *
 */
public class ForecastASM extends GridPart {

	private static final String DefaultType = "��";

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private IRevenueForecastScope scope;

	private String type;

	private int index;

	private RevenueService service;

	private Map<String, Map<Integer, Double>> data = new HashMap<>();

	@Init
	public void init() {
		service = Services.get(RevenueService.class);
		setContext(context);
		setConfig(context.getAssembly());
		setBruiService(br);
		scope = context.getRootInput(IRevenueForecastScope.class, false);
		type = scope.getRevenueForecastType();
		if (type.isEmpty()) {// ��һ�α༭����Ԥ��
			if (br.confirm("����Ԥ�ⷽʽ", "�����Ԥ�ⷽʽĬ���趨Ϊ����Ԥ�⣬�����趨��")) {
				selectType();
			}
			if (type.isEmpty()) {
				type = DefaultType;
			}
		}
		loadData();
		super.init();
	}

	private void loadData() {
		service.listRevenueForecast(scope.getScope_id()).forEach(rfi -> {
			Map<Integer, Double> row = data.get(rfi.getSubject());
			if (row == null) {
				row = new HashMap<Integer, Double>();
				data.put(rfi.getSubject(), row);
			}
			row.put(rfi.getIndex(), rfi.getAmount());
			data.put(rfi.getSubject(), row);
		});

	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}

	public void selectType() {
		Editor.open("ѡ������Ԥ�ⷽʽ", context, new BasicDBObject(), (r, d) -> {
			String type = r.getString("type");
			setType(type);
		});
	}

	private void setType(String type) {
		if (!this.type.isEmpty() && !this.type.equals(type) && br.confirm("����Ԥ�ⷽʽ", "�趨�µ�����Ԥ�ⷽʽ�������Ŀǰ��Ԥ�����ݣ���ȷ�ϡ�")) {
			service.clearRevenueForecast(scope.getScope_id());
			this.data.clear();
			Layer.message("����Ԥ�ⷽʽ���趨Ϊ��" + type);
		}
		this.type = type;
	}

	public void reset() {
		// ɾ����Ҫ��������
		GridColumn[] cols = viewer.getGrid().getColumns();
		for (int i = 0; i < index; i++) {
			cols[i + 3].dispose();
		}
		index = 0;
		appendAmountColumn();
		setViewerInput();
	}

	@Override
	protected GridTreeViewer createGridViewer(Composite parent) {
		viewer = new GridTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setAutoExpandLevel(GridTreeViewer.ALL_LEVELS);
		viewer.setUseHashlookup(false);

		Grid grid = viewer.getGrid();
		grid.setHeaderVisible(true);
		grid.setFooterVisible(false);
		grid.setLinesVisible(true);
		grid.setHideIndentionImage(true);
		UserSession.bruiToolkit().enableMarkup(grid);
		grid.setData(RWT.FIXED_COLUMNS, 3);

		return viewer;
	}

	@Override
	public void setViewerInput() {
		super.setViewerInput(Arrays.asList(scope));
		updateBackground();
	}

	@Override
	protected void createColumns(Grid grid) {

		/////////////////////////////////////////////////////////////////////////////////////
		// ������
		Column c = new Column();
		c.setName("name");
		c.setText("����");
		c.setWidth(240);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createColumn(grid, c);

		c = new Column();
		c.setName("id");
		c.setText("���");
		c.setWidth(64);
		c.setAlignment(SWT.CENTER);
		c.setMoveable(false);
		c.setResizeable(true);
		createColumn(grid, c);

		c = new Column();
		c.setName("total");
		c.setText("�ϼ�");
		c.setWidth(88);
		c.setMarkupEnabled(true);
		c.setAlignment(SWT.RIGHT);
		c.setMoveable(false);
		c.setResizeable(true);
		GridViewerColumn vcol = createColumn(grid, c);
		vcol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				Object element = cell.getElement();

				String text;
				double value = 0;
				for (int i = 0; i < index; i++) {
					value += getAmount(element, i);
				}
				if (value == 0) {
					text = "";
				} else {
					text = Util.getGenericMoneyFormatText(value);
				}

				cell.setText(text);
				cell.setBackground(BruiColors.getColor(BruiColor.Grey_50));
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return "total".equals(property);
			}

		});

		createAmountColumns();

	}

	private void createAmountColumns() {
		int count = service.getForwardRevenueForecastIndex(scope.getScope_id()) + 1;
		while (index < count) {
			appendAmountColumn();
		}
	}

	/**
	 * ׷��һ��
	 */
	public void appendAmountColumn() {
		Grid grid = viewer.getGrid();
		Column c = new Column();
		c.setName("" + index);
		c.setText("��" + (index + 1) + type);
		c.setWidth(88);
		c.setMarkupEnabled(true);
		c.setAlignment(SWT.RIGHT);
		c.setMoveable(false);
		c.setResizeable(true);
		GridViewerColumn vcol = createColumn(grid, c, 2 + this.index + 1);
		final int idx = this.index;
		vcol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				Object account = cell.getElement();

				String text = "";
				double value = getAmount(account, idx);
				if (value != 0)
					text = Util.getGenericMoneyFormatText(value);

				cell.setText(text);
				if (!isAmountEditable(account))
					cell.setBackground(BruiColors.getColor(BruiColor.Grey_50));
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return ("" + index).equals(property);
			}

		});
		vcol.setEditingSupport(supportEdit(vcol));

		index++;
	}

	private boolean isAmountEditable(Object account) {
		return account instanceof AccountIncome && ((AccountIncome) account).countSubAccountItems() == 0;
	}

	/**
	 * @param vcol
	 * @return
	 */
	protected EditingSupport supportEdit(GridViewerColumn vcol) {
		if (!hasPermission()) {
			return null;
		}

		final int index = Integer.parseInt((String) vcol.getColumn().getData("name"));
		return new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				try {
					update((AccountIncome) element, index, Util.getDoubleInput((String) value));
				} catch (Exception e) {
					Layer.message(e.getMessage(), Layer.ICON_CANCEL);
				}
			}

			@Override
			protected Object getValue(Object element) {
				double value = readAmount(((AccountIncome) element).getId(), index);
				return "" + value;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getGrid());
			}

			@Override
			protected boolean canEdit(Object element) {
				return isAmountEditable(element);
			}
		};
	}

	protected void update(AccountIncome account, int index, double amount) {
		// �������ݿ�
		RevenueForecastItem item = new RevenueForecastItem()//
				.setScope_id(scope.getScope_id())//
				.setIndex(index)//
				.setAmount(amount)//
				.setType(type)//
				.setSubject(account.getId());
		service.updateRevenueForecastItem(item);

		// ���»���
		Map<Integer, Double> row = data.get(account.getId());
		if (row == null) {
			row = new HashMap<Integer, Double>();
			data.put(account.getId(), row);
		}
		row.put(index, amount);
		// ˢ�±���
		while (this.index <= index) {
			appendAmountColumn();
		}

		ArrayList<Object> dirty = new ArrayList<>();
		dirty.add(account);
		GridItem treeItem = (GridItem) viewer.testFindItem(account);
		GridItem parentItem = treeItem.getParentItem();
		while (parentItem != null) {
			dirty.add(parentItem.getData());
			parentItem = parentItem.getParentItem();
		}
		List<String> properties = new ArrayList<>();
		properties.add("total");
		for (int i = 0; i < index; i++) {
			properties.add("" + index);
		}
		viewer.update(dirty.toArray(), properties.toArray(new String[0]));
	}

	public void updateBackground() {
		GridItem[] items = viewer.getGrid().getItems();
		updateBackground(items);
	}

	private void updateBackground(GridItem[] items) {
		for (int i = 0; i < items.length; i++) {
			GridItem[] children = items[i].getItems();
			if (children.length > 0) {
				items[i].setBackground(BruiColors.getColor(BruiColor.Grey_50));
				updateBackground(children);
			}
		}
	}

	private double readAmount(String subject, int index) {
		return service.getRevenueForecastAmount(scope.getScope_id(), subject, type, index);
	}

	private double getAmount(Object account, int index) {
		List<AccountIncome> children = null;
		if (account instanceof IRevenueForecastScope) {
			children = ((IRevenueForecastScope) account).getRootAccountIncome();
			return getRowSummaryAccount(children, index);
		} else if (account instanceof AccountIncome) {
			if (((AccountIncome) account).countSubAccountItems() > 0) {
				children = ((AccountIncome) account).getSubAccountItems();
				return getRowSummaryAccount(children, index);
			} else {
				return Optional.ofNullable(data.get(((AccountIncome) account).getId())).map(row -> row.get(index))
						.map(d -> d.doubleValue()).orElse(0d);
			}
		}
		return 0d;
	}

	private double getRowSummaryAccount(List<AccountIncome> children, int index) {
		double result = 0d;
		if (!Util.isEmptyOrNull(children)) {
			for (int i = 0; i < children.size(); i++) {
				result += getAmount(children.get(i), index);
			}
		}
		return result;
	}

	private boolean hasPermission() {
		// ���action��Ȩ��,ӳ�䵽action���м��
		IBruiContext context = getContext();
		Assembly assembly = context.getAssembly();
		List<Action> rowActions = assembly.getRowActions();
		if (rowActions == null || rowActions.isEmpty()) {
			return false;
		}
		return rowActions.stream().filter(a -> a.getName().equals(getEditbindingAction())).findFirst()
				.map(act -> PermissionUtil.checkAction(Brui.sessionManager.getUser(), act, context)).orElse(false);
	}

	protected String getEditbindingAction() {
		return "�༭����Ԥ��";
	}

}