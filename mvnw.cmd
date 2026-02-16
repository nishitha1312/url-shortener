@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.2.0
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __ MVNW_CMD__=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\MavenWrapperDownloader.java
@SET MAVEN_PROJECTBASEDIR=%~dp0

@SET MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties

@FOR /F "usebackq tokens=1,* delims==" %%A IN ("%MAVEN_WRAPPER_PROPERTIES%") DO (
    @IF "%%A"=="distributionUrl" SET DISTRIBUTION_URL=%%B
)

@SET DISTRIBUTION_ID=%DISTRIBUTION_URL:~-29,-4%
@SET MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\%DISTRIBUTION_ID%

@IF NOT EXIST "%MAVEN_HOME%" (
    @ECHO Downloading Maven...
    @MKDIR "%MAVEN_HOME%" 2>NUL
    @powershell -Command "Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%USERPROFILE%\.m2\wrapper\dists\maven.zip'"
    @powershell -Command "Expand-Archive -Path '%USERPROFILE%\.m2\wrapper\dists\maven.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists\' -Force"
    @DEL "%USERPROFILE%\.m2\wrapper\dists\maven.zip"
)

@SET MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd
@IF NOT EXIST "%MAVEN_CMD%" SET MAVEN_CMD=%MAVEN_HOME%\bin\mvn

"%MAVEN_CMD%" %*
