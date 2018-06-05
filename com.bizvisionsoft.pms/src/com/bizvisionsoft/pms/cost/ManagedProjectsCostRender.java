package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.grid.GridColumn;

import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnFooter;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnHeader;
import com.bizvisionsoft.annotations.ui.grid.GridRenderCompare;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.GridPartDefaultRender;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSSubjectCost;
import com.bizvisionsoft.serviceconsumer.Services;

public class ManagedProjectsCostRender extends GridPartDefaultRender {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService bruiService;

	private String result;

	@Init
	private void init() {
		// TODO 展开区间与当前时间的判断
		
		Date date = null;
		Object input = context.getInput();
		if (input != null) {
			if (input instanceof CBSItem) {
				CBSItem cbsItem = (CBSItem) input;
				date =cbsItem.getSettlementDate();
			}
		}
		if (result == null) {
			date = Services.get(CommonService.class).getCurrentCBSPeriod();
		}
		Calendar currentCBSPeriod = Calendar.getInstance();
		currentCBSPeriod.setTime(date);
		result = "" + currentCBSPeriod.get(Calendar.YEAR);
		result += String.format("%02d", currentCBSPeriod.get(java.util.Calendar.MONTH) + 1);
	}

	@Override
	@GridRenderUpdateCell
	public void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell,
			@MethodParam(GridRenderUpdateCell.PARAM_COLUMN) Column column,
			@MethodParam(GridRenderUpdateCell.PARAM_VALUE) Object value,
			@MethodParam(GridRenderUpdateCell.PARAM_IMAGE) Object image) {
		Object element = cell.getElement();
		if ("periodCost".equals(column.getName())) {
			if (element instanceof CBSItem) {
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCost(result);
			} else if (element instanceof CBSSubjectCost) {
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCost(result);
			}
		} else if ("totalCost".equals(column.getName())) {
			if (element instanceof CBSItem) {
				CBSItem cbsItem = (CBSItem) element;
				value = cbsItem.getCostSummary();
			} else if (element instanceof CBSSubjectCost) {
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) element;
				value = cbsSubjectCost.getCostSummary();
			}
		}
		super.renderCell(cell, column, value, image);
	}

	@Override
	@GridRenderColumnHeader
	public void renderColumnHeader(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		if ("periodCost".equals(column.getName())) {
			column.setText(result.substring(0, 4) + "/" + result.substring(4, 6));
		}
		super.renderColumnHeader(col, column);
	}

	@Override
	@GridRenderColumnFooter
	public void renderColumnFooter(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		super.renderColumnFooter(col, column);
	}

	@Override
	@GridRenderCompare
	public int compare(@MethodParam(GridRenderCompare.PARAM_COLUMN) Column col,
			@MethodParam(GridRenderCompare.PARAM_ELEMENT1) Object e1,
			@MethodParam(GridRenderCompare.PARAM_ELEMENT2) Object e2) {
		return super.compare(col, e1, e2);
	}

}
