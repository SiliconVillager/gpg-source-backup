struct( :PlayerInfos ){
  required string :name, replicate_over_network!
  required key :race
  required boolean :is_male
  required int32 :level
  required int32 :exp_points
  required vector3f :position, replicate_over_network!, 16.bytes.alignment
  required quaternion :orientation, replicate_over_network!
  optional int32 :money
  optional float :reputation
}


struct( :container_test ) {
	required int32 :size
	required struct :PlayerInfos, :player_infos
}
