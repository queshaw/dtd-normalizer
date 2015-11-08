package test

import spock.lang.Shared
import spock.lang.Specification

class Test_xml_serialization extends Specification implements Utils {

  @Shared
  def xml = null

  def setupSpec() {
    def text = output_from_command_line([smorga_xml] as String[])
    xml = new XmlSlurper().parseText(text)
  }

  def 'The root element has expected child elements.'() {
    def names = ['xml-declaration',
                 'doctype-declaration',
                 'unparsed-entity-declaration', 
                 'unparsed-entity-declaration', 
                 'external-subset']
    expect: xml.'*'.collect { it.name() } == names
  }

  def 'The doctype system id is not expanded.'() {
    def sys_id = xml.'doctype-declaration'.'system-id'.text()
    def path = 'smorga.dtd'
    expect: sys_id == path
  }

  def 'Parameter entity references nest.'() {
    def children_entref = xml.'external-subset'.'entity'.find {
      it.@name == '%children'
    }
    expect: children_entref.entity.@name == '%attlists'
  }

  // Conditional sections

  def 'Conditional sections are rendered.'() {
    def cs = xml.'**'.findAll { it.name() == 'conditional-section' }*.@condition
    expect: cs == ['INCLUDE', 'IGNORE']
  }

  // Content models

  def 'Empty content model is \'empty\'.'() {
    expect: xml.'*'.'content-model'.find {
      it.@element == 'empty'
    }.empty.size() == 1
  }

  def 'Any content model is \'any\'.'() {
    expect: xml.'*'.'content-model'.find {
      it.@element == 'any'
    }.any.size() == 1
  }

  def 'Text content model is \'pcdata\'.'() {
    expect: xml.'*'.'content-model'.find {
      it.@element == 'text'
    }.group.pcdata.size() == 1
  }

  def 'Mixed content model is mixed.'() {
    def mixed = xml.'*'.'content-model'.find {
      it.@element == 'mixed'
    }.group
    expect:
      (mixed.pcdata
       && mixed.sep.@type == '|'
       && mixed.element.@name == 'p')
  }

  def 'Children sequence content model, sequences.'() {
    def cm = xml.'**'.find {
      it.name() == 'content-model' && it.@element == 'seq'
    }
    expect: cm.group.sep*.@type == [',', ',']
  }

  def 'Children or content model, ors.'() {
    def cm = xml.'**'.find {
      it.name() == 'content-model' && it.@element == 'or'
    }
    expect: cm.group.sep*.@type == ['|', '|']
  }

  def 'Children sequence of ors content model, ors and sequences.'() {
    def cm = xml.'**'.find {
      it.name() == 'content-model' && it.@element == 'seq-or'
    }
    expect:
      (cm.group.sep*.@type == [',']
       && cm.group.group.size() == 2
       && cm.group.group.sep*.@type == ['|', '|']
       && cm.group.group.group.size() == 2
       && cm.group.group.group.sep*.@type == [',', ','])
  }

  // Occurance tokens

  def 'The zero or one token is \'?\'.'() {
    def cm = xml.'**'.find {
      it.name() == 'content-model' && it.@element == 'seq'
    }
    expect:
      cm.group.occur[0].@type == '?'
  }

  def 'The one or more token is \'+\'.'() {
    def cm = xml.'**'.find {
      it.name() == 'content-model' && it.@element == 'seq'
    }
    expect:
      cm.group.occur[1].@type == '+'
  }

  def 'The zero or more token is \'*\'.'() {
    def cm = xml.'**'.find {
      it.name() == 'content-model' && it.@element == 'seq'
    }
    expect:
      cm.group.occur[2].@type == '*'
  }

  // Attributes

  def 'Text attribute values are represented by string.'() {
    expect:
      xml.'**'.find {
        it.name() == 'attribute-declaration' && it.@name == 'text'
      }.string.implied.size() == 1
  }

  def 'Single token types are represented correctly.'() {
    expect: single_token_attributes(element, name, token)
    where:
      element    | name    | token
      'id'       | 'id'    | 'ID'
      'ref'      | 'href'  | 'IDREF'
      'refs'     | 'refs'  | 'IDREFS'
      'att-one'  | 'arf'   | 'ENTITY'
      'att-many' | 'arf'   | 'ENTITIES'
      'one'      | 'type'  | 'NMTOKEN'
      'two'      | 'types' | 'NMTOKENS'
  }

  // Enumerated attribute types

  def 'An enumeration is represented by an enumeration element.'() {
    expect: xml.'**'.find {
      it.name() == 'attribute-declaration' && it.@name == 'implied'
    }.enumeration
  }

  def 'Annotation typed elements are represented as notation elements.'() {
    def entries = xml.'**'.find {
      it.name() == 'attribute-list-declaration' && it.@name == 'boilerplate'
    }.'attribute-declaration'.'*'.entry
    expect: entries*.text() == ['text', 'jpeg']
  }

  // Default values

  def 'A required attribute is required.'() {
    def attd = xml.'**'.find {
      it.name() == 'attribute-list-declaration' && it.@name == 'id'
    }.'attribute-declaration'
    expect:
      (attd.@name == 'id'
       && attd.token.@type == 'ID'
       && attd.token.'*'*.name() == ['required']) 
  }

  def 'CDATA default value is as expected.'() {
    def dv = xml.'**'.find {
      it.name() == 'attribute-declaration' && it.@name == 'cdata-default'
    }.string.'default-value'[0]
    expect:
      dv.text() == 'cdata default value' && dv.attributes().isEmpty() // i.e., not "fixed"
  }

  def 'NMTOKEN default value is as expected.'() {
    def dv = xml.'**'.find {
      it.name() == 'attribute-declaration' && it.@name == 'nmtoken-default'
    }.token
    expect:
      (dv.@type == 'NMTOKEN'
       && dv.'default-value'[0].text() == 'cdata-default-value'
       && dv.'default-value'[0].attributes().isEmpty())
  }

  def 'Enumeration default value is as expected.'() {
    def dv = xml.'**'.find {
      it.name() == 'attribute-list-declaration' && it.@name == 'enumerations'
    }.'*'.find {
      it.name() == 'attribute-declaration' && it.@name == 'defaulted'
    }.enumeration.'default-value'[0]
    expect: 
      dv.text() == 'y' && dv.attributes().isEmpty() // i.e., not "fixed"
  }

  // Fixed default value

  def 'Enumerated notation default value is as expected.'() {
    def dv = xml.'**'.find {
      it.name() == 'attribute-list-declaration' && it.@name == 'pre'
    }.'*'.find {
      it.name() == 'attribute-declaration' && it.@name == 'type'
    }.notation.'default-value'[0]
    expect: 
      dv.text() == 'text' && dv.@fixed == 'true' // i.e., not "fixed"
  }

  // There are no tests below here

  def single_token_attributes(element, name, token) {
    return xml.'**'.find {
      it.name() == 'attribute-list-declaration' && it.@name == element
    }.'*'.find {
      it.name() == 'attribute-declaration' && it.@name == name
    }.token.@type == token
  }
}
