@ECHO OFF

:: cding to project root folder (/d is used because there is a drive letter change)
cd /d "E:\Projects\Contest\contest\"

:: getting prod and backup db paths and passwords from the config file and using them as variables
For /F "tokens=1* delims==" %%A IN (config.properties) DO (IF "%%A"=="prodDb_path" set prodDb_path=%%B)
For /F "tokens=1* delims==" %%A IN (config.properties) DO (IF "%%A"=="backupDb_path" set backupDb_path=%%B)
For /F "tokens=1* delims==" %%A IN (config.properties) DO (IF "%%A"=="prodDb_password" set prodDb_password=%%B)
For /F "tokens=1* delims==" %%A IN (config.properties) DO (IF "%%A"=="backupDb_password" set backupDb_password=%%B)

:: cding to prod db and generating db dump file
cd /d "%prodDb_path%"
mysqldump --port 3306 -u root -p%prodDb_password% --databases main > dump.sql

:: cding to backup db and applying db dump file
cd /d "%backupDb_path%"
mysql < "%prodDb_path%dump.sql" --port 3308 -u root -p%backupDb_password%