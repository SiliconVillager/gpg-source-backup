define_animation_set( :aerial_locomotion ) {
	idle from_file "al_idle"
	start_fly from_file "al_start"
	start_fly { is_a_transition! }
}

animset = define_animation_set( :terrestrial_locomotion ){

	idle(from_file("tm_idle"))
	run_forward from_file "tm_run_fwrd"
	walk_forward from_file "tm_wlk_fwrd"
	turn_90degsLeft from_file "tm_trn_90deg"

	jump_forward from_file "tm_jmp_fwrd"
	jump_forward { can_blend_with all_from "terrestrial_locomotion" }
	jump_forward { can_blend_with transitions_from("aerial_locomotion") }
}

