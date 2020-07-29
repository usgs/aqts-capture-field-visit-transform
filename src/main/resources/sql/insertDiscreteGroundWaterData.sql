with upd as (
	insert into discrete_ground_water (
									   field_visit_identifier, location_identifier, start_time, end_time, party,
									   remarks, weather, is_valid_header_info, completed_work, last_modified, parameter,
									   parm_cd, monitoring_method, field_visit_value, unit, uncertainty, reading_type,
									   manufacturer, model, serial_number, field_visit_time, field_visit_comments,
									   publish, is_valid_readings, reference_point_unique_id,
									   use_location_datum_as_reference, reading_qualifier, reading_qualifiers,
									   ground_water_measurement
		)
		select field_visit_header_info.field_visit_identifier
			 , location_identifier
			 , start_time
			 , end_time
			 , party
			 , remarks
			 , weather
			 , field_visit_header_info.is_valid is_valid_header_info
			 , completed_work
			 , last_modified
			 , field_visit_readings.parameter
			 , aq_to_nwis_parm.parm_cd
			 , monitoring_method
			 , field_visit_value
			 , unit
			 , uncertainty
			 , reading_type
			 , manufacturer
			 , model
			 , serial_number
			 , field_visit_time
			 , field_visit_comments
			 , publish
			 , field_visit_readings.is_valid    is_valid_readings
			 , reference_point_unique_id
			 , use_location_datum_as_reference
			 , reading_qualifier
			 , reading_qualifiers
			 , ground_water_measurement

		from field_visit_header_info
				 join field_visit_readings
					  on field_visit_header_info.json_data_id = field_visit_readings.json_data_id
						  and
						 field_visit_header_info.field_visit_identifier = field_visit_readings.field_visit_identifier
				 join aq_to_nwis_parm
					  on field_visit_readings.parameter || '|' || field_visit_readings.unit = aq_to_nwis_parm.parameter
				 join data_type_mapping
					  on aq_to_nwis_parm.parm_cd = data_type_mapping.parm_cd
						  and data_type_mapping.data_type = 'discreteGroundWaterTransform'
		where field_visit_header_info.json_data_id = ?
		  and lower(publish) = 'true'
		  and field_visit_header_info.partition_number = ?
		returning location_identifier
) select
         location_identifier,
         count(*) records_inserted
from upd
group by location_identifier
fetch first 1 rows only;
