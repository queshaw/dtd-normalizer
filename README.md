# dtd-normalizer

Serializes a DTD into a single XML or DTD document.

The release is packaged as an executable jar file and includes
packaging to integrate the release into Oxygen XML editor as a
transformation scenario.

## Entity resolution

The DTD to parse is read from a DOCTYPE statement on the input
document. The input document must be DTD valid.

Only DOCTYPE statements with an external subset are supported. The
internal subset is also parsed.

By default system identifiers are resolved literally, i.e. relative
URIs are resolved relative to the document that they are referenced
from, as per the XML standard. Absolute URIs are resolved by the JVM
URL stream handler factory.

If OASIS XML catalogs are found, XML commons resolver is used to
resolve public and system identifies mapped in the catalogs.

### XML catalogs

In order of preference, XML catalogs to use can be specified as a
semi-colon separated list, on the command line using the `--catalogs`
option, by setting the `xml.catalog.files` system property or by
setting the `catalogs` property in a `CatalogManager.properties` file
that is found on the classpath. Other settings from the properties
file are merged if the list of catalog files is specified separately.

Some settings are always overridden. The result is as if
`CatalogManager.properties` were found and it had these property
specifications:

```
static-catalog=no
relative-catalogs=no
```

the later means that if relative catalog paths are found in a
`CatalogManager.properties` file, they are resolved relative to the
location of the properties file (as opposed to being relative to the
current directory).

If the system property `xml.catalog.ignoreMissing` is not set, it is
treated as if `xml.catalog.ignoreMissing=yes` were specified, meaning
that no warning is printed if `CatalogManager.properties` is not
found.

## Character encoding

The character encoding of each entity is detected using the unicode
library ICU4J, it is unusual but possible for the encoding to have
been detected incorrectly.

The option `--report=charsets` can be used to see what encodings were
detected.

The charset used to encode the serialization can be specified using
the `--charset` option.

The available charsets can be listed using the `--charsets` option.

## XML serialization

The output document represents the entity and element declarations of
the DTD along with the location where they were declared within their
entities.

## DTD serialization

The DTD syntax consists of the parsed element and attribute list
declarations.

Separate element type attribute list declarations are represented
separately as they were in their entities.

### Entity declarations

Unparsed entity declarations are included in the serialization, as are
declarations of internal general entities which have been referenced
in an attribute default value, or in another internal entity
declaration that is included in the serialization.

### Entity sets

Entity declarations are omitted, except for those mentioned above,
those in the internal DTD subset (within the DOCTYPE statement), or if
their inclusion in the serialization is indicated.

A typical reason to include entity declarations would be to make 
character entities available which are declared in entity sets.

Inclusion is indicated when the entity is declared directly or
declared within an entity reference, and the declaration occurs within
a specified entity set, or within external entities referenced from
that entity set and within any external entites then referenced,
recursively, until the end of the specified entity set reference.

Among the candidate entity declarations, those that are redefinitions
are omitted.

A list of entity sets can be specified in a file referenced by the
`--entities` option. If the option has no argument, inclusion of all
candidate general entity declarations is indicated.

If the external DTD subset entity is one of the specified entity sets,
all candidiate general entity declarations in the DTD are included.

The list file format accepts lines specifying a public id, a system
id, a comment or an empty line. For example:

```
\# This is a comment

public: -//FPI//DTD Extra Fancy//EN
public: -//FPI//ENTITIES Math symbols//EN
public:-//FPI//ENTITIES Programming symbols//EN

system:isoamsa.ent
system: urn:example:characters:apl
```

Blank lines and comment lines are ignored. Whitespace is ignored when
it occurs between the `public:` or `system:` prefix and the following
identifier.

### Entity declaration location information

If the `--comment` option is specified, or the
`dtd-normalizer.comments` system property is set, and the value is
`yes` or `true`, comments are included in the output DTD showing the
entity path and line number where declarations occured as declarations
or redefinitions, as well as the locations before and after 
where external parameter entities were referenced.

## Oxygen

The distribution contains a file *dtd-normalizer.scenarios* which can
be used to add transformation scenarios to Oxygen (tested with version
17).

### Integration

Unzip the the distribution archive somewhere. The result should be
similar to:

Windows:

```
c:\somewhere\dtd-normalizer-0.5\dtd-normalizer\build.xml
c:\somewhere\dtd-normalizer-0.5\dtd-normalizer\dtd-normalizer-0.5.jar
c:\somewhere\dtd-normalizer-0.5\dtd-normalizer.scenarios
```

Find the oxygen framework's directory copy the dtd-normalizer
directory there. The result should be similar to:

```
c:\Program Files\Oxygen 17.0\frameworks\dtd-normalizer\build.xml
c:\Program Files\Oxygen 17.0\frameworks\dtd-normalizer\dtd-normalizer-0.5.jar
```

In Oxygen select *Options->Import* transformation scenarios, select
the *dtd-normalizer.scenarios* file that was extracted from the zip
file and click okay.

After opening the secenarios file, Oxygen will allow you to choose
which of 3 transformation scenarios to import (all, by default). Near
the bottom where it says "Storage", select "Global Options", unless
you want the transformation scenarios to only be available in a
specific project.

### Oxygen usage

Note: By default, the transformation scenario uses the DITA
framework's catalog. This can be changed by changing the catalog.path
parameter in the transformation scenario.)

* Open an XML file that contains a doctype statement.
* Click the button that looks like a wrench (Configure transformation scenarios).
* In the Global section, select the transformation scenario that you want to use.
* Click "Apply associated".

## Command-line usage

```
Usage: java com.kendallshaw.dtdnormalizer.CommandLine
       [options] input-file [output-file]
Creates a simplified representation of the input document's DTD.

Options:

 -D <property=value>             System properties.
 -C,--catalog <path[...;path]>   (-Dxml.catalog.files) Specifies a list of
                                 semi-colon separated OASIS XML catalog files
                                 which can be used in resolving entity
                                 resources.
 -c,--comments <boolean>         (-Ddtd-normalizer.comments) true/yes/false/no
                                 default: false. Specifies that parameter entity
                                 locations are added as comments to the DTD
                                 serialization.
 -charset,--charset <charset>    (-Ddtd-normalizer.charset) default: UTF-8.
                                 Specifies the charset for reading and writing.
 -charsets,--charsets            Lists the charsets supported for encoding and
                                 decoding prior to XML parsing and
                                 serialization.
 -decode,--decode <charset>      (-Ddtd-normalizer.charset.input) default:
                                 UTF-8. Specifies the charset to decode for
                                 input.
 -e,--entities <path>            (-Ddtd-normalizer.entities) Specifies a file
                                 containing a list of public identifiers of
                                 entity sets to be included in the DTD
                                 serialization.
 -encode,--encode <charset>      (-Ddtd-normalizer.charset.input) default:
                                 UTF-8. Specifies the charset for encoding
                                 output.
 -s,--serialization <format>     (-Ddtd-normalizer.serialization) xml/dtd.
                                 default: xml. Specifies XML or DTD syntax
                                 output.
 -stacktrace,--stacktrace        Includes a stacktrace, if possible, after an
                                 error occurs.
 -h,--help                       Writes this message to standard out.

Command line arguments may be specified before or after options. Options take
precendence over system properties. If no output file is specified, the output
is written to standard out.
```

## Building

To build from source, you need java, then:

Unix:

./gradlew

Windows:

gradlew

If you already have gradle you might want to first try using gradle
instead of gradlew. In the case where the server that the URL that the
gradle wrapper uses doesn't exist for all of eternity, and you don't
already have gradle, you might have to download and install gradle.

The **dist** target can be used to attempt to retrieve newer versions
of the dependencies, than those in the lib directory. It is not run
automatically.
