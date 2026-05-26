@ECHO OFF
SETLOCAL

SET MAVEN_VERSION=3.9.9
SET MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%
SET MAVEN_DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip

IF NOT EXIST "%MAVEN_HOME%\bin\mvn.cmd" (
    ECHO Downloading Apache Maven %MAVEN_VERSION%...
    MKDIR "%MAVEN_HOME%" 2>NUL
    powershell -Command "Expand-Archive -Force (Invoke-WebRequest '%MAVEN_DOWNLOAD_URL%' -OutFile '%TEMP%\maven.zip'; '%TEMP%\maven.zip') '%MAVEN_HOME%'"
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
