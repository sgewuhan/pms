[ {
	"$match" : {
		"summary" : false,
		"distributed" : true,
		"stage" : {
			"$ne" : true
		}
	}
}, {
	"$lookup" : {
		"from" : "workReport",
		"let" : {
			"chargerId" : "$chargerId",
			"assignerId" : "$assignerId",
			"project_id" : "$project_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					$and : [ {
						"$or" : [ {
							"$eq" : [ "$reporter", "$$chargerId" ]
						}, {
							"$eq" : [ "$reporter", "$$assignerId" ]
						} ]
					}, {
						$eq : [ "$project_id", "$$project_id" ]
					} ]
				}
			}
		} ],
		"as" : "workReport"
	}
}, {
	"$unwind" : "$workReport"
}, {
	"$addFields" : {
		"workReport_id" : "$workReport._id",
		"period" : "$workReport.period",
		"finish" : {
			"$eq" : [ {
				"$dateToString" : {
					"format" : "%Y-%m-%d",
					"timezone" : "GMT",
					"date" : "$actualFinish"
				}
			}, {
				"$dateToString" : {
					"format" : "%Y-%m-%d",
					"timezone" : "GMT",
					"date" : "$workReport.period"
				}
			} ]
		}
	}
}, {
	"$match" : {
		"workReport_id" : "<workReport_id>",
		"$or" : [ {
			"actualFinish" : null
		}, {
			"finish" : true
		} ]
	}
}, {
	"$lookup" : {
		"from" : "workInReport",
		"let" : {
			"work_id" : "$_id",
			"workReport_id" : "$workReport_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$work_id", "$$work_id" ]
					}, {
						"$eq" : [ "$workReport_id", "$$workReport_id" ]
					} ]
				}
			}
		} ],
		"as" : "workInReport"
	}
}, {
	"$unwind" : {
		"path" : "$workInReport",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$lookup" : {
		"from" : "user",
		"localField" : "chargerId",
		"foreignField" : "userId",
		"as" : "user1"
	}
}, {
	"$lookup" : {
		"from" : "user",
		"localField" : "assignerId",
		"foreignField" : "userId",
		"as" : "user2"
	}
}, {
	"$unwind" : {
		"path" : "$user1",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$unwind" : {
		"path" : "$user2",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"chargerInfo" : {
			"$concat" : [ "$user1.name", "[", "$user1.userId", "]" ]
		},
		"assignerInfo" : {
			"$concat" : [ "$user2.name", "[", "$user2.userId", "]" ]
		}
	}
}, {
	"$project" : {
		"workReport" : false,
		"user1" : false,
		"user2" : false
	}
} ]