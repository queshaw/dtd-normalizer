package test

import spock.lang.Specification

class Test_entity_sets extends Specification implements Utils {

  def 'Entities are included according to specifed entity sets.'() {
    def arghs = [catalog_list_spec,
                '--entities', entity_sets_cfg,
                '--serialization', 'dtd',
                entities_test_xml] as String[]
    def output = output_from_command_line(arghs)
    def decls = ['<!ENTITY snook "SNOOK">',
                 '<!ENTITY afr "&#x1D51E;">',
                 '<!ENTITY Afr "&#x1D504;">',
                 '<!ENTITY fraktur-z "'
                 + new String(Character.toChars(0x1d59f))
                 + '">']
    output.eachLine {
      decls.remove(it)
    }
    decls.each { println it }
    expect:
      decls.size() == 0
  }

  def 'Nested entities are included according to specifed entity sets.'() {
    def arghs = [catalog_list_spec,
                 '--entities', entity_sets_cfg,
                 '--serialization', 'dtd',
                 entities_nested_xml] as String[]
    def output = output_from_command_line(arghs)
    def decls = ['<!ENTITY before "BEFORE">',
                 '<!ENTITY between "BETWEEN">',
                 '<!ENTITY after "AFTER">',
                 '<!ENTITY a "a">',
                 '<!ENTITY b "b">',
                 '<!ENTITY c "c">',
                 '<!ENTITY one "1">',
                 '<!ENTITY two "2">',
                 '<!ENTITY hundred "100">',
                 '<!ENTITY roman-one "i">',
                 '<!ENTITY roman-two "ii">',
                 '<!ENTITY roman-three "iii">']
    output.eachLine {
      decls.remove(it)
    }
    expect:
      decls.size() == 0
  }

  def 'Specifying the doctype entity includes all entity decls.'() {
    def arghs = [catalog_list_spec,
                 '--entities', 'src/test/resources/doctype-entity.cfg',
                 '--serialization', 'dtd',
                entities_test_xml] as String[]
    def output = output_from_command_line(arghs)
    def decls = ['<!ENTITY snook "SNOOK">',
                 '<!ENTITY bork "bork">',
                 '<!ENTITY asdf "\'">',
                 '<!ENTITY other "a&asdf;">',
                 '<!ENTITY double-quote "&#x22;">',
                 '<!ENTITY text "Abc &amp; (&#x1D35;) 123 &bork; xyz'
                 + ' &#x1D35;">',
                 '<!ENTITY afr "&#x1D51E;">',
                 '<!ENTITY Afr "&#x1D504;">',
                 '<!ENTITY fraktur-z "'
                 + new String(Character.toChars(0x1d59f))
                 + '">',
                 '<!ENTITY gcirc "&#x0011D;">']
    output.eachLine {
      decls.remove(it)
    }
    decls.each { println it }
    expect:
      decls.size() == 0
  }

  def 'Specifying the doctype entity includes all entity decls.'() {
    def arghs = [catalog_list_spec,
                 '--entities', 'src/test/resources/doctype-entity.cfg',
                 '--serialization', 'dtd',
                entities_test_xml] as String[]
    def output = output_from_command_line(arghs)
    def decls = ['<!ENTITY snook "SNOOK">',
                 '<!ENTITY bork "bork">',
                 '<!ENTITY asdf "\'">',
                 '<!ENTITY other "a&asdf;">',
                 '<!ENTITY double-quote "&#x22;">',
                 '<!ENTITY text "Abc &amp; (&#x1D35;) 123 &bork; xyz'
                 + ' &#x1D35;">',
                 '<!ENTITY afr "&#x1D51E;">',
                 '<!ENTITY Afr "&#x1D504;">',
                 '<!ENTITY fraktur-z "'
                 + new String(Character.toChars(0x1d59f))
                 + '">',
                 '<!ENTITY gcirc "&#x0011D;">']
    output.eachLine {
      decls.remove(it)
    }
    decls.each { println it }
    expect:
      decls.size() == 0
  }

  def 'Unparsed entities are serialized correctly in DTD syntax.'() {
    def arghs = [catalog_list_spec,
                 '-sdtd', 
                 'src/test/resources/notation-subset.xml'] as String[]
    def output = output_from_command_line(arghs)
    def decls = ['<!ENTITY a.jpg SYSTEM "a.jpg" NDATA jpeg>',
                 '<!ENTITY b.jpg SYSTEM "b.jpg" NDATA jpeg>',
                 '<!ENTITY a.png SYSTEM "a.png" NDATA png>',
                 '<!NOTATION jpeg PUBLIC "-//MEDIA JPEG//EN">',
                 '<!NOTATION png PUBLIC "-//MEDIA PNG//EN">',
                 '          data NOTATION (jpeg) #REQUIRED>',
                 '          data NOTATION (jpeg | png) #REQUIRED>']
    output.eachLine {
      decls.remove(it)
    }
    decls.each { println it }
    expect:
      decls.size() == 0
  }

  def 'UTF-16BE with a BOM in DTD syntax, works.'() {
    def arghs = [catalog_list_spec,
                 '-sdtd', 
                 '--entities', '--',
                 'src/test/resources/characters/characters-16.xml'] as String[]
    def decls = ['<!ENTITY right "'
                 + new String(Character.toChars(0xFFEB))
                 + '">',
                 '<!ENTITY right-hex "&#xFFEB;">',
                 '<!ENTITY kana "'
                 + new String(Character.toChars(0x1B000))
                 + '">',
                 '<!ENTITY kana-hex "&#x1B000;">',
                 '<!ENTITY a "'
                 + new String(Character.toChars(0x10300))
                 + '">',
                 '<!ENTITY a-hex "&#x10300;">',
                 '<!ENTITY bar "'
                 + new String(Character.toChars(0x1D100))
                 + '">',
                 '<!ENTITY bar-hex "&#x1D100;">',
                 '<!ENTITY coda "'
                 + new String(Character.toChars(0x1D10C))
                 + '">',
                 '<!ENTITY coda-hex "&#x1D10C;">',
                 '<!ENTITY some "right &right; hex &right-hex;'
                 + ' and &a; and &a-hex; and code &coda; hex'
                 + ' &coda-hex;">']
    def output = output_from_command_line(arghs)
    output.eachLine {
        decls.remove(it)
    }
    decls.each { println it }
    expect:
      decls.size() == 0
  }
}