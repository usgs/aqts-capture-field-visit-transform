insert into discrete_ground_water_aqts (
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
