# dtd-normalizer

Creates an XML representation of a DTD using XML catalog resolution.

## Usage

java -jar dtd-normalizer-0.1.jar output-file catalog-path source-xml-path

### System properties

dtd-normalizer.comments=(true|false) (default: false)   Adds entity location comments.
dtd-normalizer.serialization=(xml|dtd) (default: xml)   Selects XML or DTD text output.


## Building

The **get-dependencies.*** scripts use maven to retreive
dependencies. The result should be that jars are put in a lib
directory. The m2 and target directories that should also be
created may be discarded after the lib directory is populated by
the script.

After supplying the dependencies, you can use Gradle to build a jar file.

