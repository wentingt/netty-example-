# Netty Discard Server Example

A minimal maven project using [Netty][1].
This [sample code][2] is from taken from the netty distribution.

[1]: http://www.jboss.org/netty
[2]: http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/discard/package-summary.html

## Archetypes

To create an archetype from this project you can use:

    mvn archetype:create-from-project

To install the archetype in your local maven repo use:

    cd target/generated-sources/archetype/
    mvn install

To create a project from your archetype use:

    mvn archetype:generate -DarchetypeCatalog=local

and select yours.


#wentingt

SSL does not work. No need to consider.

Test with single client: good.

Test with single client: multiple files. 
	change chunkhandler. no need. bottleneck 1G / 8

Test with multi-client: good. different channel.

read Hadoop: cache pool? Does not matter.

To do: Hadoop test.
