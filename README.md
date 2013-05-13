validation-toolkit
==================

Providing a toolkit to automate common validation tasks for web-documents, especially the W3C toolkit (markup, css, ...)

The aim is to allow automated testing of single (X)HTML and CSS documents, or even a whole page against common validation services e.g. the W3C Markup Validation service.
The current version includes an ANT task to check a document and either fail or just print out all problems, this can then be used in Integration Servers like Hudson/Jenkins to run auotmated tests.

features
--------
- [x] W3C MarkupValidator Task
- [x] Hudson/Jenkins parser for generated Markup-Validator errors
- [x] recursively check HTML document using public URLs
- [x] W3C CSS Validation Task
- [ ] Hudson/Jenkins parser for generated CSS-Validator errors
- [ ] JS Linting/Checking task
- [ ] Hudson/Jenkins parser for generated JS Linting/Checking errors
- [ ] Full-(recursive)Page checker that validates/checks HTML, CSS and JS
- [ ] ... ideas are welcome ...

general setup
-------------
Fist download the latest build from the 'assembly' directory (or build your own version, see below).
Extract all files and define a new task in your build.xml
```xml
<path id="classpath.validator" >
	<fileset dir="/path/to/files">
		<include name="*.jar" />
	</fileset>
</path>
<taskdef classname="de.laeubisoft.tools.ant.validation.W3CMarkupValidationTask" classpathref="classpath.markupvalidator" name="W3CMarkupValidation"></taskdef> 
<taskdef classname="de.laeubisoft.tools.ant.validation.W3CCSSValidationTask" classpathref="classpath.markupvalidator" name="W3CCSSValidation"></taskdef>
```
Now you can use the new tasks like this:
usage W3CMarkupValidationTask
-----------------------------
```xml
<W3CMarkupValidation uri="http://example.test" />
```
This will check the page against the online version of the validator (http://validator.w3.org/), alternatively you can validate a fragment
```xml
<W3CMarkupValidation fragment="${propertyHoldingHTMLFragmentToValidate}" />
```
or even use file upload
```xml
<W3CMarkupValidation file="/file/to/send.html" />
```
It is recommend to install a private copy of the validator (see http://validator.w3.org/docs/install.html) for maximum performance, you can specify an alternative URL (this of course also applies to fragments and file uploads) like this:
```xml
<W3CMarkupValidation uri="http://example.test" validator="http://localhost/w3c-markup-validator/check" />
```
If required you can specify charset and/or doctype via the charset and doctype attribute as well as debug option
```xml
<W3CMarkupValidation charset="UTF-8" doctype="XHTML1.1" uri="http://example.test" validator="http://localhost/w3c-markup-validator/check" debug="true" />
```
With the fail attribute, you can control if the build should be failed (fail="true") or only a message should be printed out on validation errors:
```xml
<W3CMarkupValidation fail="false" uri="http://example.test" validator="http://localhost/w3c-markup-validator/check" />
```
If you want to check all pages of a page (connected via links) you can specify the recurse attribute (this is currently only supported for URIs!), use the embedded ignore element to skip pages, links matching any of the ignore pattern are not checked.
```xml
<W3CMarkupValidation recurse="true" uri="http://example.test" validator="http://localhost/w3c-markup-validator/check">
	<ignore>.*bad_page\.html</ignore>
	<ignore>.*\.zip</ignore>
</W3CMarkupValidation>
```
usage W3CCSSValidationTask
--------------------------
```xml
<W3CCSSValidation uri="http://example.test" />
```
This will check the page against the online version of the validator (http://jigsaw.w3.org/css-validator), alternatively you can validate a css text
```xml
<W3CCSSValidation cssText="${propertyHoldingCSSToValidate}" />
```
or even use file upload
```xml
<W3CCSSValidation file="/file/to/send.css" />
```
Like with W3CMarkupValidation it is recommend to install a private copy of the validator (see note below), you can specify an alternative URL (this of course also applies to fragments and file uploads) like this:
```xml
<W3CCSSValidation uri="http://example.test" validator="http://localhost/css-validator/validator" />
```
Optionally you can specify usermedium, profile, lang and warning level as attributes as described in the user-manual (http://jigsaw.w3.org/css-validator/manual.html#expert) if not given the validators defaults are used

*One note about private install*: deviating from the manual under http://jigsaw.w3.org/css-validator/DOWNLOAD.html i recommend the following procedure (requires csv, ant and an applicationserver like Tomcat or Jetty):
- checkout the source as described here: http://dev.w3.org/cvsweb/2002/css-validator/
- build the validator with 'ant war'
- this will produce a css-validator.war file youcan copy to the deply dir of your application server
This is IMO much more straigth forward then the "offical" description.


licence
-------
The code and its dependencies are distributed under 'The Apache Software License, Version 2.0' see licenses.xml for details.

building
--------
The code is build with maven 3, you can use mvn eclipse:eclipse task to generate an Eclipse Project, and mvn install to build from commandline, since the code is very simple at the moment you can of course choose to build it with the tool you prefer as long as all dependencies are on the classpath (e.g Ivy can resolve and download maven dependencies too).

dependencies
------------
- ant-1.8.0.jar (it should build with previous versions of ant as well since it does not use any special features)
- ant-launcher-1.8.0.jar
- tagsoup-1.2.jar (used for parsing html documents and extract links)
- commons-httpclient-3.1.jar (used to communicate with the W3C API)
- commons-logging-1.0.4.jar
- commons-codec-1.2.jar



