#
#
#

class Animation
  attr_accessor :name
  attr_reader :filename, :transition, :blend_list


  def initialize(from_filename )
    @filename = from_filename
    @transition = false
    @blend_list = Array.new
  end

  def can_blend_with(list)
    @blend_list << list
    @blend_list.flatten!
  end

  def is_a_transition?
    return @transition
  end
  def is_a_transition!
     @transition = true
  end

  def all_from(set_name) 
    # should resolve when dsl code is completely parsed 
    # (so that all sets and animations are defined
    # $global_interpreter.animation_sets[set_name.to_s].animations
    return []
  end

  def transitions_from(set_name)
    # should resolve when dsl code is completely parsed 
    # (so that all sets and animations are defined
    # transitions = $global_interpreter.animation_sets[set_name].animations.select { |anim| anim.is_a_transition? }
    return [] 
  end
end

class AnimationSet
  attr_reader :name, :animations

  def initialize(name)
    @name = name
    @animations = Hash.new
  end

  def method_missing(user_symbol, *args, &block)
    if block != nil
      define_animation_properties(user_symbol, &block)
    else
      raise "malformed expression" if args == nil or args.length < 1
      @animations[user_symbol] = args[0] unless args[0].instance_of?(Animation) == false
    end
  end

  def from_file(filename)
    return Animation.new(filename)
  end

  def define_animation_properties(animation_name, &property_block)
    anim = @animations[animation_name]
    raise "unknown animation #{animation_name}" if anim == nil
    anim.instance_eval &property_block
  end

  def to_s
    str = "#{@name} :\n"
    @animations.each_pair{ |name, anim|
      str << "\t#{name} : #{anim.to_s}\n"
    }
    return str
  end
end

# global interpreter
class Interpreter
  attr_reader :animation_sets

  def self.load_file(filename)
    dsl_object = load_code(IO.read(filename))
    dsl_object
  end

  def self.load_code(stringdata)
    dsl_object = new
    $global_interpreter = dsl_object
    dsl_object.instance_eval(stringdata)
    dsl_object
  end

  def initialize
    @animation_sets = Hash.new
  end

  def define_animation_set(set_name, &animation_knowledge)
    set = AnimationSet.new(set_name)
    set.instance_eval &animation_knowledge
    @animation_sets[set_name.to_s] = set
    return set
  end

  def to_s
    str = ""
    @animation_sets.each { |animation_set| 
      str << animation_set.to_s
    }
    return str
  end

end


# main
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
