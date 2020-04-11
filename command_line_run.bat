@ECHO OFF
e:
cd Projects\Contest\contest\
mvn clean test -Dsurefire.suiteXmlFiles=testng.xml