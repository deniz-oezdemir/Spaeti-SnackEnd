# Deployment Guide (AWS EC2)

This guide explains how to deploy the e-commerce application to an AWS EC2 instance running Ubuntu.

## 1. Connect and Install Software
First, we connect to our server via SSH and install the necessary software: Java (and Git if we want to clone our project repository).

```bash
ssh -i /path/to/your-key.pem ubuntu@your-ec-ip-address

sudo apt-get update
sudo apt-get install openjdk-21-jdk git -y
```

## 2. Create a Deployment Directory
We create a directory on the server where our application will be deployed.

``` bash
mkdir ~/app
```

## 3. Build Locally and Upload the JAR

The remote machine does not have enough resources to build the application, so we build it locally and transfer the JAR file.

We update the application properties so the production build uses port 80.
We keep a separate properties file for testing on port 8080.

Build the application and upload the JAR file to the EC2 instance.

```bash
./gradlew build
scp -i /path/to/your-key.pem ./build/libs/spring-ecommerce-0.0.1-SNAPSHOT.jar ubuntu@your-ec2-ip-address:~/app/
```

## 4. Create and Run the Start Script

Create a start script similar to [start.sh](start.sh).

Run it with sudo to bind to port 80.

```bash
chmod +x ~/app/start.sh
sudo ~/app/start.sh
```


