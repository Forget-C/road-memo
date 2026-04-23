@ECHO OFF
SET APP_HOME=%~dp0
SET CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

IF NOT "%JAVA_HOME%"=="" (
  SET JAVA_CMD=%JAVA_HOME%\bin\java.exe
) ELSE (
  SET JAVA_CMD=java.exe
)

"%JAVA_CMD%" -classpath "%CLASSPATH%" -Dorg.gradle.appname=gradlew org.gradle.wrapper.GradleWrapperMain %*
