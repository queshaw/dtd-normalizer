package test

import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import spock.lang.Shared

import com.kendallshaw.dtdnormalizer.CommandLine

trait Utils {

  @Shared
  def test_xml = 'src/test/resources/test.xml'

  @Shared
  def smorga_xml = 'src/test/resources/smorgasbord/smorga.xml'

  @Shared
  def entities_nested_xml = 'src/test/resources/entities/nested.xml'

  @Shared
  def entities_test_xml = 'src/test/resources/entities/test.xml'

  @Shared
  def entities_catalog = 'src/test/resources/entities-catalog.xml'

  @Shared
  def entities_dir_catalog = 'src/test/resources/entities/catalog.xml'

  @Shared
  def catalog_list_spec =
    "--catalogs=${entities_catalog};${entities_dir_catalog}"

  @Shared
  def entity_sets_cfg = 'src/test/resources/entity-sets.cfg'

  def output_from_command_line(args) {
    def stdout = System.out
    def baos = new ByteArrayOutputStream()
    def ps = new PrintStream(baos)
    System.setOut(ps)
    try {
      def cl = new CommandLine()
      cl.execute(args)
      def bb = ByteBuffer.wrap(baos.toByteArray());
      def cb = Charset.forName("UTF-8").decode(bb);
      return cb.toString()
    } finally {
      System.setOut(stdout)
    }
  }
}