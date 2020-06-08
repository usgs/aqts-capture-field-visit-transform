delete
from discrete_ground_water
where exists
		  (select null
		   from field_visit_header_info
		   where json_data_id = ? and
				   discrete_ground_water.field_visit_identifier = field_visit_header_info.field_visit_identifier
		  )
