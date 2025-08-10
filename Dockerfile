FROM openjdk:11-jdk

# Installiere Android SDK
ENV ANDROID_HOME /opt/android-sdk
ENV ANDROID_SDK_ROOT $ANDROID_HOME
ENV PATH $PATH:$ANDROID_HOME/emulator
ENV PATH $PATH:$ANDROID_HOME/tools
ENV PATH $PATH:$ANDROID_HOME/tools/bin
ENV PATH $PATH:$ANDROID_HOME/platform-tools

# Installiere notwendige Pakete
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    curl \
    git \
    && rm -rf /var/lib/apt/lists/*

# Lade Android SDK herunter
RUN wget -q https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip -O /tmp/commandlinetools.zip
RUN mkdir -p $ANDROID_HOME/cmdline-tools
RUN unzip /tmp/commandlinetools.zip -d $ANDROID_HOME/cmdline-tools
RUN mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest

# Akzeptiere Android SDK Lizenzen
RUN yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses

# Installiere Android SDK Komponenten
RUN $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0"

# Arbeitsverzeichnis
WORKDIR /app

# Kopiere Projekt-Dateien
COPY . .

# Berechtigungen für Gradle Wrapper
RUN chmod +x gradlew

# Build und Test
CMD ["./gradlew", "build"]