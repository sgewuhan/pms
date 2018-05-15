package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSPeriod;
import com.bizvisionsoft.service.model.CBSSubject;
import com.bizvisionsoft.service.model.Work;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.result.UpdateResult;

public class CBSServiceImpl extends BasicServiceImpl implements CBSService {

	@Override
	public CBSItem get(ObjectId _id) {
		return query(new BasicDBObject("_id", _id)).first();
	}

	public AggregateIterable<CBSItem> query(BasicDBObject match) {
		/*
		 * db.getCollection('cbs').aggregate([
		 * {$lookup:{"from":"cbsPeriod","localField":"_id","foreignField":"cbsItem_id",
		 * "as":"_period"}},
		 * {$addFields:{_budget:{$map:{"input":"$_period","as":"itm","in":{"k":
		 * "$$itm.id","v":"$$itm.budget"}}}}},
		 * {$addFields:{"budgetTotal":{$sum:"$_budget.v"}}}, {$addFields:{"budget":{
		 * $arrayToObject: "$_budget" }}},
		 * {$lookup:{"from":"cbs","localField":"_id","foreignField":"parent_id","as":
		 * "_children"}}, {$addFields:{"children":{$map:{input: "$_children._id",as:
		 * "id",in: "$$id" }}}},
		 * {$project:{"_period":false,"_budget":false,"_children":false}}, ])
		 */

		List<Bson> pipeline = new ArrayList<Bson>();
		if (match != null)
			pipeline.add(Aggregates.match(match));

		pipeline.add(Aggregates.lookup("cbsPeriod", "_id", "cbsItem_id", "_period"));

		pipeline.add(Aggregates.addFields(//
				new Field<BasicDBObject>("_budget",
						new BasicDBObject("$map", new BasicDBObject("input", "$_period").append("as", "itm")
								.append("in", new BasicDBObject("k", "$$itm.id").append("v", "$$itm.budget"))))));

		pipeline.add(Aggregates.addFields(//
				new Field<BasicDBObject>("budgetTotal", new BasicDBObject("$sum", "$_budget.v"))));

		pipeline.add(Aggregates.addFields(//
				new Field<BasicDBObject>("budget", new BasicDBObject("$arrayToObject", "$_budget"))));

		pipeline.add(Aggregates.lookup("cbs", "_id", "parent_id", "_children"));

		pipeline.add(Aggregates.addFields(//
				new Field<BasicDBObject>("children", new BasicDBObject("$map", new BasicDBObject()
						.append("input", "$_children._id").append("as", "id").append("in", "$$id")))));

		pipeline.add(Aggregates
				.project(new BasicDBObject("_period", false).append("_budget", false).append("_children", false)));

		AggregateIterable<CBSItem> a = c(CBSItem.class).aggregate(pipeline);
		return a;
	}

	// /**
	// * db.getCollection('accountItem').aggregate([ {$match:{parent_id:null}},
	// * {$project:{_id:false,name:true,id:true,parent_id:"aaa",scope_id:"bbb"}} ])
	// *
	// * @param items
	// * @param scope_id
	// * @param parent_id
	// * @param acountItemParent_id
	// */
	// public void appendSubItemsFromTemplate(List<CBSItem> items, ObjectId
	// scope_id, ObjectId parent_id,
	// ObjectId acountItemParent_id) {
	// Iterable<Document> iter = c("accountItem").find(new
	// BasicDBObject("parent_id", acountItemParent_id));
	// iter.forEach(d -> {
	// ObjectId _id = new ObjectId();
	// CBSItem item = new CBSItem().set_id(_id)//
	// .setName(d.getString("name"))//
	// .setId(d.getString("id"))//
	// .setScope_id(scope_id)//
	// .setParent_id(acountItemParent_id);//
	// items.add(item);
	// appendSubItemsFromTemplate(items, scope_id, _id, d.getObjectId("_id"));
	// });
	// }

	@Override
	public List<CBSItem> getScopeRoot(ObjectId scope_id) {
		return query(new BasicDBObject("scope_id", scope_id).append("scopeRoot", true)).into(new ArrayList<CBSItem>());
	}

	@Override
	public CBSItem insertCBSItem(CBSItem o) {
		CBSItem item = insert(o, CBSItem.class);
		return query(new BasicDBObject("_id", item.get_id())).first();
	}

	@Override
	public List<CBSItem> getSubCBSItems(ObjectId parent_id) {
		return query(new BasicDBObject("parent_id", parent_id)).into(new ArrayList<CBSItem>());
	}

	@Override
	public long countSubCBSItems(ObjectId parent_id) {
		return count(new BasicDBObject("parent_id", parent_id), CBSItem.class);
	}

	@Override
	public void delete(ObjectId _id) {
		ArrayList<ObjectId> deletecbsIds = new ArrayList<ObjectId>();
		deletecbsIds.add(_id);
		listDescendants(_id, cId -> deletecbsIds.add(cId));

		ArrayList<ObjectId> deletePeriodIds = c(CBSPeriod.class)
				.distinct("_id", new Document("cbsItem_id", new Document("$in", deletecbsIds)), ObjectId.class)
				.into(new ArrayList<ObjectId>());

		if (!deletePeriodIds.isEmpty())
			c(CBSPeriod.class).deleteMany(new Document("_id", new Document("$in", deletePeriodIds)));

		c(CBSItem.class).deleteMany(new Document("_id", new Document("$in", deletecbsIds)));
	}

	private void listDescendants(ObjectId cbsId, Consumer<ObjectId> consumer) {
		Iterable<Document> iter = c("cbs").find(new Document("parent_id", cbsId)).projection(new Document("_id", true));
		iter.forEach(e -> {
			ObjectId id = e.getObjectId("_id");
			consumer.accept(id);
			listDescendants(id, consumer);
		});
	}

	@Override
	public ObjectId updateCBSPeriodBudget(CBSPeriod o) {
		Document filter = new Document("cbsItem_id", o.getCbsItem_id()).append("id", o.getId());
		ObjectId _id = Optional.ofNullable(c("cbsPeriod").find(filter).first()).map(d -> d.getObjectId("_id"))
				.orElse(null);
		if (_id == null) {
			_id = new ObjectId();
			c("cbsPeriod").insertOne(
					filter.append("_id", _id).append("budget", Optional.ofNullable(o.getBudget()).orElse(0d)));
		} else {
			c("cbsPeriod").updateOne(filter,
					new Document("$set", new Document("budget", Optional.ofNullable(o.getBudget()).orElse(0d))));
		}
		return _id;
	}

	@Override
	public CBSSubject upsertCBSSubjectBudget(CBSSubject o) {
		Document filter = new Document("cbsItem_id", o.getCbsItem_id()).append("id", o.getId()).append("subjectNumber",
				o.getSubjectNumber());
		ObjectId _id = Optional.ofNullable(c("cbsSubject").find(filter).first()).map(d -> d.getObjectId("_id"))
				.orElse(null);
		if (_id == null) {
			_id = new ObjectId();
			c("cbsSubject").insertOne(
					filter.append("_id", _id).append("budget", Optional.ofNullable(o.getBudget()).orElse(0d)));
		} else {
			c("cbsSubject").updateOne(filter,
					new Document("$set", new Document("budget", Optional.ofNullable(o.getBudget()).orElse(0d))));
		}

		return get(_id, CBSSubject.class);
	}

	@Override
	public List<CBSItem> createDataSet(BasicDBObject filter) {
		return query(filter).into(new ArrayList<CBSItem>());
	}

	@Override
	public List<CBSSubject> getSubjectBudget(ObjectId cbs_id) {
		return c(CBSSubject.class).find(new BasicDBObject("cbsItem_id", cbs_id)).into(new ArrayList<CBSSubject>());
	}

	@Override
	public CBSItem allocateBudget(ObjectId _id, ObjectId scope_id, String scopename) {
		UpdateResult ur = c(Work.class).updateOne(new BasicDBObject("_id", scope_id),
				new BasicDBObject("$set", new BasicDBObject("cbs_id", _id)));
		ur = c(CBSItem.class).updateOne(new BasicDBObject("_id", _id), new BasicDBObject("$set",
				new BasicDBObject("scope_id", scope_id).append("scopename", scopename).append("scopeRoot", true)));
		if (ur.getModifiedCount() == 0) {

		}
		List<ObjectId> list = new ArrayList<ObjectId>();
		list.add(_id);
		List<ObjectId> desentItems = getDesentItems(list, "cbs", "parent_id");
		ur = c(CBSItem.class).updateMany(new BasicDBObject("_id", new BasicDBObject("$in", desentItems)),
				new BasicDBObject("$set", new BasicDBObject("scope_id", scope_id).append("scopename", scopename)));
		// TODO ���󷵻�
		if (ur.getModifiedCount() == 0) {

		}
		return get(_id);
	}

	@Override
	public CBSItem unallocateBudget(ObjectId _id, ObjectId parent_id) {
		CBSItem parent = get(parent_id);
		UpdateResult ur = c(Work.class).updateOne(new BasicDBObject("cbs_id", _id),
				new BasicDBObject("$unset", new BasicDBObject("cbs_id", 1)));
		ur = c(CBSItem.class).updateOne(new BasicDBObject("_id", _id),
				new BasicDBObject("$set", new BasicDBObject("scope_id", parent.getScope_id())
						.append("scopename", parent.getScopeName()).append("scopeRoot", false)));

		if (ur.getModifiedCount() == 0) {

		}
		List<ObjectId> list = new ArrayList<ObjectId>();
		list.add(_id);
		List<ObjectId> desentItems = getDesentItems(list, "cbs", "parent_id");
		ur = c(CBSItem.class).updateMany(new BasicDBObject("_id", new BasicDBObject("$in", desentItems)),
				new BasicDBObject("$set", new BasicDBObject("scope_id", parent.getScope_id()).append("scopename",
						parent.getScopeName())));
		// TODO ���󷵻�

		if (ur.getModifiedCount() == 0) {

		}
		return get(_id);
	}

	@Override
	public CBSItem calculationBudget(ObjectId _id) {
		List<CBSSubject> subjectBudget = getSubjectBudget(_id);
		Map<String, Double> cbsPeriodMap = new HashMap<String, Double>();
		for (CBSSubject cbsSubject : subjectBudget) {
			String id = cbsSubject.getId();
			Double budget = cbsPeriodMap.get(id);
			cbsPeriodMap.put(id,
					(cbsSubject.getBudget() != null ? cbsSubject.getBudget() : 0) + (budget != null ? budget : 0));
		}
		c(CBSPeriod.class).deleteMany(new BasicDBObject("cbsItem_id", _id));

		List<CBSPeriod> cbsPeriods = new ArrayList<CBSPeriod>();
		for (String id : cbsPeriodMap.keySet()) {
			CBSPeriod cbsPeriod = new CBSPeriod();
			cbsPeriod.setCBSItem_id(_id);
			cbsPeriod.setId(id);
			cbsPeriod.setBudget(cbsPeriodMap.get(id));
			cbsPeriods.add(cbsPeriod);
		}
		c(CBSPeriod.class).insertMany(cbsPeriods);

		return get(_id);
	}

	@Override
	public CBSItem addCBSItemByStage(ObjectId _id, ObjectId project_id) {
		CBSItem parentCBSItem = get(_id);
		List<Work> workList = c(Work.class)
				.find(new BasicDBObject("project_id", project_id).append("stage", Boolean.TRUE))
				.into(new ArrayList<Work>());
		if (workList.size() > 0) {
			List<CBSItem> cbsItemList = new ArrayList<CBSItem>();
			for (Work work : workList) {
				CBSItem cbsItem = CBSItem.getInstance(parentCBSItem);
				cbsItem.setId(work.getCode());
				cbsItem.setName(work.toString());
				cbsItem.setParent_id(_id);
				cbsItemList.add(cbsItem);
			}
			c(CBSItem.class).insertMany(cbsItemList);
		}
		return parentCBSItem;
	}

}
