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

set CLASSPATH=%APP_HOME%\lib\bot.jar;%APP_HOME%\lib\telegrambots-6.9.7.1.jar;%APP_HOME%\lib\jsoup-1.16.2.jar;%APP_HOME%\lib\jakarta.json-2.0.1.jar;%APP_HOME%\lib\selenium-java-4.31.0.jar;%APP_HOME%\lib\jedis-5.1.0.jar;%APP_HOME%\lib\lombok-1.18.24.jar;%APP_HOME%\lib\webdrivermanager-5.7.0.jar;%APP_HOME%\lib\telegrambots-meta-6.9.7.1.jar;%APP_HOME%\lib\jersey-media-json-jackson-2.41.jar;%APP_HOME%\lib\jackson-datatype-jsr310-2.16.1.jar;%APP_HOME%\lib\jackson-jaxrs-base-2.16.1.jar;%APP_HOME%\lib\docker-java-3.3.5.jar;%APP_HOME%\lib\docker-java-core-3.3.5.jar;%APP_HOME%\lib\jackson-databind-2.16.1.jar;%APP_HOME%\lib\jackson-core-2.16.1.jar;%APP_HOME%\lib\jackson-jaxrs-json-provider-2.16.1.jar;%APP_HOME%\lib\jackson-module-jaxb-annotations-2.16.1.jar;%APP_HOME%\lib\docker-java-api-3.3.5.jar;%APP_HOME%\lib\jackson-annotations-2.16.1.jar;%APP_HOME%\lib\jersey-hk2-2.41.jar;%APP_HOME%\lib\jersey-container-grizzly2-http-2.41.jar;%APP_HOME%\lib\jersey-server-2.41.jar;%APP_HOME%\lib\httpmime-4.5.14.jar;%APP_HOME%\lib\httpclient-4.5.14.jar;%APP_HOME%\lib\commons-compress-1.26.0.jar;%APP_HOME%\lib\commons-io-2.15.1.jar;%APP_HOME%\lib\docker-java-transport-httpclient5-3.3.5.jar;%APP_HOME%\lib\httpclient5-5.2.1.jar;%APP_HOME%\lib\jcl-over-slf4j-1.7.30.jar;%APP_HOME%\lib\slf4j-api-2.0.12.jar;%APP_HOME%\lib\selenium-chrome-driver-4.31.0.jar;%APP_HOME%\lib\selenium-devtools-v133-4.31.0.jar;%APP_HOME%\lib\selenium-devtools-v134-4.31.0.jar;%APP_HOME%\lib\selenium-devtools-v135-4.31.0.jar;%APP_HOME%\lib\selenium-edge-driver-4.31.0.jar;%APP_HOME%\lib\selenium-firefox-driver-4.31.0.jar;%APP_HOME%\lib\selenium-ie-driver-4.31.0.jar;%APP_HOME%\lib\selenium-safari-driver-4.31.0.jar;%APP_HOME%\lib\selenium-support-4.31.0.jar;%APP_HOME%\lib\selenium-chromium-driver-4.31.0.jar;%APP_HOME%\lib\selenium-remote-driver-4.31.0.jar;%APP_HOME%\lib\selenium-manager-4.31.0.jar;%APP_HOME%\lib\selenium-http-4.31.0.jar;%APP_HOME%\lib\selenium-json-4.31.0.jar;%APP_HOME%\lib\selenium-os-4.31.0.jar;%APP_HOME%\lib\selenium-api-4.31.0.jar;%APP_HOME%\lib\commons-pool2-2.12.0.jar;%APP_HOME%\lib\json-20231013.jar;%APP_HOME%\lib\gson-2.10.1.jar;%APP_HOME%\lib\dec-0.1.2.jar;%APP_HOME%\lib\commons-lang3-3.14.0.jar;%APP_HOME%\lib\jakarta.xml.bind-api-2.3.3.jar;%APP_HOME%\lib\jakarta.activation-api-1.2.2.jar;%APP_HOME%\lib\jersey-client-2.41.jar;%APP_HOME%\lib\jersey-common-2.41.jar;%APP_HOME%\lib\hk2-locator-2.6.1.jar;%APP_HOME%\lib\javassist-3.29.2-GA.jar;%APP_HOME%\lib\jersey-entity-filtering-2.41.jar;%APP_HOME%\lib\hk2-api-2.6.1.jar;%APP_HOME%\lib\hk2-utils-2.6.1.jar;%APP_HOME%\lib\jakarta.inject-2.6.1.jar;%APP_HOME%\lib\grizzly-http-server-2.4.4.jar;%APP_HOME%\lib\jakarta.ws.rs-api-2.1.6.jar;%APP_HOME%\lib\jakarta.annotation-api-1.3.5.jar;%APP_HOME%\lib\jakarta.validation-api-2.0.2.jar;%APP_HOME%\lib\httpcore-4.4.16.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\commons-codec-1.11.jar;%APP_HOME%\lib\guava-33.4.6-jre.jar;%APP_HOME%\lib\jspecify-1.0.0.jar;%APP_HOME%\lib\auto-service-annotations-1.1.1.jar;%APP_HOME%\lib\opentelemetry-exporter-logging-1.48.0.jar;%APP_HOME%\lib\opentelemetry-sdk-extension-autoconfigure-1.48.0.jar;%APP_HOME%\lib\opentelemetry-sdk-extension-autoconfigure-spi-1.48.0.jar;%APP_HOME%\lib\opentelemetry-sdk-1.48.0.jar;%APP_HOME%\lib\opentelemetry-sdk-trace-1.48.0.jar;%APP_HOME%\lib\opentelemetry-sdk-metrics-1.48.0.jar;%APP_HOME%\lib\opentelemetry-sdk-logs-1.48.0.jar;%APP_HOME%\lib\opentelemetry-sdk-common-1.48.0.jar;%APP_HOME%\lib\opentelemetry-api-1.48.0.jar;%APP_HOME%\lib\opentelemetry-context-1.48.0.jar;%APP_HOME%\lib\byte-buddy-1.17.5.jar;%APP_HOME%\lib\docker-java-transport-3.3.5.jar;%APP_HOME%\lib\jna-5.13.0.jar;%APP_HOME%\lib\httpcore5-h2-5.2.jar;%APP_HOME%\lib\httpcore5-5.2.jar;%APP_HOME%\lib\osgi-resource-locator-1.0.3.jar;%APP_HOME%\lib\aopalliance-repackaged-2.6.1.jar;%APP_HOME%\lib\grizzly-http-2.4.4.jar;%APP_HOME%\lib\failureaccess-1.0.3.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\error_prone_annotations-2.36.0.jar;%APP_HOME%\lib\j2objc-annotations-3.0.0.jar;%APP_HOME%\lib\commons-exec-1.4.0.jar;%APP_HOME%\lib\bcpkix-jdk18on-1.76.jar;%APP_HOME%\lib\grizzly-framework-2.4.4.jar;%APP_HOME%\lib\bcutil-jdk18on-1.76.jar;%APP_HOME%\lib\bcprov-jdk18on-1.76.jar


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
