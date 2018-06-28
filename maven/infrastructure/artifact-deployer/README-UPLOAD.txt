Steps for "deploying":

Deploy to a local directory:
- create & enable a new profile local.deploy.target defining:
  - protocol (set to "file", not "scpexe")
  - ot.host
  - ot.maven.repository.basepath

Manually copy to server (versions as of 2.6.2):
$ cd localrepo/repository
$ cd org/eclipse/objectteams
$ for f in * ; do scp -r ${f}/2.6.2 sherrmann@build.eclipse.org:downloads/objectteams/maven/3/repository/org/eclipse/objectteams/${f}/; done
$ for f in  objectteams-otdre objectteams-otdre-agent; do scp -r ${f}/1.3.2 sherrmann@build.eclipse.org:downloads/objectteams/maven/3/repository/org/eclipse/objectteams/${f}/; done
$ scp -r objectteams-compile-test/1.1.3 sherrmann@build.eclipse.org:downloads/objectteams/maven/3/repository/org/eclipse/objectteams/objectteams-compile-test/

$ cd localrepo/sites
$ scp -r * sherrmann@build.eclipse.org:downloads/objectteams/maven/3/sites/

On the server update maven-metadata (excluding objectteams-weaver-maven-plugin):
- edit maven-metadata.xml
$ for f in objectteams-[a-v]*; do md5sum $f/maven-metadata.xml > $f/maven-metadata.xml.md5; done
$ for f in objectteams-[a-v]*; do sha1sum $f/maven-metadata.xml > $f/maven-metadata.xml.sha1; done