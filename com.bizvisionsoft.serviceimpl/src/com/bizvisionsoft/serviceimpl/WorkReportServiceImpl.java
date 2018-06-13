package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class WorkReportServiceImpl extends BasicServiceImpl implements WorkReportService {

	@SuppressWarnings("unchecked")
	@Override
	public List<WorkReport> createWorkReportDailyDataSet(BasicDBObject condition, String userid) {
		List<Bson> pipeline = (List<Bson>) new JQ("查询工作报告")
				.set("match", new Document("reporter", userid).append("type", WorkReport.TYPE_DAILY)).array();

		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		Integer skip = (Integer) condition.get("skip");
		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		Integer limit = (Integer) condition.get("limit");
		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		return c(WorkReport.class).aggregate(pipeline).into(new ArrayList<WorkReport>());
	}

	@Override
	public long countWorkReportDailyDataSet(BasicDBObject filter, String userid) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("reporter", userid);

		filter.put("type", WorkReport.TYPE_DAILY);
		return count(filter, WorkReport.class);
	}

	@Override
	public WorkReport insertWorkReport(WorkReport workReport) {
		WorkReport newWorkReport = insert(workReport);
		return getWorkReport(newWorkReport.get_id());
	}

	@Override
	public long deleteWorkReport(ObjectId _id) {
		return delete(_id, WorkReport.class);
	}

	@Override
	public long updateWorkReport(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkReport.class);
	}

	@Override
	public WorkReport getWorkReport(ObjectId _id) {
		List<? extends Bson> pipeline = new JQ("查询工作报告").set("match", new Document("_id", _id)).array();
		return c(WorkReport.class).aggregate(pipeline).first();
	}

	@Override
	public List<Work> createworkInReportDailyDataSet(BasicDBObject condition, ObjectId workReport_id) {
		List<? extends Bson> pipeline = new JQ("查询工作报告-日报工作").set("workReport_id", workReport_id).array();
		return c(Work.class).aggregate(pipeline).into(new ArrayList<Work>());
	}

	@Override
	public long countworkInReportDailyDataSet(BasicDBObject filter, ObjectId workReport_id) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("type", WorkReport.TYPE_DAILY);
		return count(filter, WorkReport.class);
	}

}
