# Data management DSL interpreter
#
#  This DSL is just an example, and as such does not handle lot of things
#  It shows how instance_eval, missing_method and instrospection from ruby
#  can help building dsl.
#


# empty object used for introspection
class ReplicationObject
end

# literal extension to handle bytes
class Fixnum
  
  def bytes
    return Byte.new(self)
  end

end

# alignment object, used in combination to instrospection when setting field attributes
class Alignment
  attr_reader :value
  def initialize(value)
    @byte_value = value
    @value = @byte_value.value
  end
end

# used in combination to literal extension
class Byte
  attr_reader :value
  def initialize(value)
    @value = value
  end

  def alignment
    return Alignment.new(self)
  end
end


# simple representation of a layout's field
class Field
  attr_accessor :need_replication
  attr_reader :name, :type, :required, :optional

  def initialize(name, type)
    @name = name
    @type = type
    @need_replication = false
    @required = false
    @optional = false

  end

  def set_required!
    @required = true
  end

  def set_optional!
    @optional = true
  end

  def to_s
    return ":name => #{@name}, :type => #{@type}, :need_replication => #{@need_replication}\n"
  end
end


# model of structure layout
class StructureLayout

  def initialize(name, global_interpreter)
    @name = name
    @fields = Array.new
    @supported_field_types = %w(int32 vector3f quaternion string key float double boolean bytes)

    @global_interpreter = global_interpreter
  end

  def  required(field)
    field.set_required!
  end

  def optional(field)
    field.set_optional!
  end

  def replicate_over_network!()
    return ReplicationObject.new
  end


  def method_missing(user_symbol, *args)
    field = nil
    if user_symbol.to_s == "struct"
      struct_type = args.shift
      field = new_field(struct_type, args)
    else
      raise "unknown field type #{user_symbol}" unless @supported_field_types.include? user_symbol.to_s
      field = new_field(user_symbol, *args)
    end

    @fields << field
    return field
  end

  def new_field(field_type, *args)
    raise "malformed field" if args.length < 1
    field_name = args[0]
    field = Field.new(field_name, field_type)
    field.need_replication = true if (args.select {|obj| obj.instance_of? ReplicationObject}).length > 0
    return field
  end

  def to_s
    str = "#{@name} :\n"
    @fields.each { |field|
      str << "\t" << field.to_s
    }
    return str
  end

end


# global interpreter
class Interpreter
  def self.load_file(filename)
    dsl_object = load_code(IO.read(filename))
    dsl_object
  end

  def self.load_code(stringdata)
    dsl_object = new
    dsl_object.instance_eval(stringdata)
    dsl_object
  end

  def initialize
    @layouts = Hash.new
  end

  def struct(structure_name, &layout_block)

     struct_layout = StructureLayout.new(structure_name, self)
     struct_layout.instance_eval &layout_block

     @layouts[structure_name] = struct_layout
  end

  def to_s
    str = ""
    @layouts.each_pair { |name, layout| 
      str << layout.to_s
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
