# dtd-normalizer

Creates an XML representation of a DTD using XML catalog resolution.

## Oxygen

The distribution contains a file *dtd-normalizer.scenarios* which can
be used to add transformation scenarios to Oxygen (tested with version
17).

### Integration

Unzip the zip file somewhere. The result should be similar to:

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

java -jar dtd-normalizer-0.5.jar output-file catalog-path source-xml-path

### System properties

| Property                                            | Default   | Description                                              |
|:----------------------------------------------------|:----------|:---------------------------------------------------------|
| `dtd-normalizer.comments` = (`true`&#124;`false`)   | `false`   | Adds entity location comments.                           |
| `dtd-normalizer.serialization` = (`xml`&#124;`dtd`) | `xml`     | Selects XML or DTD text output.                          |
| `dtd-normalizer.entities` = path                    |           | Specifies a file containing a list of public identifiers |
|                                                     |           | of entity sets to be included in the DTD serialization.  |

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
