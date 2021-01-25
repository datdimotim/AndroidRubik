##Запуск сборки
docker build -t android-build . && docker run -v /home/dimotim/Загрузки/_android-build:/android-build --name AndroidBuild android-build