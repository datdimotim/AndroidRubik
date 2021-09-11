##Запуск сборки
docker build -t android-build . && docker run -v /home/dimotim/AndroidStudioProjects/AndroidRubik/:/project --name AndroidBuild android-build