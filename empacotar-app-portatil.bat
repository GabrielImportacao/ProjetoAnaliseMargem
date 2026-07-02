@echo on
cd /d "C:\Users\import3\Desktop\Git\ProjetoAnaliseMargem"

set "JAVAFX_JMODS=C:\Users\import3\Downloads\javafx-jmods-25.0.3"
set "MAVEN_CMD=C:\Ferramentas\apache-maven-3.9.16\bin\mvn.cmd"

if not exist "%JAVAFX_JMODS%\javafx.controls.jmod" (
    echo.
    echo ERRO: Pasta dos JavaFX JMODS nao encontrada ou invalida:
    echo %JAVAFX_JMODS%
    echo.
    pause
    exit /b 1
)

if not exist "%MAVEN_CMD%" (
    echo.
    echo ERRO: Maven nao encontrado em:
    echo %MAVEN_CMD%
    echo.
    pause
    exit /b 1
)

if exist "dist" rmdir /s /q "dist"
if exist "release-portatil" rmdir /s /q "release-portatil"

mkdir dist

echo.
echo COMPILANDO PROJETO E COPIANDO DEPENDENCIAS...
echo.

call "%MAVEN_CMD%" clean package dependency:copy-dependencies -DoutputDirectory=dist -DexcludeGroupIds=org.openjfx

if errorlevel 1 (
    echo.
    echo ERRO AO COMPILAR OU COPIAR DEPENDENCIAS
    pause
    exit /b 1
)

if not exist "target\Projeto-0.0.1-SNAPSHOT.jar" (
    echo.
    echo ERRO: JAR principal nao foi gerado:
    echo target\Projeto-0.0.1-SNAPSHOT.jar
    echo.
    pause
    exit /b 1
)

copy /Y "target\Projeto-0.0.1-SNAPSHOT.jar" "dist\ProgramaMargem.jar"

if errorlevel 1 (
    echo.
    echo ERRO AO COPIAR O JAR PRINCIPAL PARA A PASTA DIST
    pause
    exit /b 1
)

echo.
echo GERANDO APP PORTATIL...
echo.

jpackage ^
--type app-image ^
--name "ProgramaMargem" ^
--input "dist" ^
--main-jar "ProgramaMargem.jar" ^
--main-class "Visao.telaInicial" ^
--dest "release-portatil" ^
--module-path "%JAVAFX_JMODS%" ^
--add-modules javafx.controls,javafx.fxml,java.sql,java.xml,java.desktop,java.logging,java.naming,java.management,jdk.crypto.ec ^
--java-options "--enable-native-access=javafx.graphics" ^
--app-version "1.0.6" ^
--vendor "Plasnox"

if errorlevel 1 (
    echo.
    echo ERRO AO GERAR APP PORTATIL
    pause
    exit /b 1
)

echo.
echo FINALIZADO
echo App portatil gerado em:
echo release-portatil\ProgramaMargem
echo.
pause