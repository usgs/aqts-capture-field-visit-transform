delete
from discrete_ground_water
where exists
    (select null
        from field_visit_readings_by_loc
           where json_data_id = ?
           and partition_number = ?
           and discrete_ground_water.location_identifier = field_visit_readings_by_loc.location_identifier
    )
