# jabsorb
Custom jabsorb library for NGFW
# Build
In current flow we have created a new repo for jabsorb which is completely compatible with ngfw as we applied the patch changes in this repo it self.
To introduce new changes in this repo follow below steps:

To introduce new changes in this repo follow below steps:

- Update the `version` in build.properties.
- Update the webapps/jsonrpc/CHANGES.txt.
- Do the required changes and build the project by running ant commands from the repo root.
  - Install ant using apt.
  - Clean the project using `ant clean` command. (Fix your JAVA_HOME and PATH env variables if you face related errors after running ant commands).
  - Build the project using `ant` command.

- To test changes locally, create a zip file,
  - Create a directory with name such as jabsorb-1.2.5 (assuming your version=1.2.5)
  - Build the project as mentioned above.
  - Copy all the contents for jabsorb git repo and paste them under directory created from 4.a
  - Generate a zip by compressing this directory (right click from UI or command line) and provide name of that zip such as jabsorb-1.2.5. You now have the same zip as you would get from git release.
  - Replace the new zip with the older zip in `ngfw_src/downloads`.
  - Build NGFW locally and verify your changes.
- Commit and push the changes.
# Create release
- After jabsorb changes are verified, merge the branch in master. Make sure to include all the files generated as part of `ant` command.
- Create a tag with name {VERSION} and push it. For ex., `git tag -a 1.2.5 -m "Release 1.2.5" && git push origin 1.2.5`
- From github, create a new release using the tag.
# Update NGFW with latest jabsorb library version
- Download the zip from git based on the tag.
- In ngfw_src, update `downloads/Makefile`, this instructs build script to unzip the zip file.
```
ips=javamail-1_3_3_01.zip jabsorb-1.2.5.zip \
```
update `buildtools/jars.rb` instructing where to expect the jars after unzip. Such as below:
```
const_set(:Jabsorb, [ Jars.downloadTarget('jabsorb-1.2.5/jabsorb-1.2.5/jabsorb-1.2.5.jar')])
const_set(:Json, [ Jars.downloadTarget('jabsorb-1.2.5/jabsorb-1.2.5/json.jar')])
```
