@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul

rem ============================================================
rem CONFIGURACAO
rem Mantenha este arquivo .BAT na raiz do projeto, ao lado do pom.xml
rem ============================================================

set "APP_NAME=ProgramaMargem"
set "APP_VERSION=1.0.7"
set "APP_VENDOR=Plasnox"
set "MAIN_CLASS=Visao.telaInicial"

set "JAR_ORIGINAL=Projeto-0.0.1-SNAPSHOT.jar"
set "JAR_PORTATIL=ProgramaMargem.jar"

set "JAVAFX_JMODS=C:\Users\import3\Downloads\javafx-jmods-25.0.3"
set "MAVEN_CMD=C:\Ferramentas\apache-maven-3.9.16\bin\mvn.cmd"

set "PROJECT_DIR=%~dp0"
set "DIST_DIR=%PROJECT_DIR%dist"
set "RELEASE_DIR=%PROJECT_DIR%release-portatil"
set "APP_IMAGE_DIR=%RELEASE_DIR%\%APP_NAME%"
set "ZIP_FILE=%RELEASE_DIR%\%APP_NAME%-portatil-v%APP_VERSION%.zip"

cd /d "%PROJECT_DIR%"

echo.
echo ============================================================
echo EMPACOTAMENTO PORTATIL - %APP_NAME% v%APP_VERSION%
echo Projeto: %PROJECT_DIR%
echo ============================================================
echo.

rem ============================================================
rem VALIDACOES
rem ============================================================

if not exist "%PROJECT_DIR%pom.xml" (
    echo ERRO: pom.xml nao encontrado.
    echo Mantenha este BAT na raiz do projeto.
    goto :erro
)

if not exist "%JAVAFX_JMODS%\javafx.controls.jmod" (
    echo ERRO: Pasta dos JavaFX JMODS nao encontrada ou invalida:
    echo %JAVAFX_JMODS%
    goto :erro
)

if not exist "%JAVAFX_JMODS%\javafx.fxml.jmod" (
    echo ERRO: javafx.fxml.jmod nao encontrado em:
    echo %JAVAFX_JMODS%
    goto :erro
)

if not exist "%MAVEN_CMD%" (
    echo ERRO: Maven nao encontrado em:
    echo %MAVEN_CMD%
    goto :erro
)

set "JPACKAGE_CMD="

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\jpackage.exe" (
        set "JPACKAGE_CMD=%JAVA_HOME%\bin\jpackage.exe"
    )
)

if not defined JPACKAGE_CMD (
    for /f "delims=" %%I in ('where jpackage.exe 2^>nul') do (
        if not defined JPACKAGE_CMD (
            set "JPACKAGE_CMD=%%I"
        )
    )
)

if not defined JPACKAGE_CMD (
    echo ERRO: jpackage.exe nao encontrado.
    echo Configure JAVA_HOME para um JDK completo ou adicione o JDK ao PATH.
    goto :erro
)

echo Maven:
echo %MAVEN_CMD%
echo.
echo jpackage:
echo %JPACKAGE_CMD%
echo.
echo JavaFX JMODS:
echo %JAVAFX_JMODS%
echo.

rem ============================================================
rem LIMPEZA
rem ============================================================

if exist "%DIST_DIR%" (
    echo Removendo DIST antiga...
    rmdir /s /q "%DIST_DIR%"
)

if exist "%RELEASE_DIR%" (
    echo Removendo RELEASE antiga...
    rmdir /s /q "%RELEASE_DIR%"
)

mkdir "%DIST_DIR%"

if errorlevel 1 (
    echo ERRO: Nao foi possivel criar:
    echo %DIST_DIR%
    goto :erro
)

rem ============================================================
rem COMPILACAO E DEPENDENCIAS
rem ============================================================

echo.
echo ============================================================
echo 1/4 - COMPILANDO PROJETO E COPIANDO DEPENDENCIAS
echo ============================================================
echo.

call "%MAVEN_CMD%" clean package dependency:copy-dependencies ^
    -DskipTests ^
    -DincludeScope=runtime ^
    -DoutputDirectory=dist ^
    -DexcludeGroupIds=org.openjfx

if errorlevel 1 (
    echo.
    echo ERRO AO COMPILAR OU COPIAR DEPENDENCIAS.
    goto :erro
)

if not exist "%PROJECT_DIR%target\%JAR_ORIGINAL%" (
    echo.
    echo ERRO: JAR principal nao foi gerado:
    echo %PROJECT_DIR%target\%JAR_ORIGINAL%
    goto :erro
)

copy /Y "%PROJECT_DIR%target\%JAR_ORIGINAL%" "%DIST_DIR%\%JAR_PORTATIL%" >nul

if errorlevel 1 (
    echo.
    echo ERRO AO COPIAR O JAR PRINCIPAL PARA A PASTA DIST.
    goto :erro
)

rem Verifica dependencias essenciais do app portatil.
dir /b "%DIST_DIR%\sqlite-jdbc-*.jar" >nul 2>&1

if errorlevel 1 (
    echo.
    echo ERRO: sqlite-jdbc nao foi copiado para DIST.
    echo Verifique as dependencias do pom.xml.
    goto :erro
)

rem ============================================================
rem GERACAO DO APP-IMAGE
rem ============================================================

echo.
echo ============================================================
echo 2/4 - GERANDO APP PORTATIL COM RUNTIME INCLUIDO
echo ============================================================
echo.

"%JPACKAGE_CMD%" ^
    --type app-image ^
    --name "%APP_NAME%" ^
    --input "%DIST_DIR%" ^
    --main-jar "%JAR_PORTATIL%" ^
    --main-class "%MAIN_CLASS%" ^
    --dest "%RELEASE_DIR%" ^
    --module-path "%JAVAFX_JMODS%" ^
    --add-modules javafx.controls,javafx.fxml,java.sql,java.xml,java.desktop,java.logging,java.naming,java.management,jdk.crypto.ec ^
    --java-options "--enable-native-access=javafx.graphics" ^
    --java-options "-Dfile.encoding=UTF-8" ^
    --app-version "%APP_VERSION%" ^
    --vendor "%APP_VENDOR%"

if errorlevel 1 (
    echo.
    echo ERRO AO GERAR O APP PORTATIL.
    goto :erro
)

if not exist "%APP_IMAGE_DIR%\%APP_NAME%.exe" (
    echo.
    echo ERRO: Executavel nao encontrado:
    echo %APP_IMAGE_DIR%\%APP_NAME%.exe
    goto :erro
)

if not exist "%APP_IMAGE_DIR%\runtime\bin\server\jvm.dll" (
    echo.
    echo ERRO: Runtime Java nao foi incluido corretamente.
    goto :erro
)

if not exist "%APP_IMAGE_DIR%\app\%JAR_PORTATIL%" (
    echo.
    echo ERRO: JAR principal nao foi incluido no app-image.
    goto :erro
)

rem ============================================================
rem ARQUIVOS DE ORIENTACAO
rem ============================================================

echo.
echo ============================================================
echo 3/4 - GERANDO INFORMACOES DA VERSAO
echo ============================================================
echo.

(
    echo %APP_NAME% - Versao %APP_VERSION%
    echo Gerado em: %date% %time%
    echo.
    echo EXECUCAO:
    echo Abra %APP_NAME%.exe.
    echo.
    echo IMPORTANTE:
    echo Nao copie somente o arquivo EXE.
    echo Toda a pasta %APP_NAME% deve permanecer junta, pois ela contem:
    echo - o runtime Java;
    echo - os modulos JavaFX;
    echo - as bibliotecas do programa;
    echo - os arquivos necessarios para execucao.
    echo.
    echo BANCOS LOCAIS:
    echo As bases SQLite nao sao distribuidas dentro desta pasta.
    echo Na primeira execucao, elas sao sincronizadas automaticamente para:
    echo %%LOCALAPPDATA%%\ProgramaAnaliseMargem\bases
    echo.
    echo O programa pode usar a ultima copia local valida quando o servidor
    echo estiver temporariamente indisponivel.
) > "%APP_IMAGE_DIR%\LEIA-ME.txt"

(
    echo APP=%APP_NAME%
    echo VERSION=%APP_VERSION%
    echo BUILD_DATE=%date%
    echo BUILD_TIME=%time%
) > "%APP_IMAGE_DIR%\VERSAO.txt"

rem ============================================================
rem ZIP PARA DISTRIBUICAO
rem ============================================================

echo.
echo ============================================================
echo 4/4 - COMPACTANDO PACOTE PORTATIL
echo ============================================================
echo.

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command ^
    "Compress-Archive -Path '%APP_IMAGE_DIR%' -DestinationPath '%ZIP_FILE%' -Force"

if errorlevel 1 (
    echo.
    echo ERRO AO COMPACTAR O APP PORTATIL.
    goto :erro
)

if not exist "%ZIP_FILE%" (
    echo.
    echo ERRO: ZIP final nao foi criado.
    goto :erro
)

certutil -hashfile "%ZIP_FILE%" SHA256 > "%ZIP_FILE%.sha256.txt" 2>nul

echo.
echo ============================================================
echo EMPACOTAMENTO CONCLUIDO COM SUCESSO
echo ============================================================
echo.
echo Pasta portatil:
echo %APP_IMAGE_DIR%
echo.
echo Arquivo para distribuicao:
echo %ZIP_FILE%
echo.
echo Tamanho do ZIP:
for %%A in ("%ZIP_FILE%") do echo %%~zA bytes
echo.
echo Antes de distribuir, execute:
echo %APP_IMAGE_DIR%\%APP_NAME%.exe
echo.
echo Teste tambem o ZIP extraido em outra pasta.
echo.
pause
exit /b 0

:erro
echo.
echo ============================================================
echo EMPACOTAMENTO CANCELADO POR ERRO
echo ============================================================
echo.
pause
exit /b 1
