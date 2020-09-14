delete
    from discrete_ground_water
    where discrete_ground_water_id in
        (select discrete_ground_water_id
            from field_visit_readings_by_loc
            join discrete_ground_water
                on discrete_ground_water.location_identifier = field_visit_readings_by_loc.location_identifier
            where json_data_id = ? and
            partition_number = ?
        )
