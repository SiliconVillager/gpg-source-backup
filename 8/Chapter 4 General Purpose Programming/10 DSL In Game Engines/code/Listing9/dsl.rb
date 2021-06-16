class SWModule
  attr_reader :name

  def initialize(name)
    @name = name
    @dependancies = Array.new
  end

  def depends_on(module_name)
    @dependancies << module_name
    @dependancies.flatten!
  end

  def is_bound_to(thread)
    
  end

  def to_s
    str = "module #{name} "
    if (@dependancies.empty? == false)
      str << "which depends on #{@dependancies.to_s}"
    end
    str << "\n"
    return str
  end
end

class SWThread
  def initialize
  end
end
class HWThread
end

class HWCore
  def have(threads)
  end
end

#literal extensions
class Symbol
  def module
    return SWModule.new(self)
  end
end

class Fixnum
  def software_thread
    return SWThread.new if self == 1
    return self.software_threads
  end
  def software_threads
    threads = Array.new(self, SWThread.new)
    return threads
  end

  def hardware_thread
    return HWThread.new if self == 1
    return self.hardware_threads
  end

  def hardware_threads
    threads = Array.new(self, HWThread.new)
    return threads
  end


  def cores
    c = Array.new(self, HWCore.new)
    return c
  end
end


class SoftwareInterface
  attr_reader :module_list, :thread_list

  def initialize
    @module_list = Hash.new
    @thread_list = Array.new
  end

  def instanciate(*args)
      args.flatten.each() do |element|
        do_instanciate(element)
      end
  end

  def do_instanciate(type)
    if type.is_a?(SWThread)
      @thread_list << type
    elsif type.is_a?(SWModule)
      @module_list[type.name] = type
    end
  end

  def method_missing(module_name)
    get_module(module_name)
  end

  def get_module(module_name)
    mod = @module_list[module_name]
    raise "unknown module \"#{module_name}\"" if mod == nil
    return mod
  end

  def thread(thread_id)
    return @thread_list[thread_id]
  end

end

class HardwareInterface
  attr_reader :cores

  def has(cores)
    @cores = cores
  end
end

class Interpreter
  attr_accessor :hardware, :software

  def self.load_file(filename)
    load_code(IO.read(filename))
  end

  def self.load_code(stringdata)
    dsl = new
    dsl.instance_eval(stringdata)
    dsl
  end

  def initialize()
    @hardware = HardwareInterface.new
    @software = SoftwareInterface.new
  end

  def software(&block)
    @software.instance_eval &block
  end
  def hardware(&block)
    @hardware.instance_eval &block
  end

  def to_s
    str = "dumping domain knowledge :\n"
    if @hardware.cores != nil
    str << "hardware has #{@hardware.cores.length} cores\n"
    end
    if @software.thread_list !=nil
      str << "software has #{@software.thread_list.length} threads\n"
    end
    if @software.module_list != nil
      @software.module_list.each_pair { |name, mod|
        str << "software instanciate #{mod.to_s}"
      }
    end
    return str
  end

end


#main
begin
  # suppress warnings about future version of ruby and parentheses
  $VERBOSE = nil
  # loads domain specific code
  filename = ARGV.shift
  filename = "testdata.dsl" unless filename != nil
  parsedsl = Interpreter.load_file(filename)

  # do whatever transformation with domain knowledge
  puts parsedsl.to_s
end
