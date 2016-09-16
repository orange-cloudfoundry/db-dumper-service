UPDATE database_dump_file ddf
SET service_instance_id = (
  SELECT MIN(ddsi.service_instance_id)
  FROM db_dumper_service_instance ddsi
  WHERE ddsi.database_ref_id = ddf.database_ref_id
);