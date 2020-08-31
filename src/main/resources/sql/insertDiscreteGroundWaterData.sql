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
--                                        completed_work,
--                                        last_modified,
                                       parameter,
                                       parm_cd,
                                       monitoring_method,
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
--                                        reading_qualifier,
                                       reading_qualifiers,
--                                        ground_water_measurement,
                                       datum
        )
        select field_visit_readings_by_loc.field_visit_identifier
             , jsonb_extract_path_text(json_data.parameters, 'locationIdentifier') location_identifier -- this needs to come from probably earlier in the etl, maybe we can add it to the json_data table and grab it from there?
--              , start_time -- drop?
--              , end_time -- drop?
--              , party -- not in field_visit_readings_by_loc
--              , remarks -- not in field_visit_readings_by_loc
--              , weather -- not in field_visit_readings_by_loc
--              , field_visit_header_info.is_valid is_valid_header_info -- not in field_visit_readings_by_loc
--              , completed_work -- not in field_visit_readings_by_loc
--              , last_modified -- not in field_visit_readings_by_loc
             , field_visit_readings_by_loc.parameter
             , aq_to_nwis_parm.parm_cd
             , field_visit_readings_by_loc.monitoring_method
             , datum_converted_values.display_value field_visit_value
             , field_visit_readings_by_loc.unit
             , field_visit_readings_by_loc.quantitative_uncertainty uncertainty
             , field_visit_readings_by_loc.reading_type
--              , field_visit_readings.manufacturer -- not in field_visit_readings_by_loc
             , field_visit_readings_by_loc.model
             , field_visit_readings_by_loc.serial_number
             , field_visit_readings_by_loc.field_visit_time
             , field_visit_readings_by_loc.field_visit_comments
             , field_visit_readings_by_loc.publish
--              , field_visit_readings.is_valid    is_valid_readings -- not in field_visit_readings_by_loc
             , field_visit_readings_by_loc.reference_point_unique_id
             , field_visit_readings_by_loc.use_location_datum_as_reference
--              , reading_qualifier -- not in field_visit_readings_by_loc
             , field_visit_readings_by_loc.qualifiers
--              , ground_water_measurement -- not in field_visit_readings_by_loc
             , datum_converted_values.target_datum datum

--         from field_visit_header_info
--                  join field_visit_readings
--                       on field_visit_header_info.json_data_id = field_visit_readings.json_data_id
--                           and field_visit_header_info.field_visit_identifier = field_visit_readings.field_visit_identifier
--                  join field_visit_readings_by_loc
        from field_visit_readings_by_loc
--                       on field_visit_header_info.field_visit_identifier = field_visit_readings_by_loc.field_visit_identifier
--                           and field_visit_readings.field_visit_time = field_visit_readings_by_loc.field_visit_time
                 join datum_converted_values
                      on field_visit_readings_by_loc.json_data_id = datum_converted_values.json_data_id
                          and field_visit_readings_by_loc.field_visit_identifier = datum_converted_values.field_visit_identifier
                 join aq_to_nwis_parm
--                       on field_visit_readings.parameter || '|' || field_visit_readings.unit = aq_to_nwis_parm.parameter
                      on field_visit_readings_by_loc.parameter || '|' || field_visit_readings_by_loc.unit = aq_to_nwis_parm.parameter
                 join data_type_mapping
                      on aq_to_nwis_parm.parm_cd = data_type_mapping.parm_cd
                          and data_type_mapping.data_type = 'discreteGroundWaterTransform'
                 join json_data
                    on field_visit_readings_by_loc.json_data_id = json_data.json_data_id
--         where field_visit_header_info.json_data_id = ?
        where field_visit_readings_by_loc.json_data_id = ?
          and lower(field_visit_readings_by_loc.publish) = 'true'
--           and lower(field_visit_readings.publish) = 'true'
--           and field_visit_header_info.partition_number = ?
--           and field_visit_readings.partition_number = ?
          and field_visit_readings_by_loc.partition_number = ?
          and datum_converted_values.partition_number = ?
        returning location_identifier
), identifiers as (
-- identifiers is here so that we always return the monitoring_location_identifier and location_identifier for downstream
-- orphan handling/delete capabilities.
    select
--         location_identifier,
        jsonb_extract_path_text(json_data.parameters, 'locationIdentifier') location_identifier,
        coalesce(nullif((regexp_match(jsonb_extract_path_text(json_data.parameters, 'locationIdentifier'), '(\d*)-*(.*)'))[2], ''), 'USGS') || '-' || (regexp_match(jsonb_extract_path_text(json_data.parameters, 'locationIdentifier'), '(\d*)-*(.*)'))[1] monitoring_location_identifier
--     from field_visit_header_info
    from json_data
    where json_data_id = ?
      and json_data.partition_number = ?
--         fetch first 1 rows only
) select
        (select location_identifier from identifiers),
        (select monitoring_location_identifier from identifiers),
         count(*) records_inserted
from upd;
