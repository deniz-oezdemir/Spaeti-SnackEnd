# Production Deployment Guide: Custom Domain & HTTPS

This guide outlines the steps to deploy the application to an AWS EC2 instance and configure a custom domain with HTTPS.

The chosen architecture uses an **AWS Application Load Balancer (ALB)** to manage incoming traffic and handle SSL/TLS termination. This is a highly scalable and secure pattern for production workloads, separating the public-facing web traffic from the application server itself.

## Prerequisites

Before starting, ensure you have the following:

- A registered domain name (e.g., `deniz.fyi` managed at Namecheap).
- An AWS account with an IAM user that has the necessary permissions.
- The Spring Boot application deployed and running on an EC2 instance, listening on its application port (e.g., `8080`).

## Step-by-Step Configuration

The process involves five main stages: obtaining a certificate, defining where traffic should go, creating the load balancer, pointing the domain to it, and finally, securing the application server.

### 1\. Obtain an SSL/TLS Certificate

We use AWS Certificate Manager (ACM) to get a free, auto-renewing public SSL certificate.

1.  Navigate to **AWS Certificate Manager (ACM)** in the same region as your EC2 instance.
2.  **Request a public certificate** for your desired subdomain (e.g., `shop.deniz.fyi`).
3.  Choose **DNS validation**. ACM will provide a CNAME record.
4.  Log in to **Namecheap**, navigate to your domain's "Advanced DNS" settings, and create the CNAME record provided by ACM. This validates that you own the domain.
5.  Wait for the certificate status in the ACM console to change from "Pending" to **"Issued"**.

### 2\. Create a Target Group

The Target Group tells the load balancer which instance(s) to send traffic to.

1.  In the EC2 console, navigate to **Target Groups**.
2.  Create a new target group with the following settings:
    - **Target type:** `Instances`
    - **Protocol:** `HTTP`
    - **Port:** `8080` (your Spring Boot application's port)
    - **VPC:** Select the VPC containing your EC2 instance.
    - **Health checks:** Set the path to `/actuator/health` for accurate health monitoring.
3.  In the "Register targets" step, **select your EC2 instance** and include it. This is a critical step to ensure the load balancer knows your instance is available.

### 3\. Create the Application Load Balancer (ALB)

The ALB is the public entry point to your application.

1.  In the EC2 console, navigate to **Load Balancers** and create a new **Application Load Balancer**.
2.  **Configuration:**
    - **Scheme:** `Internet-facing`.
    - **Subnets:** Select at least two subnets in different Availability Zones for high availability.
    - **Security Group:** Assign a security group to the ALB (e.g., `webapp-alb-sg`) that allows inbound traffic from **Anywhere (0.0.0.0/0)** on ports **80 (HTTP)** and **443 (HTTPS)**.
    - **Listeners:**
        - **HTTPS / Port 443:** Configure this listener to **forward** traffic to the target group created in Step 2. Attach your issued ACM certificate here.
        - **HTTP / Port 80:** (Optional but recommended) Configure this listener to issue a **permanent redirect (301)** to the corresponding HTTPS URL.

### 4\. Point Your Subdomain to the Load Balancer

This step makes your custom domain live.

1.  Copy the **DNS name** of your newly created ALB from its details page.
2.  Go back to your **Namecheap "Advanced DNS"** settings.
3.  Create a new **CNAME record**:
    - **Host:** `shop`
    - **Value:** Paste the DNS name of your ALB.
    - **TTL:** `Automatic`

### 5\. Secure the EC2 Instance

This is the final and most important security step. Your EC2 instance should only accept traffic from the load balancer, not from the public internet.

1.  Go to the **Security Group** associated with your **EC2 instance**.
2.  Edit its **inbound rules**.
3.  Ensure the rule for your application port (`8080`) has its **Source** set to the **security group of your ALB** (e.g., `webapp-alb-sg`).
4.  Remove any other public-facing rules (like port 80, 443, or 8080 from `0.0.0.0/0`) from the EC2 instance's security group.

## Final Architecture

The final, secure request flow is as follows:

```
User Browser
      |
      | HTTPS Request to https://shop.deniz.fyi
      |
      v
[ DNS (Namecheap) ]
(CNAME record points 'shop' to the ALB)
      |
      v
[ Application Load Balancer ]
- Listens on Port 443 (HTTPS)
- Terminates SSL using ACM certificate
- Security Group: Allows traffic from Anywhere (0.0.0.0/0) on ports 80/443
      |
      | HTTP Request on Port 8080
      v
[ EC2 Instance ]
- Spring Boot App runs on Port 8080
- Security Group: Allows traffic ONLY from the ALB's Security Group on port 8080
```
