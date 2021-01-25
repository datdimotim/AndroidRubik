FROM ubuntu:18.04
RUN apt update
RUN apt install -y git
RUN apt install -y default-jdk
RUN apt install -y android-sdk
ENV ANDROID_HOME=/usr/lib/android-sdk/

CMD cd /android-build && \
    git clone https://github.com/datdimotim/AndroidRubik.git && \
    cd /android-build/AndroidRubik && \
    chmod 777 gradlew && \
    echo -e "\nd56f5187479451eabf01fb78af6dfcb131a6481e\n24333f8a63b6825ea9c5514f83c2829b004d1fee" > /usr/lib/android-sdk/licenses/android-sdk-license && \
    ./gradlew build
