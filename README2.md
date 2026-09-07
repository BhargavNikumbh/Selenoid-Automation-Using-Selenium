# Selenoid Setup Guide – Windows + AWS EC2 + Docker

This guide explains how to set up **Selenoid**, **Selenoid UI**, and Chrome browser containers on an **AWS EC2 instance using Docker**.

The Selenium automation framework runs on a Windows machine, while the actual browser execution takes place remotely on the AWS EC2 instance.

An **SSH tunnel** is used to securely connect the local Selenium tests and Selenoid UI to the EC2 instance.

---

## Architecture

```text
                    Windows Machine
                          │
                    Java + TestNG
                          │
                    Selenium Tests
                          │
                      SSH Tunnel
                          │
                          ▼
                 AWS EC2 Instance
                 Amazon Linux 2023
                          │
             ┌────────────┴────────────┐
             │                         │
         Selenoid                 Selenoid UI
          :4444                      :8090
             │
             │ Docker
             ▼
       Chrome Container
       chrome_128.0
```

The Java/TestNG automation framework runs on the Windows machine.

The browser itself runs remotely inside a Docker container on the AWS EC2 instance.

Selenium tests connect to Selenoid through the SSH tunnel using:

http://localhost:4444/wd/hub

The Java/TestNG test code runs on the Windows machine.

The browser itself runs remotely inside a Docker container on the AWS EC2 instance.

Prerequisites
AWS EC2

### Create an AWS EC2 instance using:

Amazon Linux 2023
Docker
An appropriate Security Group
An SSH key pair

An Elastic IP is recommended so the EC2 public IP does not change after restarting the instance.

Windows Machine

Make sure the following are installed:

Java
Maven
Selenium WebDriver
TestNG
IntelliJ IDEA or another Java IDE
OpenSSH client
1. Install Docker on Amazon Linux 2023

Amazon Linux 2023 uses dnf instead of apt.

Update the system:

sudo dnf update -y


Install Docker:

sudo dnf install docker -y


Start Docker:

sudo systemctl start docker


Enable Docker to start automatically after reboot:

sudo systemctl enable docker


Verify Docker:

docker --version


Allow the ec2-user to run Docker without sudo:

sudo usermod -aG docker ec2-user


Log out and reconnect to the EC2 instance after running this command.

Verify:

docker ps

2. Pull Selenoid Images

Pull the Selenoid image:

docker pull aerokube/selenoid:1.11.3


Pull the Selenoid UI image:

docker pull aerokube/selenoid-ui

3. Pull the Chrome Browser Image

For example, this project uses Chrome 128.0:

docker pull selenoid/vnc:chrome_128.0


Verify the images:

docker images


You should see something similar to:

aerokube/selenoid
aerokube/selenoid-ui
selenoid/vnc


Important: The Chrome version in browsers.json must match the Docker image tag.

For example:

{
"chrome": {
"default": "128.0",
"versions": {
"128.0": {
"image": "selenoid/vnc:chrome_128.0",
"port": "4444",
"path": "/",
"tmpfs": {
"/tmp": "size=128m"
}
}
}
}
}

4. Create the Selenoid Configuration

Create the Selenoid configuration directory on the EC2 instance:

mkdir -p /home/ec2-user/selenoid/config


Create:

/home/ec2-user/selenoid/config/browsers.json


Example:

{
"chrome": {
"default": "128.0",
"versions": {
"128.0": {
"image": "selenoid/vnc:chrome_128.0",
"port": "4444",
"path": "/",
"tmpfs": {
"/tmp": "size=128m"
}
}
}
}
}


The browser version must match the Docker image.

5. Create the Selenoid Docker Network

Create a dedicated Docker network:

docker network create selenoid


Verify:

docker network ls


You should see:

selenoid


The Selenoid container and dynamically-created browser containers will use this network.

6. Start Selenoid

Start the Selenoid container:

docker run -d \
--name selenoid \
--restart unless-stopped \
--network selenoid \
-p 4444:4444 \
-v /var/run/docker.sock:/var/run/docker.sock \
-v /home/ec2-user/selenoid/config:/etc/selenoid:ro \
aerokube/selenoid:1.11.3 \
-container-network selenoid

Important options

The Docker socket:

/var/run/docker.sock


allows Selenoid to create and manage browser containers.

The Selenoid network:

-container-network selenoid


ensures that dynamically-created Chrome containers are connected to the same Docker network as Selenoid.

7. Verify Selenoid

Check running containers:

docker ps


You should see:

selenoid


Check the Selenoid logs:

docker logs selenoid


Follow the logs:

docker logs -f selenoid


Check the Selenoid status:

curl http://localhost:4444/status


Selenoid should return a JSON status response.

8. Start Selenoid UI

Start Selenoid UI:

docker run -d \
--name selenoid-ui \
--restart unless-stopped \
--network selenoid \
-p 8090:8080 \
aerokube/selenoid-ui \
--selenoid-uri http://selenoid:4444


Check the containers:

docker ps


You should see:

selenoid
selenoid-ui


Check Selenoid UI:

curl http://localhost:8090


The command should return the Selenoid UI HTML.

9. AWS Security Group

Because the Selenoid UI and Selenium connection are accessed through an SSH tunnel, ports 4444 and 8090 do not need to be publicly accessible.

The Security Group only needs SSH access:

Inbound:

Port: 22
Protocol: TCP
Source: Your allowed IP/network


Do not expose the following ports publicly unless there is a specific requirement:

4444
8090


This provides a more secure setup.

10. SSH Tunnel – Selenoid UI

The SSH tunnel allows the Windows machine to access Selenoid UI running on the EC2 instance.

From Windows PowerShell:

ssh -i "C:\path\to\Selenoid-Key.pem" -L 8090:localhost:8090 ec2-user@<EC2_PUBLIC_IP>


Example:

ssh -i "C:\Users\bharg\Desktop\CV\Ireland-CV\Testing\Spanish-Point\Selenoid-Key.pem" -L 8090:localhost:8090 ec2-user@<EC2_PUBLIC_IP>


Keep the SSH terminal open.

Selenoid UI can then be accessed from the Windows machine using:

http://localhost:8090

11. SSH Tunnel – Selenium + Selenoid UI

For running Selenium tests, forward both ports:

ssh -i "C:\path\to\Selenoid-Key.pem" `
  -L 8090:localhost:8090 `
-L 4444:localhost:4444 `
ec2-user@<EC2_PUBLIC_IP>


Example:

ssh -i "C:\Users\bharg\Desktop\CV\Ireland-CV\Testing\Spanish-Point\Selenoid-Key.pem" `
  -L 8090:localhost:8090 `
-L 4444:localhost:4444 `
ec2-user@<EC2_PUBLIC_IP>


Keep this terminal open while running Selenium tests.

12. Verify the SSH Tunnel

From Windows:

curl http://localhost:8090


Selenoid UI HTML should be returned.

Check Selenoid:

curl http://localhost:4444/status


Selenoid status information should be returned.

13. Selenium RemoteWebDriver Configuration

Because the SSH tunnel forwards local port 4444 to the EC2 Selenoid port, Selenium should connect to:

http://localhost:4444/wd/hub


Example:

URL remoteUrl = new URL("http://localhost:4444/wd/hub");

ChromeOptions options = new ChromeOptions();

options.addArguments("--incognito");

options.setCapability("browserVersion", "128.0");

options.setCapability(
"selenoid:options",
Map.of("enableVNC", true)
);

WebDriver driver = new RemoteWebDriver(remoteUrl, options);

14. Test Configuration

Example:

url=https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
username=Admin
password=admin123

browser=chrome
browserversion=128.0
headless=false
incognito=true

remote=true
huburl=http://localhost:4444/wd/hub


The important configuration is:

remote=true
huburl=http://localhost:4444/wd/hub

15. Browser Version Must Match

There are three important places where the browser version needs to be consistent.

Test Configuration
browserversion=128.0

browsers.json
"default": "128.0"


and:

"128.0": {
"image": "selenoid/vnc:chrome_128.0"
}

Docker Image

The image must exist on the EC2 instance:

docker images selenoid/vnc


Expected:

REPOSITORY     TAG
selenoid/vnc   chrome_128.0


If required:

docker pull selenoid/vnc:chrome_128.0

16. Run Selenium Tests

The complete workflow is:

Step 1 – Verify Selenoid

On EC2:

docker ps


Make sure Selenoid is running.

Step 2 – Verify Selenoid UI

On EC2:

docker ps


Make sure selenoid-ui is running.

Step 3 – Start the SSH Tunnel

On Windows:

ssh -i "C:\path\to\Selenoid-Key.pem" `
  -L 8090:localhost:8090 `
-L 4444:localhost:4444 `
ec2-user@<EC2_PUBLIC_IP>

Step 4 – Verify Selenoid from Windows
curl http://localhost:4444/status

Step 5 – Run the Selenium Tests

Run the Maven/TestNG tests from IntelliJ IDEA or Maven.

The test framework connects to:

http://localhost:4444/wd/hub

Step 6 – Monitor the Browser

Open:

http://localhost:8090


The Selenoid UI displays the active browser session.

17. Verify Chrome Browser Container

When a Selenium test is running, execute on EC2:

docker ps


You should temporarily see:

selenoid
selenoid-ui
selenoid/vnc:chrome_128.0


The Chrome container is dynamically created by Selenoid when the Selenium test requests a Chrome session.

Check the Docker network:

docker network inspect selenoid


The Chrome container should be connected to the selenoid network.

18. Useful Docker Commands
    Check running containers
    docker ps

Check all containers
docker ps -a

Check Selenoid logs
docker logs selenoid

Follow Selenoid logs
docker logs -f selenoid

Check Selenoid UI logs
docker logs selenoid-ui

Stop Selenoid
docker stop selenoid

Remove Selenoid
docker rm selenoid

Stop Selenoid UI
docker stop selenoid-ui

Remove Selenoid UI
docker rm selenoid-ui

Check Docker networks
docker network ls

Inspect Selenoid network
docker network inspect selenoid

19. Restart Selenoid After Configuration Changes

If you modify browsers.json, recreate the Selenoid container.

Stop and remove the existing container:

docker stop selenoid
docker rm selenoid


Start it again:

docker run -d \
--name selenoid \
--restart unless-stopped \
--network selenoid \
-p 4444:4444 \
-v /var/run/docker.sock:/var/run/docker.sock \
-v /home/ec2-user/selenoid/config:/etc/selenoid:ro \
aerokube/selenoid:1.11.3 \
-container-network selenoid

20. Troubleshooting
    Error: sudo apt: command not found

Amazon Linux 2023 does not use apt.

Use:

sudo dnf update -y


and:

sudo dnf install docker -y

Error: Requested environment is not available

Check:

Browser name
Browser version
browsers.json
Docker image
Selenoid logs

Check:

docker logs selenoid

Error: No such image

Example:

No such image: selenoid/vnc:chrome_128.0


Pull the image:

docker pull selenoid/vnc:chrome_128.0


Verify:

docker images selenoid/vnc

Error: wait: http://172.17.x.x:4444/ does not respond

This indicates a Docker networking problem between Selenoid and the browser container.

Make sure Selenoid was started with:

--network selenoid


and:

-container-network selenoid


Check the network:

docker network inspect selenoid


The dynamically-created Chrome container should be connected to the same network.

21. Security Considerations

Selenoid ports 4444 and 8090 should preferably not be exposed directly to the public internet.

The SSH tunnel provides secure access:

Windows
│
│ SSH Tunnel
▼
AWS EC2
│
├── Selenoid :4444
│
└── Selenoid UI :8090


This approach is also useful when the Windows machine is connected through a mobile hotspot and its public IP changes frequently.

An Elastic IP is recommended for the EC2 instance so that the EC2 public IP remains stable.

22. Project Structure

A typical project structure can look like this:

Selenium-Selenoid/
│
├── config/
│   └── browsers.json
│
├── selenoid/
│   └── video/
│
├── reports/
│   └── TestExecutionReport.html
│
├── screenshot/
│   └── *.png
│
├── src/
│   ├── main/
│   └── test/
│
└── pom.xml


The Selenoid configuration on EC2 is:

/home/ec2-user/selenoid/
│
└── config/
└── browsers.json

23. Quick Start
    On AWS EC2

Create the Docker network:

docker network create selenoid


Pull Selenoid:

docker pull aerokube/selenoid:1.11.3


Pull Selenoid UI:

docker pull aerokube/selenoid-ui


Pull Chrome:

docker pull selenoid/vnc:chrome_128.0


Start Selenoid:

docker run -d \
--name selenoid \
--restart unless-stopped \
--network selenoid \
-p 4444:4444 \
-v /var/run/docker.sock:/var/run/docker.sock \
-v /home/ec2-user/selenoid/config:/etc/selenoid:ro \
aerokube/selenoid:1.11.3 \
-container-network selenoid


Start Selenoid UI:
```bash
docker run -d \
--name selenoid-ui \
--restart unless-stopped \
--network selenoid \
-p 8090:8080 \
aerokube/selenoid-ui \
--selenoid-uri http://selenoid:4444
```

Verify:

docker ps

On Windows

## Start the SSH tunnel:

```bash
ssh -i "C:\path\to\Selenoid-Key.pem" `
  -L 8090:localhost:8090 `
-L 4444:localhost:4444 `
ec2-user@<EC2_PUBLIC_IP>
```
### In my case it is 
```bash

ssh -i C:\Users\bharg\Desktop\CV\Ireland-CV\Testing\Spanish-Point\Selenoid-Key.pem -L 8090:localhost:8090 -L 4444:localhost:4444 ec2-user@15.252.181.105
```

Open Selenoid UI:

http://localhost:8090


Selenium Hub URL:

http://localhost:4444/wd/hub


Run the Selenium tests.
Selenium tests connect through the SSH tunnel using:

http://localhost:4444/wd/hub


Selenoid UI is accessed through:

http://localhost:8090


Selenoid dynamically creates the Chrome browser container when a Selenium test requests a matching browser and version.