package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.model.WorkReportItem;
import com.bizvisionsoft.service.model.WorkResourceAssignment;
import com.bizvisionsoft.service.model.WorkResourceInWorkReport;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class WorkReportServiceImpl extends BasicServiceImpl implements WorkReportService {

	@SuppressWarnings("unchecked")
	@Override
	public List<WorkReport> createWorkReportDailyDataSet(BasicDBObject condition, String userid) {
		List<Bson> pipeline = (List<Bson>) new JQ("��ѯ��������")
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
	public WorkReport insert(WorkReport workReport) {
		// {
		// "summary" : false,
		// "distributed" : true,
		// "stage" : {"$ne" : true},
		// project_id:ObjectId("5b04d2c17fbf7437fc199880"),
		// actualStart:{$ne:null},
		// $or:[{actualFinish:null},{actualFinish:ISODate("2018-06-14T01:14:44.661+0000")}]
		// }
		if (c("workReport").count(new Document("project_id", workReport.getProject_id())
				.append("period", workReport.getPeriod()).append("type", workReport.getType())) > 0) {
			throw new ServiceException("�Ѿ��������档");
		}

		Document query = new Document();
		query.append("summary", false);
		query.append("distributed", true);
		query.append("stage", new Document("$ne", true));
		query.append("project_id", workReport.getProject_id());
		query.append("actualStart", new Document("$ne", null));
		query.append("$and",
				Arrays.asList(
						new Document("$or",
								Arrays.asList(new Document("chargerId", workReport.getReporter()),
										new Document("assignerId", workReport.getReporter()))),
						new Document("$or", Arrays.asList(new Document("actualFinish", null),
								new Document("actualFinish", workReport.getPeriod())))));
		if (c("work").count(query) == 0) {
			throw new ServiceException("û����Ҫ��д����Ĺ�����");
		}
		WorkReport newWorkReport = super.insert(workReport);
		c("work").find(query).forEach((Document doc) -> {
			c("workReportItem").insertOne(new Document().append("work_id", doc.get("_id"))
					.append("report_id", newWorkReport.get_id()).append("reporter", newWorkReport.getReporter()));
		});
		return listInfo(newWorkReport.get_id()).get(0);
	}

	@Override
	public long delete(ObjectId _id) {
		return delete(_id, WorkReport.class);
	}

	@Override
	public long update(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkReport.class);
	}

	@Override
	public List<WorkReport> listInfo(ObjectId _id) {
		return c(WorkReport.class).aggregate(new JQ("��ѯ��������").set("match", new Document("_id", _id)).array())
				.into(new ArrayList<>());
	}

	@Override
	public List<WorkReportItem> listDailyReportItem(ObjectId report_id) {
		List<? extends Bson> pipeline = Arrays.asList(
				new Document().append("$match", new Document().append("report_id", report_id)),
				new Document().append("$lookup",
						new Document().append("from", "work").append("localField", "work_id")
								.append("foreignField", "_id").append("as", "work")),
				new Document().append("$unwind",
						new Document().append("path", "$work").append("preserveNullAndEmptyArrays", false)));

		ArrayList<WorkReportItem> result = c(WorkReportItem.class).aggregate(pipeline).into(new ArrayList<>());
		return result;
	}

	@Override
	public long countDailyReportItem(ObjectId workReport_id) {
		return count(new BasicDBObject("type", WorkReport.TYPE_DAILY).append("report_id", workReport_id),
				WorkReport.class);
	}

	@Override
	public long updateWorkInReport(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkReportItem.class);
	}

	@Override
	public List<WorkResourceInWorkReport> createWorkResourceInWorkReportDataSet(ObjectId work_id,
			ObjectId workReport_id) {
		List<? extends Bson> pipeline = new JQ("��ѯ��������-��Դ����")
				.set("match", new Document("work_id", work_id).append("workReport_id", workReport_id)).array();
		ArrayList<WorkResourceInWorkReport> into = c(WorkResourceInWorkReport.class).aggregate(pipeline)
				.into(new ArrayList<WorkResourceInWorkReport>());
		return into;
	}

	@Override
	public long countWorkResourceInWorkReportDataSet(ObjectId work_id, ObjectId workReport_id) {
		List<? extends Bson> pipeline = new JQ("��ѯ��������-��Դ����")
				.set("match", new Document("work_id", work_id).append("workReport_id", workReport_id)).array();
		return c(WorkResourceInWorkReport.class).aggregate(pipeline).into(new ArrayList<WorkResourceInWorkReport>())
				.size();
	}

	@Override
	public List<WorkResourceInWorkReport> listSubWorkResourceInWorkReport(
			WorkResourceAssignment workResourceAssignment) {
		// TODO Auto-generated method stub
		return c(WorkResourceInWorkReport.class).find(workResourceAssignment.getQuery())
				.into(new ArrayList<WorkResourceInWorkReport>());
	}

	@Override
	public long countSubWorkResourceInWorkReport(WorkResourceAssignment workResourceAssignment) {
		// TODO Auto-generated method stub
		return c(WorkResourceInWorkReport.class).count(workResourceAssignment.getQuery());
	}

}