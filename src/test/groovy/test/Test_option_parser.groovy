package test

import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import com.kendallshaw.dtdnormalizer.OptionParser
import com.kendallshaw.dtdnormalizer.OptionParser.OptionParseError

class Test_option_parser extends Specification implements Utils {

  @Shared
  def topic_xml = 'src/test/resources/topic.xml'

  @Shared
  def invalid_sets_cfg = 'src/test/resources/invalid-entity-sets.cfg'

  @Shared
  def entities_op = null

  def setupSpec() {
    entities_op =
      option_parser(['--entities', entity_sets_cfg, topic_xml])
  }

  // Positional arguments tests

  def 'An input file can be found and is not null.'() {
    expect: input_path(topic_xml) != null
  }

  def 'An input file can be found and exists.'() {
    expect: new File(topic_xml).exists()
  }

  def 'An output file can exist.'() {
    def path = output_path topic_xml, 'src/test/resources/out.xml'
    expect: new File(path).parentFile.exists()
  }

  def 'An output file that can not exist is flagged as an error.'() {
    when: silent_output_path topic_xml, 'src/test/reszources/out.xml'
    then: thrown OptionParseError
  }

  // The comments option

  def 'Comments can be requested.'() {
    expect: option_parser(['-c', 'yes', topic_xml]).withComments
  }

  def 'Comments can be requested without yes or no.'() {
    expect: option_parser([topic_xml, '-c']).withComments
  }

  def 'Invalid comments.'() {
    when: silent_option_parser([topic_xml, '-c', 'yup'])
    then: thrown OptionParseError
  }

  // Option specification variants

  def 'Short options can have their argument in the same token.'() {
    expect: option_parser([topic_xml, '-cyes']).withComments
  }

  def 'Long options can have their argument in the same token.'() {
    expect: option_parser(['--comment=yes', topic_xml]).withComments
  }

  def 'Long options can use space as a delimiter.'() {
    expect: option_parser(['--comment', 'yes', topic_xml]).withComments
  }

  def 'Options that look like property specifications are recognized.'() {
    def op = option_parser(['-Ddtd-normalizer.comments=yes',  topic_xml])
    expect: op.withComments
  }

  def 'System property dtd-normalizer.comments is recognized.'() {
    System.setProperty('dtd-normalizer.comments', 'yes')
    def op = option_parser([topic_xml])
    expect: op.withComments
  }

  // Charsets

  def 'A valid charset can be specified for output.'() {
    def op = option_parser([test_xml, '--charset=UTF-16'])
    def cs = op.charset
    expect:
      cs != null
      cs.name() == 'UTF-16'
  }

  def 'An invalid charset is flagged as an error.'() {
    when: silent_option_parser([test_xml, '--charset=boingo'])
    then: thrown OptionParseError
  }

  def 'The charsets option is recognized.'() {
    def lines = option_parser_output(['--charsets'])
    def matched = false
    when:
      lines.eachLine {
        if (it.startsWith('IANA Registered:'))
          matched = true
      }
    then:
      matched
  }

  // Reporting option

  @Unroll
  def 'The report option works.'() {
    expect: specify_report(arg, reporting, catalogs, charsets, entities)
    where:
    arg                                   | reporting | catalogs | charsets | entities
    '--report'                            | true      | true     | true     | true
    '--report=catalogs,all'               | true      | true     | true     | true
    '--report=charsets,all'               | true      | true     | true     | true
    '--report=entities,all'               | true      | true     | true     | true
    '--report=catalogs,charsets,all'      | true      | true     | true     | true
    '--report=catalogs,entities,all'      | true      | true     | true     | true
    '--report=charsets,entities,all'      | true      | true     | true     | true
    '--report=catalogs'                   | true      | true     | false    | false
    '--report=charsets'                   | true      | false    | true     | false
    '--report=entities'                   | true      | false    | false    | true
    '--report=catalogs,charsets'          | true      | true     | true     | false
    '--report=catalogs,entities'          | true      | true     | false    | true
    '--report=charsets,entities'          | true      | false    | true     | true
    '--report=catalogs,charsets,entities' | true      | true     | true     | true
  }

  // Catalog paths

  def 'Specified catalog paths are made absolute.'() {
    //def spec = "${entities_catalog};${entities_dir_catalog}"
    def op = option_parser([test_xml, catalog_list_spec])
    def paths = parsed_catalog_paths(op.catalogList)
    expect:
      paths[0].equals(new File(entities_catalog).canonicalPath)
      paths[1].equals(new File(entities_dir_catalog).canonicalPath)
  }

  // Entity sets

  def 'The entity inclusion config file path is normalized.'() {
    def path = entities_op.entitiesPath
    expect:
      path.equals(new File(entity_sets_cfg).canonicalPath)
  }

  def 'The entity inclusion option works.'() {
    def ids = entities_op.inclusionIds()
    expect:
      'public'.equals(ids['-//KS//ENTITIES Unicode//EN'])
      'system'.equals(ids['urn:ks:entities:other'])
  }

  def 'Specifying entites without an argument includes all.'() {
    expect: option_parser([topic_xml, '--entities']).includingAll
  }

  def 'An invalid entities configuration file causes an error.'() {
    when: silent_option_parser(['--entities',
                                invalid_sets_cfg, topic_xml])
    then: thrown OptionParseError
  }

  // There are no tests below here

  def input_path (path) {
    def args = [path] as String[]
    def op = new OptionParser().parseCommandLineWithoutExit(args)
    return op.inputPath
  }

  def output_path (input, output) {
    def args = [input, output] as String[]
    def op = new OptionParser().parseCommandLineWithoutExit(args)
    return op.outputPath
  }

  def silent_output_path (input, output) {
    def stderr = System.err
    def baos = new ByteArrayOutputStream()
    def ps = new PrintStream(baos)
    System.setErr(ps)
    try {
      def args = [input, output] as String[]
      def op = new OptionParser().parseCommandLineWithoutExit(args)
      return op.outputPath
    } finally {
      System.setErr(stderr)
    }
  }

  def option_parser(args) {
    return new OptionParser().parseCommandLineWithoutExit(args as String[])
  }

  def silent_option_parser (args) {
    def stderr = System.err
    def baos = new ByteArrayOutputStream()
    def ps = new PrintStream(baos)
    System.setErr(ps)
    try {
      return new OptionParser().parseCommandLineWithoutExit(args as String[])
    } finally {
      System.setErr(stderr)
    }
  }

  def option_parser_output (args) {
    def stdout = System.out
    def baos = new ByteArrayOutputStream()
    def ps = new PrintStream(baos)
    System.setOut(ps)
    try {
      new OptionParser().parseCommandLineWithoutExit(args as String[])
    } finally {
      System.setOut(stdout)
    }
    try {
      def bb = ByteBuffer.wrap(baos.toByteArray());
      def cb = Charset.forName("UTF-8").decode(bb);
      return cb.toString()
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  def specify_report(arg, reporting, catalogs, charsets, entities) {
    def op = option_parser([test_xml, arg] as String[])
    return ((op.reporting == reporting)
            && (op.reportingCatalogs == catalogs)
            && (op.reportingEncodings == charsets)
            && (op.reportingEntities == entities))
  }

  def parsed_catalog_paths(catalog_list) {
    def st = new StringTokenizer(catalog_list, ';')
    def paths = []
    while (st.hasMoreTokens()) {
      paths << st.nextToken()
    }
    return paths
  }
}
