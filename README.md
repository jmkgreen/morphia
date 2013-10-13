### This Project Has Moved

Please be aware that this fork has been officially superceded by one driven by MongoDB, Inc.

As a consequence this fork is really not needed. You are strongly encouraged to adopt https://github.com/mongodb/morphia instead.

Bugs and improvements should be submitted to that project.

This project will remain here but not actively maintained.

Once again, please visit https://github.com/mongodb/morphia from now on.

### Basic Introduction

Morphia provides an annotation-driven approach to mapping POJO based entities into and out of MongoDB. As such, Morphia is an ODM (or Object Document Mapper).

To use, add Morphia to your Java SE or EE project alongside the mongo-java-driver (which Morphia depends on). Then, create POJOs representing your entities just like you would with JPA entities. Finally, either use Morphia's Datastore interface to store and retrieve entities, or hand that responsibility to a type-safe DAO that can extend from the shipped BasicDAO.

### Downloads

*Jar files* are downloadable from [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.jmkgreen.morphia%22).

*Maven* users: Releases are available in Central. Snapshots are available via [OSS Sonatype](https://oss.sonatype.org).

<table>
	<tr>
		<th colspan="2">Maven Dependency</td>
	</tr>
	<tr>
		<td>Group ID</td><td>com.github.jmkgreen.morphia</td>

	</tr>
		<td>Artifact ID</td><td>morphia</td>
	</tr>
	<tr>
		<td>Version</td><td>1.2.3</td>
	</tr>
</table>

You can paste the following into your pom.xml:
```
<dependency>
    <groupId>com.github.jmkgreen.morphia</groupId>
    <artifactId>morphia</artifactId>
    <version>1.2.3</version>
</dependency>
```

### Documentation

The [wiki](https://github.com/jmkgreen/morphia/wiki) has quite a lot of good example code.
It was copied over from the original GoogleCode site.

Maven Site docs are [here](http://jmkgreen.github.com/morphia).

JavaDoc packages are available for more detailed IDE-based help.

GitHub gists are welcomed from contributors.

### Testing

jUnit tests form part of this project. If these are green, it's shippable!

### Bugs / Support

The original [Google Group](http://groups.google.com/group/morphia) remains active for discussion, although please take care to note if you are using the original project's code or the code from this fork.

Please use the GitHub issues tracker to report problems and make requests. A jUnit test case illustrating a bug has a higher chance of getting a fix that a more vague text description of course.

### Contributions

Fork this project, do you work, and ask for a pull request!

### Building

You will need:

* JDK 1.5 or better (1.6 and 1.7 are tested)
* Mongod running on localhost, on the default port.

### License

Apache, v2.

### History

This project is a fork of http://code.google.com/p/morphia/, taken from SVN trunk at revision 1826 (Jul 2012). This original work was authored by Scott Hernandez et al.

The intention of this fork is to:

1. Improve the documentation
2. Maintain compatibility with newer MongoDB driver releases
3. Fix and improve the code

### Travis Continuous Integration Build Status

Hopefully this thing is routinely green. Travis-CI monitors new code to this project and tests it on a variety of JDKs.

[![Build Status](https://secure.travis-ci.org/jmkgreen/morphia.png?branch=master)](https://travis-ci.org/jmkgreen/morphia)
