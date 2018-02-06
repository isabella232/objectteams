Steps for "deploying":

Deploy to a local directory:
- create & enable a new profile local.deploy.target defining:
  - ot.host
  - ot.maven.repository.basepath
  
Temporarily modify upload URLs from scpexe: to file: in
- parent-pom
- parent-pom-otdre
- artifact-deployer
- testproject

Manually copy to server (versions as of 2.6.1):
$ cd localrepo/repository
$ for f in * ; do scp -r ${f}/2.6.1 sherrmann@build.eclipse.org:downloads/objectteams/maven/3/repository/org/eclipse/objectteams/${f}/; done
$ for f in  objectteams-otdre objectteams-otdre-agent; do scp -r ${f}/1.3.1 sherrmann@build.eclipse.org:downloads/objectteams/maven/3/repository/org/eclipse/objectteams/${f}/; done
$ scp -r objectteams-compile-test/1.1.2 sherrmann@build.eclipse.org:downloads/objectteams/maven/3/repository/org/eclipse/objectteams/objectteams-compile-test/

$ cd localrepo/sites
$ scp -r * sherrmann@build.eclipse.org:downloads/objectteams/maven/3/sites/

On the server update maven-metadata (excluding objectteams-weaver-maven-plugin):
- edit maven-metadata.xml
$ for f in objectteams-[a-v]*; do md5sum $f/maven-metadata.xml > $f/maven-metadata.xml.md5; done
$ for f in objectteams-[a-v]*; do sha1sum $f/maven-metadata.xml > $f/maven-metadata.xml.sha1; done