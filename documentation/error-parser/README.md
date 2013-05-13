Configuration of a custom warnings parser for HTML Validation results
=====================================================================

Use as the pattern:

\[W3CMarkupValidation\]\s*\[([A-Z]+)\]\s*\[(.*)\] Line (\d+), Column (\d+): (.*)

Use as Groovy skript:

import hudson.plugins.warnings.parser.Warning
import hudson.plugins.analysis.util.model.Priority

String category = matcher.group(1)
String fileName = matcher.group(2)
String lineNumber = matcher.group(3)
String message = matcher.group(5)

return new Warning(fileName, Integer.parseInt(lineNumber), "W3C Parser", category, message, Priority.HIGH);

You can change the priority as you prefer

Configuration of a custom warnings parser for CSS Validation results
====================================================================
This is similar to the above, just use this pattern:

\[W3C(CSSValidation)\] \[ERROR\] \[(.+)\]\[\d\] Line (\d+): (.*)

and this Groovy skript:

import hudson.plugins.warnings.parser.Warning
import hudson.plugins.analysis.util.model.Priority

String category = matcher.group(1)
String fileName = matcher.group(2)
String lineNumber = matcher.group(3)
String message = matcher.group(4)

return new Warning(fileName, Integer.parseInt(lineNumber), "W3C Parser", category, message, Priority.HIGH);

see screenshot.png for a complete example.

![ScreenShot](https://raw.github.com/laeubi/validation-toolkit/master/documentation/error-parser/screenshot.png)
