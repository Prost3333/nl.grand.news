@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  nl.grand.news startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and NL_GRAND_NEWS_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\bot.jar;%APP_HOME%\lib\telegrambots-6.9.7.1.jar;%APP_HOME%\lib\jsoup-1.16.2.jar;%APP_HOME%\lib\jakarta.json-2.0.1.jar;%APP_HOME%\lib\jedis-5.1.0.jar;%APP_HOME%\lib\caffeine-3.1.8.jar;%APP_HOME%\lib\rome-1.18.0.jar;%APP_HOME%\lib\lombok-1.18.24.jar;%APP_HOME%\lib\google-cloud-translate-2.38.0.jar;%APP_HOME%\lib\telegrambots-meta-6.9.7.1.jar;%APP_HOME%\lib\jersey-media-json-jackson-2.41.jar;%APP_HOME%\lib\jackson-datatype-jsr310-2.17.0.jar;%APP_HOME%\lib\jackson-jaxrs-json-provider-2.17.0.jar;%APP_HOME%\lib\jackson-module-jaxb-annotations-2.17.0.jar;%APP_HOME%\lib\jackson-jaxrs-base-2.17.0.jar;%APP_HOME%\lib\jackson-databind-2.17.0.jar;%APP_HOME%\lib\jackson-core-2.17.0.jar;%APP_HOME%\lib\jackson-annotations-2.17.0.jar;%APP_HOME%\lib\jersey-hk2-2.41.jar;%APP_HOME%\lib\jersey-container-grizzly2-http-2.41.jar;%APP_HOME%\lib\jersey-server-2.41.jar;%APP_HOME%\lib\httpmime-4.5.14.jar;%APP_HOME%\lib\httpclient-4.5.14.jar;%APP_HOME%\lib\commons-io-2.15.1.jar;%APP_HOME%\lib\rome-utils-1.18.0.jar;%APP_HOME%\lib\slf4j-api-2.0.11.jar;%APP_HOME%\lib\commons-pool2-2.12.0.jar;%APP_HOME%\lib\json-20231013.jar;%APP_HOME%\lib\gson-2.10.1.jar;%APP_HOME%\lib\proto-google-cloud-translate-v3beta1-0.120.0.jar;%APP_HOME%\lib\proto-google-cloud-translate-v3-2.38.0.jar;%APP_HOME%\lib\checker-qual-3.42.0.jar;%APP_HOME%\lib\error_prone_annotations-2.26.1.jar;%APP_HOME%\lib\jdom2-2.0.6.1.jar;%APP_HOME%\lib\grpc-api-1.62.2.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\grpc-stub-1.62.2.jar;%APP_HOME%\lib\grpc-protobuf-1.62.2.jar;%APP_HOME%\lib\grpc-protobuf-lite-1.62.2.jar;%APP_HOME%\lib\api-common-2.29.1.jar;%APP_HOME%\lib\auto-value-annotations-1.10.4.jar;%APP_HOME%\lib\javax.annotation-api-1.3.2.jar;%APP_HOME%\lib\j2objc-annotations-3.0.0.jar;%APP_HOME%\lib\protobuf-java-3.25.2.jar;%APP_HOME%\lib\proto-google-common-protos-2.37.1.jar;%APP_HOME%\lib\guava-33.1.0-jre.jar;%APP_HOME%\lib\failureaccess-1.0.2.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\gax-2.46.1.jar;%APP_HOME%\lib\opencensus-api-0.31.1.jar;%APP_HOME%\lib\opentelemetry-api-1.36.0.jar;%APP_HOME%\lib\opentelemetry-context-1.36.0.jar;%APP_HOME%\lib\gax-grpc-2.46.1.jar;%APP_HOME%\lib\grpc-inprocess-1.62.2.jar;%APP_HOME%\lib\grpc-core-1.62.2.jar;%APP_HOME%\lib\annotations-4.1.1.4.jar;%APP_HOME%\lib\animal-sniffer-annotations-1.23.jar;%APP_HOME%\lib\grpc-alts-1.62.2.jar;%APP_HOME%\lib\grpc-grpclb-1.62.2.jar;%APP_HOME%\lib\conscrypt-openjdk-uber-2.5.2.jar;%APP_HOME%\lib\grpc-auth-1.62.2.jar;%APP_HOME%\lib\grpc-netty-shaded-1.62.2.jar;%APP_HOME%\lib\grpc-util-1.62.2.jar;%APP_HOME%\lib\perfmark-api-0.27.0.jar;%APP_HOME%\lib\grpc-googleapis-1.62.2.jar;%APP_HOME%\lib\grpc-xds-1.62.2.jar;%APP_HOME%\lib\opencensus-proto-0.2.0.jar;%APP_HOME%\lib\grpc-services-1.62.2.jar;%APP_HOME%\lib\re2j-1.7.jar;%APP_HOME%\lib\gax-httpjson-2.46.1.jar;%APP_HOME%\lib\google-http-client-gson-1.44.1.jar;%APP_HOME%\lib\protobuf-java-util-3.25.2.jar;%APP_HOME%\lib\threetenbp-1.6.8.jar;%APP_HOME%\lib\google-cloud-core-2.36.1.jar;%APP_HOME%\lib\proto-google-iam-v1-1.32.1.jar;%APP_HOME%\lib\google-cloud-core-http-2.36.1.jar;%APP_HOME%\lib\google-api-client-2.4.0.jar;%APP_HOME%\lib\commons-codec-1.16.1.jar;%APP_HOME%\lib\google-oauth-client-1.35.0.jar;%APP_HOME%\lib\google-http-client-apache-v2-1.44.1.jar;%APP_HOME%\lib\google-http-client-appengine-1.44.1.jar;%APP_HOME%\lib\opencensus-contrib-http-util-0.31.1.jar;%APP_HOME%\lib\google-api-services-translate-v2-rev20170525-2.0.0.jar;%APP_HOME%\lib\google-auth-library-credentials-1.23.0.jar;%APP_HOME%\lib\google-auth-library-oauth2-http-1.23.0.jar;%APP_HOME%\lib\google-http-client-1.44.1.jar;%APP_HOME%\lib\httpcore-4.4.16.jar;%APP_HOME%\lib\grpc-context-1.62.2.jar;%APP_HOME%\lib\google-http-client-jackson2-1.44.1.jar;%APP_HOME%\lib\commons-lang3-3.14.0.jar;%APP_HOME%\lib\jakarta.xml.bind-api-2.3.3.jar;%APP_HOME%\lib\jakarta.activation-api-1.2.2.jar;%APP_HOME%\lib\jersey-client-2.41.jar;%APP_HOME%\lib\jersey-common-2.41.jar;%APP_HOME%\lib\hk2-locator-2.6.1.jar;%APP_HOME%\lib\javassist-3.29.2-GA.jar;%APP_HOME%\lib\jersey-entity-filtering-2.41.jar;%APP_HOME%\lib\hk2-api-2.6.1.jar;%APP_HOME%\lib\hk2-utils-2.6.1.jar;%APP_HOME%\lib\jakarta.inject-2.6.1.jar;%APP_HOME%\lib\grizzly-http-server-2.4.4.jar;%APP_HOME%\lib\jakarta.ws.rs-api-2.1.6.jar;%APP_HOME%\lib\jakarta.annotation-api-1.3.5.jar;%APP_HOME%\lib\jakarta.validation-api-2.0.2.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\osgi-resource-locator-1.0.3.jar;%APP_HOME%\lib\aopalliance-repackaged-2.6.1.jar;%APP_HOME%\lib\grizzly-http-2.4.4.jar;%APP_HOME%\lib\grizzly-framework-2.4.4.jar;%APP_HOME%\lib\byte-buddy-1.14.9.jar


@rem Execute nl.grand.news
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %NL_GRAND_NEWS_OPTS%  -classpath "%CLASSPATH%" nl.grand.news.Main %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable NL_GRAND_NEWS_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%NL_GRAND_NEWS_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
