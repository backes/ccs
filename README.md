CCS: Eclipse Plugin to evaluate and visualize CCS expressions
=============================================================

This project was started in the context of the course "concurrent programming"
by Holger Hermanns at Saarland University in the winter term 2007/2008.

The initial source code was developed by Clemens Hammacher, and since then has
been subject to many improvements and fixes by other students.

The main interface is the eclipse plugin, but there also exists a command line tool
to evaluate CCS expressions.

License
-------

The whole source code is licensed under the ***Eclipse Public License, Version 1.0***.  
See http://www.eclipse.org/legal/epl-v10.html for the full license text.

Repository organization
-----------------------

These are the different folders in the repository:

* core:       the core of the ccs evaluator, including commandline tool
* doc:        some documentation (to be continued...)
* examples:   some ccs examples
* feature:    the feature to install into eclipse (contains only the ccs plugin)
* grappa:     a mod of grappa v1.2
* plugin:     the eclipse plugin (editor, view(s), actions, jobs, ...)
* rcp:        rcp application containing the ccs plugin
* updatesite: contains everything to be put on the eclipse update site

