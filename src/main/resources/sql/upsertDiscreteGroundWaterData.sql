insert into discrete_ground_water (
	field_visit_identifier
	,location_identifier
	,start_time
	,end_time
	,party
	,remarks
	,weather
	,is_valid_header_info
	,completed_work
	,last_modified
	,parameter
	,monitoring_method
	,field_visit_value
	,unit
	,uncertainty
	,reading_type
	,manufacturer
	,model
	,serial_number
	,field_visit_time
	,field_visit_comments
	,publish
	,is_valid_readings
	,reference_point_unique_id
	,use_location_datum_as_reference
	,reading_qualifier
	,reading_qualifiers
	,ground_water_measurement
)
select
	field_visit_identifier
	,location_identifier
	,start_time
	,end_time
	,party
	,remarks
	,weather
	,field_visit_header_info.is_valid is_valid_header_info
	,completed_work
	,last_modified
	,parameter
	,monitoring_method
	,field_visit_value
	,unit
	,uncertainty
	,reading_type
	,manufacturer
	,model
	,serial_number
	,field_visit_time
	,field_visit_comments
	,publish
	,field_visit_readings.is_valid is_valid_readings
	,reference_point_unique_id
	,use_location_datum_as_reference
	,reading_qualifier
	,reading_qualifiers
	,ground_water_measurement

from field_visit_header_info
		 join field_visit_readings
			  on field_visit_header_info.json_data_id = field_visit_readings.json_data_id
where field_visit_header_info.json_data_id = ?
on conflict on constraint discrete_ground_water_ak do update
	set
	  field_visit_identifier = excluded.field_visit_identifier
	  ,location_identifier = excluded.location_identifier
	  ,start_time = excluded.start_time
	  ,end_time = excluded.end_time
	  ,party = excluded.party
	  ,remarks = excluded.remarks
	  ,weather = excluded.weather
	  ,is_valid_header_info = excluded.is_valid_header_info
	  ,completed_work = excluded.completed_work
	  ,last_modified = excluded.last_modified
	  ,parameter = excluded.parameter
	  ,monitoring_method = excluded.monitoring_method
	  ,field_visit_value = excluded.field_visit_value
	  ,unit = excluded.unit
	  ,uncertainty = excluded.uncertainty
	  ,reading_type = excluded.reading_type
	  ,manufacturer = excluded.manufacturer
	  ,model = excluded.model
	  ,serial_number = excluded.serial_number
	  ,field_visit_time = excluded.field_visit_time
	  ,field_visit_comments = excluded.field_visit_comments
	  ,publish = excluded.publish
	  ,is_valid_readings = excluded.is_valid_readings
	  ,reference_point_unique_id = excluded.reference_point_unique_id
	  ,use_location_datum_as_reference = excluded.use_location_datum_as_reference
	  ,reading_qualifier = excluded.reading_qualifier
	  ,reading_qualifiers = excluded.reading_qualifiers
	  ,ground_water_measurement = excluded.ground_water_measurement
where discrete_ground_water.last_modified < excluded.last_modified
;
