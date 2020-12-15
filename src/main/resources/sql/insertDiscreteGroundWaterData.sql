with upd as (
    insert into discrete_ground_water (
                                       field_visit_identifier,
                                       location_identifier,
--                                        start_time,
--                                        end_time,
--                                        party,
--                                        remarks,
--                                        weather,
--                                        is_valid_header_info,
                                       completed_work,
--                                        last_modified,
                                       parameter,
                                       parm_cd,
                                       monitoring_method,
                                       nwis_method_code,
                                       field_visit_value,
                                       unit,
                                       uncertainty,
                                       reading_type,
--                                        manufacturer,
                                       model,
                                       serial_number,
                                       field_visit_time,
                                       field_visit_comments,
                                       publish,
--                                        is_valid_readings,
                                       reference_point_unique_id,
                                       use_location_datum_as_reference,
                                       reading_qualifiers,
                                       approval_level,
                                       approval_level_description,
--                                        ground_water_measurement,
                                       datum
        )
        /*
            This query grabs the datum converted values and maps them to an NWIS parameter code using the datum and the
            unit.
            For now, parameter code 62610, 62600, and 62601 have duplicate datum/unit values, and cannot be uniquely
            mapped to an NWIS parameter code.  We have been told to use 62610 for now, so the other two are excluded
            in the where clause.
        */
        select field_visit_readings_by_loc.field_visit_identifier
             , field_visit_readings_by_loc.location_identifier
             , field_visit_header_info.completed_work
             , aq_to_nwis_parm.parameter
             , aq_to_nwis_parm.parm_cd
             , field_visit_readings_by_loc.monitoring_method
             , aq_to_nwis_method_code.nwis_method_code
             , datum_converted_values.display_value field_visit_value
             , field_visit_readings_by_loc.unit
             , field_visit_readings_by_loc.quantitative_uncertainty uncertainty
             , field_visit_readings_by_loc.reading_type
             , field_visit_readings_by_loc.model
             , field_visit_readings_by_loc.serial_number
             , field_visit_readings_by_loc.field_visit_time
             , field_visit_readings_by_loc.field_visit_comments
             , field_visit_readings_by_loc.publish
             , field_visit_readings_by_loc.reference_point_unique_id
             , field_visit_readings_by_loc.use_location_datum_as_reference
             , field_visit_readings_by_loc.qualifiers
             , field_visit_readings_by_loc.approval_level
             , field_visit_readings_by_loc.approval_level_description
             , aq_to_nwis_parm.datum

        from field_visit_readings_by_loc
                 join datum_converted_values
                      on field_visit_readings_by_loc.json_data_id = datum_converted_values.json_data_id
                          and field_visit_readings_by_loc.field_visit_identifier = datum_converted_values.field_visit_identifier
                          and field_visit_readings_by_loc.field_visit_time = datum_converted_values.field_visit_time
                 join field_visit_header_info
                      on field_visit_header_info.json_data_id = datum_converted_values.json_data_id
                          and field_visit_header_info.field_visit_identifier = datum_converted_values.field_visit_identifier
                 join aq_to_nwis_parm
                      on datum_converted_values.target_datum = aq_to_nwis_parm.datum
                          and datum_converted_values.unit = aq_to_nwis_parm.unit
                 join data_type_mapping
                      on aq_to_nwis_parm.parm_cd = data_type_mapping.parm_cd
                          and data_type_mapping.data_type = 'discreteGroundWaterTransform'
                 join aq_to_nwis_method_code
                      on field_visit_readings_by_loc.monitoring_method = aq_to_nwis_method_code.aqts_monitoring_method
        where field_visit_readings_by_loc.json_data_id = ?
          and field_visit_readings_by_loc.partition_number = ?
          and datum_converted_values.partition_number = ?
          and field_visit_header_info.json_data_id = ?
          and field_visit_header_info.partition_number = ?
          and lower(field_visit_readings_by_loc.publish) = 'true'
          and aq_to_nwis_parm.parm_cd not in ('62600', '62601')
          and lower(field_visit_readings_by_loc.reading_type) = 'referenceprimary'

        /*
            Union is grabbing the non-duplicate values for these two unique datasets so that we can have both the
            original discrete groundwater level value from the GetFieldVisitReadingsByLocation call, and the the datum
            converted values while mapping both to a corresponding NWIS parameter code - which have different mapping
            conditions.
        */
        union

        /*
            This query grabs the original discrete groundwater level value and maps it to an NWIS parameter code using
            the parameter long name concatenated with the unit.
        */
        select field_visit_readings_by_loc.field_visit_identifier
             , field_visit_readings_by_loc.location_identifier
             , field_visit_header_info.completed_work
             , aq_to_nwis_parm.parameter
             , aq_to_nwis_parm.parm_cd
             , field_visit_readings_by_loc.monitoring_method
             , aq_to_nwis_method_code.nwis_method_code
             , field_visit_readings_by_loc.display_value field_visit_value
             , field_visit_readings_by_loc.unit
             , field_visit_readings_by_loc.quantitative_uncertainty uncertainty
             , field_visit_readings_by_loc.reading_type
             , field_visit_readings_by_loc.model
             , field_visit_readings_by_loc.serial_number
             , field_visit_readings_by_loc.field_visit_time
             , field_visit_readings_by_loc.field_visit_comments
             , field_visit_readings_by_loc.publish
             , field_visit_readings_by_loc.reference_point_unique_id
             , field_visit_readings_by_loc.use_location_datum_as_reference
             , field_visit_readings_by_loc.qualifiers
             , field_visit_readings_by_loc.approval_level
             , field_visit_readings_by_loc.approval_level_description
             , aq_to_nwis_parm.datum

        from field_visit_readings_by_loc
                 join field_visit_header_info
                      on field_visit_header_info.json_data_id = field_visit_readings_by_loc.json_data_id
                          and field_visit_header_info.field_visit_identifier = field_visit_readings_by_loc.field_visit_identifier
                 join aq_to_nwis_parm
                      on field_visit_readings_by_loc.parameter || '|' || field_visit_readings_by_loc.unit = aq_to_nwis_parm.parameter
                 join data_type_mapping
                      on aq_to_nwis_parm.parm_cd = data_type_mapping.parm_cd
                          and data_type_mapping.data_type = 'discreteGroundWaterTransform'
                 join aq_to_nwis_method_code
                      on field_visit_readings_by_loc.monitoring_method = aq_to_nwis_method_code.aqts_monitoring_method
        where field_visit_readings_by_loc.json_data_id = ?
          and field_visit_readings_by_loc.partition_number = ?
          and field_visit_header_info.json_data_id = ?
          and field_visit_header_info.partition_number = ?
          and lower(field_visit_readings_by_loc.publish) = 'true'
          and lower(field_visit_readings_by_loc.reading_type) = 'referenceprimary'

        returning location_identifier
), identifiers as (
    /*
        identifiers is here so that we always return the monitoring_location_identifier and location_identifier for
        downstream orphan handling/delete capabilities.

        Pulling the location identifier from the json_data table's parameters column guarantees that even when an empty
        dataset from AQTS comes through the etl, we can still pass the location identifier and monitoring location
        identifier to the aqts-capture-discrete-loader lambda, which handles empty/orphan datasets in the observations
        database.
    */
    select
        jsonb_extract_path_text(parameters, 'locationIdentifier') location_identifier,
        coalesce(nullif((regexp_match(jsonb_extract_path_text(parameters, 'locationIdentifier'), '(\d*)-*(.*)'))[2], ''), 'USGS') || '-' || (regexp_match(jsonb_extract_path_text(parameters, 'locationIdentifier'), '(\d*)-*(.*)'))[1] monitoring_location_identifier
    from json_data
    where json_data.json_data_id = ?
      and json_data.partition_number = ?
) select
        (select location_identifier from identifiers),
        (select monitoring_location_identifier from identifiers),
         count(*) records_inserted
from upd;
