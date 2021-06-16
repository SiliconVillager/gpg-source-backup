hardware {
	has 3.cores.each { |core| core.have 2.hardware_threads }
}

software do
	instanciate 6.software_threads
	instanciate :camera.module
	instanciate :player.module, :bots.module, :sound.module 
	instanciate :physics.module, :graphics.module

	camera.depends_on(:player)
	bots.depends_on(:player)
	graphics.is_bound_to(thread(0))	
end
