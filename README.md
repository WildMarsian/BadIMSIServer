# BadIMSIServer

## 1. Requirements 

BadIMSIServer uses the JDK 8. The following dependencies need to be installed on the host before:
```
sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update
sudo apt-get install -y oracle-java8-installer
```
BadIMSIServer uses the BadIMSICore module. It must be installed before you continue. 

See the <a href="https://github.com/WarenUT/BadIMSICore" target="_blank">BadIMSICore</a> installation.

## 2. Installation
To install BadIMSIServer, just simply clone the git repository.
```
git clone https://github.com/WarenUT/BadIMSIServer
```

After cloning the repository, we need to import it into an IDE like Eclipse or Netbeans and build the source to generate the jar files. 

BadIMSIServer has a jar file that we need to launch to communicate with the web part located in ~/BadIMSIServer/target/

You only need to launch the jar with all dependencies : 
```
cd ~/BadIMSIServer/target/
sudo java -jar BadIMSIServer-1.0-SNAPSHOT-jar-with-dependencies.jar
```

We can go on.
