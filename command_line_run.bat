@ECHO OFF
cd /d "E:\Projects\Contest\contest\"
mvn clean test -Dsurefire.suiteXmlFiles=testng.xml