package com.bizvisionsoft.pms.project;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bizivisionsoft.widgets.carousel.Carousel;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.model.Project;

public class ProjecBasicIndicatorsWidgetASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private Project project;

	@Init
	private void init() {
		project = context.getRootInput(Project.class, false);
	}

	@CreateUI
	public void createUI(Composite parent) {
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		FormLayout layout = new FormLayout();
		parent.setLayout(layout);

		Label title = new Label(parent, SWT.NONE);
		title.setText("��Ҫָ��");
		title.setForeground(BruiColors.getColor(BruiColor.Grey_600));

		FormData fd = new FormData();
		title.setLayoutData(fd);
		fd.left = new FormAttachment(0, 8);
		fd.top = new FormAttachment(0, 8);
		fd.right = new FormAttachment(100, -8);

		Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setBackground(BruiColors.getColor(BruiColor.Grey_50));

		fd = new FormData();
		sep.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(title, 4);
		fd.right = new FormAttachment(100);
		fd.height = 1;

		Carousel carousel = new Carousel(parent, SWT.NONE);
		carousel.setAnimation("default");
		carousel.setInterval(5000);
		carousel.setIndicator("none");

		Composite page = createPage(parent, carousel, 3);
		addIndicator(page, Util.getFormatText(project.getPlanStart()), "�ƻ���ʼ");
		addIndicator(page, Util.getFormatText(project.getPlanFinish()), "�ƻ����");
		addIndicator(page, project.getPlanDuration() + "��", "�ƻ�����");
		String overdue = getOverdueHtml();
		if ("����".equals(overdue)) {
			addIndicator(page, overdue, "����Ԥ��", "layui-bg-orange", "#ffffff", "#ffffff");
		} else if ("��".equals(overdue)) {
			addIndicator(page, overdue, "����Ԥ��", "layui-bg-red", "#ffffff", "#ffffff");
		} else if ("��".equals(overdue)) {
			addIndicator(page, overdue, "����Ԥ��", "layui-bg-orange", "#ffffff", "#ffffff");
		} else if ("��".equals(overdue)) {
			addIndicator(page, overdue, "����Ԥ��", "layui-bg-blue", "#ffffff", "#ffffff");
		} else {
			addIndicator(page, "", "����Ԥ��");
		}
		addIndicator(page, Util.getFormatText(project.getEstimateFinish()), "�����깤����");

		addIndicator(page, Util.getFormatText(project.getEstimateDuration()) + "��", "���㹤��");

		page = createPage(parent, carousel, 2);

		List<List<Double>> values = project.getDurationForcast();
		if (values != null) {
			// �ֹ�
			Double t = values.get(0).get(0);
			Calendar cal = Calendar.getInstance();
			cal.setTime(project.getPlanStart());
			cal.add(Calendar.DATE, t.intValue());
			String text = Util.getFormatText(cal.getTime()) + "<br/>"
					+ toString(values.get(0).get(1).doubleValue() / 100);
			addIndicator(page, text, "�ֹ۹���", "brui_bg_lightgrey", "#757575", "#009688");

			// ����
			t = values.get(1).get(0);
			cal.setTime(project.getPlanStart());
			cal.add(Calendar.DATE, t.intValue());
			text = Util.getFormatText(cal.getTime());
			addIndicator(page, text, "���۹���", "brui_bg_lightgrey", "#757575", "#ff9800");

			// �����
			t = values.get(2).get(0);
			cal.setTime(project.getPlanStart());
			cal.add(Calendar.DATE, t.intValue());
			text = Util.getFormatText(cal.getTime()) + "<br/>" + toString(values.get(2).get(1).doubleValue() / 100);
			addIndicator(page, text, "����ܵ��깤����", "brui_bg_lightgrey", "#757575", "#03a9f4");
		}else {
			addIndicator(page, "��δ����", "�ֹ۹���");
			addIndicator(page, "��δ����", "���۹���");
			addIndicator(page, "��δ����", "����ܵ��깤����");

		}

		Double probability = project.getDurationProbability();
		if (probability == null) {
			addIndicator(page, "��δ����", "���ڼƻ��깤����");
		} else {
			String text = Util.getFormatText(project.getPlanFinish()) + "<br/>" + toString(probability)+"</span>";
			if (probability >= 0.9) {
				addIndicator(page, text, "���ƻ��깤����", "layui-bg-green", "#ffffff", "#ffffff");
			} else if (probability >= 0.7) {
				addIndicator(page, text, "���ƻ��깤����", "layui-bg-blue", "#ffffff", "#ffffff");
			} else if (probability >= 0.5) {
				addIndicator(page, text, "���ƻ��깤����", "layui-bg-orange", "#ffffff", "#ffffff");
			} else {
				addIndicator(page, text, "�����깤����", "layui-bg-red", "#ffffff", "#ffffff");
			}
		}

		page = createPage(parent, carousel, 2);
		addIndicator(page, toString(project.getWAR()), "�������������");
		addIndicator(page, toString(project.getDAR()), "���������");
		addIndicator(page, toString(project.getCAR()), "�ɱ����������");
		addIndicator(page, toString(project.getBDR()), "Ԥ��ƫ����");

		page = createPage(parent, carousel, 2);

		addIndicator(page, toString(project.getSAR()), "�ƻ������");
		addIndicator(page, toString(0.12), "һ���ƻ����������");// TODO

		new Composite(page, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		new Composite(page, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fd = new FormData();
		carousel.setLayoutData(fd);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(sep);
		fd.bottom = new FormAttachment(100);
	}

	private Composite createPage(Composite parent, Carousel carousel, int colCount) {
		Composite page = carousel.addPage(new Composite(carousel, SWT.NONE));
		page.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridLayout layout = new GridLayout(colCount, true);
		layout.horizontalSpacing = 16;
		layout.verticalSpacing = 16;
		layout.marginHeight = 16;
		layout.marginWidth = 16;
		page.setLayout(layout);
		return page;
	}

	private String toString(Object ind) {
		return Optional.ofNullable(ind).map(d -> new DecimalFormat("#0.0%").format(d)).orElse("</br>");
	}

	private Control addIndicator(Composite parent, String ind, String title, String css, String titleColor,
			String textColor) {
		Label btn = new Label(parent, SWT.CENTER);
		UserSession.bruiToolkit().enableMarkup(btn);
		btn.setHtmlAttribute("class", css);
		StringBuffer sb = new StringBuffer();
		sb.append("<div style='margin-top:8px;color:" + titleColor + ";'>" + title + "</div>");
		sb.append("<div style='font-size:24px;text-align:center;color:" + textColor + ";margin-top:8px;'>" + ind
				+ "</div>");
		btn.setText(sb.toString());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		btn.setLayoutData(data);
		return btn;
	}

	private Control addIndicator(Composite parent, String ind, String title) {
		return addIndicator(parent, ind, title, "brui_bg_lightgrey", "#757575", "#009688");
	}

	private String getOverdueHtml() {
		Date _actual = project.getActualFinish();
		Date _plan = project.getPlanFinish();
		if (_actual == null) {
			_actual = new Date();
		}
		// �����ǰʱ������ʱ���Ѿ������˼ƻ���ɣ���ʾΪ����
		if (_actual.after(_plan)) {
			return "����";
		}

		if (project.getOverdueIndex() != null) {
			switch (project.getOverdueIndex()) {
			case 0:
				return "��";
			case 1:
				return "��";
			case 2:
				return "��";
			}
		}
		return "";
	}

}