package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.tools.Formatter;

public class D4Renderer {

	public static Document renderCauseConsequence(Document doc, String lang) {
		StringBuffer sb = new StringBuffer();
		int rowHeight = RenderTools.margin * 3;

		String name = doc.getString("name");
		String desc = Optional.ofNullable(doc.getString("description")).orElse("");
		int w = doc.getInteger("weight", 1);
		String p = Formatter.getPercentageFormatString(doc.getDouble("probability"));
		String type;
		if ("因果分析-制造".equals(doc.getString("type"))) {
			type = "产生";
		} else if ("因果分析-流出".equals(doc.getString("type"))) {
			type = "流出";
		} else {
			type = "产生";// 根据不同类型
		}

		sb.append("<div style='display:flex;flex-direction:column;justify-content:space-around;color:white;'>");
		sb.append(
				"<div style='flex-grow:1;width:44px;display:flex;flex-direction:column;justify-content:space-around;align-items:center;background:#5c6bc0;margin-bottom:1px;border-radius:4px 0px 0px 0px;'>");
		sb.append("<div class='label_caption'>" + type + "</div></div>");
		sb.append(
				"<div style='flex-grow:1;width:44px;display:flex;flex-direction:column;justify-content:space-around;align-items:center;background:#5c6bc0;margin-bottom:1px;'>");
		sb.append("<div class='label_caption'>" + w + "</div><div class='label_caption'>权重</div></div>");
		sb.append(
				"<div style='flex-grow:1;width:44px;display:flex;flex-direction:column;justify-content:space-around;align-items:center;background:#5c6bc0;;border-radius:0px 0px 0px 4px;'>");
		sb.append("<div class='label_caption'>" + p + "</div><div class='label_caption'>概率</div></div>");
		sb.append("</div>");

		sb.append("<div style='width:0;flex-grow:1;padding:0px 4px;display:flex;flex-direction:column;justify-content:space-around;'>");
		sb.append("<div class='brui_text_line'>" + name + "</div><div class='brui_card_text3 label_caption' style='height:48px;'>" + desc
				+ "</div>");
		sb.append("</div>");

		rowHeight += 82;

		sb.insert(0, "<div class='brui_card_trans' style='display:flex;background:#f9f9f9;height:" + (rowHeight - 2 * RenderTools.margin)
				+ "px;margin:" + RenderTools.margin + "px;'>");
		sb.append("</div>");

		return new Document("_id", doc.get("_id")).append("html", sb.toString()).append("height", rowHeight);
	}

}
