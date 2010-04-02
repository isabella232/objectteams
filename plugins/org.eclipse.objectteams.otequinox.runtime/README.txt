Libraries used in this transformer.hook OSGI extension bundle can not be
packaged as inner jar files (e.g. otre.jar and BCEL.jar), as OSGI does not
know how to deal with those.

See https://bugs.eclipse.org/bugs/show_bug.cgi?id=143283