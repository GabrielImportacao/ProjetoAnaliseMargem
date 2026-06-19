@echo on
cd /d "C:\Users\import3\Desktop\Git\ProjetoAnaliseMargem"

if exist "dist" rmdir /s /q "dist"
if exist "release-installer" rmdir /s /q "release-installer"

mkdir dist

call mvn clean package dependency:copy-dependencies -DoutputDirectory=dist -DexcludeGroupIds=org.openjfx

if errorlevel 1 (
    echo.
    echo ERRO AO COMPILAR OU COPIAR DEPENDENCIAS
    pause
    exit /b 1
)

copy /Y "target\Projeto-0.0.1-SNAPSHOT.jar" "dist\ProgramaMargem.jar"

jpackage ^
--type exe ^
--name "ProgramaMargem" ^
--input "dist" ^
--main-jar "ProgramaMargem.jar" ^
--main-class "Visao.telaInicial" ^
--dest "release-installer" ^
--module-path "C:\Users\import3\Downloads\javafx-jmods-21.0.4" ^
--add-modules javafx.controls,javafx.fxml ^
--java-options "--enable-native-access=javafx.graphics" ^
--app-version "1.0.2" ^
--vendor "Gabriel" ^
--win-dir-chooser ^
--win-menu ^
--win-menu-group "Programa Margem" ^
--win-shortcut ^
--win-upgrade-uuid "6f2f1c45-67db-4f2f-9a17-5c6f2bb9d821"

echo.
echo FINALIZADO
echo.
pause