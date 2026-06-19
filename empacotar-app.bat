@echo on
cd /d "C:\Users\import3\Desktop\Git\ProjetoAnaliseMargem"

echo.
echo ==========================================
echo GERANDO APP-IMAGE DO PROGRAMA DE MARGEM
echo ==========================================
echo.

set "JAVAFX_JMODS=C:\Users\import3\Downloads\javafx-jmods-25.0.3"

if not exist "dist\ProgramaMargem.jar" (
    echo ERRO: dist\ProgramaMargem.jar nao encontrado.
    pause
    exit /b 1
)

if not exist "dist\ProgramaMargem_lib" (
    echo ERRO: dist\ProgramaMargem_lib nao encontrado.
    pause
    exit /b 1
)

if not exist "%JAVAFX_JMODS%" (
    echo ERRO: pasta de JavaFX JMODS nao encontrada:
    echo %JAVAFX_JMODS%
    pause
    exit /b 1
)

if exist "release-app" (
    rmdir /s /q "release-app"
)

jpackage ^
--type app-image ^
--name "ProgramaMargem" ^
--input "dist" ^
--main-jar "ProgramaMargem.jar" ^
--main-class "Visao.telaInicial" ^
--dest "release-app" ^
--module-path "%JAVAFX_JMODS%" ^
--add-modules javafx.controls,javafx.fxml ^
--java-options "--enable-native-access=javafx.graphics" ^
--win-console ^
--app-version "1.0.0" ^
--vendor "Plasnox"

if errorlevel 1 (
    echo.
    echo ERRO AO GERAR APP-IMAGE.
    pause
    exit /b 1
)

echo.
echo APP-IMAGE GERADO COM SUCESSO.
echo.
echo Execute:
echo release-app\ProgramaMargem\ProgramaMargem.exe
echo.
pause