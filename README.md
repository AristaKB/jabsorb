# jabsorb
Custom jabsorb library for NGFW
# Build
In current flow we have created a new repo for jabsorb which is completely compatible with ngfw as we applied the patch changes in this repo it self.
To introduce new changes in this repo follow below steps:

To introduce new changes in this repo follow below steps:

- Update the version in build.properties.
- Update the webapps/jsonrpc/CHANGES.txt.
- Do the required changes and build the project by running ant commands from the repo root.
  - Install ant using apt
  - Clean the project using `ant clean` command. (Fix your JAVA_HOME and PATH env variables if you face related errors after running ant commands).
  - Build the project using `ant` command.

- To test changes locally without creating a git release, create a zip file,
  - Create a directory with name such as jabsorb-1.2.5 (assuming your version=1.2.5)
  - Build the project as mentioned above.
  - Copy all the contents for jabsorb git repo and paste them under directory created from 4.a
  - Generate a zip by compressing this directory (right click from UI or command line) and provide name of that zip such as jabsorb-1.2.5-src. You now have the same zip as you would get from git release.
  - Replace the new zip with the older zip in ngfw_src/downloads.
  - Build NGFW locally and verify your changes.
- Commit and push the changes.
# Create release tag
- Create a tag with name {VERSION}-src and push it. For ex., `git tag -a 1.2.5-src -m "Release 1.2.5" && git push origin 1.2.5-src`
# Update NGFW
- In ngfw_src, update `buildtools/jars.rb` as per the latest dependent libraries. Such as below:
```
def Jars.findJars
## Named groups of jars
const_set(:CommonsLang, [ Jars.downloadTarget('commons-lang3-3.9/commons-lang3-3.9.jar') ])
const_set(:Log4j, [ Jars.downloadTarget('apache-log4j-1.2.16/log4j-1.2.16.jar') ])
const_set(:JavaMailApi, [ Jars.downloadTarget('javamail-1.3.3_01/lib/mailapi.jar') ])
const_set(:Jabsorb, [ Jars.downloadTarget('jabsorb-1.2.5-src/jabsorb-1.2.5/jabsorb-1.2.5.jar')])
const_set(:Json, [ Jars.downloadTarget('jabsorb-1.2.5-src/jabsorb-1.2.5/json.jar')])
const_set(:GetText, [ Jars.downloadTarget('gettext-commons-0.9.1/gettext-commons-0.9.1.jar') ])
const_set(:JakartaActivation, [ Jars.downloadTarget('jakarta.activation-1.2.1/jakarta.activation-1.2.1.jar') ])
const_set(:JavaTransaction, [ Jars.downloadTarget('jta-1.1/jta-1.1.jar') ])
const_set(:Slf4j, [ Jars.downloadTarget( 'slf4j-1.4.3/slf4j-log4j12-1.4.3.jar'),
Jars.downloadTarget( 'slf4j-1.4.3/slf4j-api-1.4.3.jar' ) ])
```
- Update `ngfw_src/downloads/Makefile`
```
ips=javamail-1_3_3_01.zip jabsorb-1.2.5-src.zip \
jradius-client-1.0.0-release.zip apache-taglibs-standard-1.2.5.zip \
selenium-java-3.141.59.zip geoip2-2.17.0-with-dependencies.zip
```