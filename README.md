# SecureLayer

**Group Name: Cloud Inspector**

**Project Members:**
*Metehan Ozsoy*
*Jay P. Patel*

Motivation:
-----------
Storing files and sharing them securely on cloud storage has been a trending topic in the field of information security. There have been solutions provided by various applications that store encrypted user files together with the keys to decrypt them. As the cloud server may be an adversary, the files will be vulnerable since using the keys stored on the same cloud server could decrypt them. This violates the confidentiality. Our motivation in this work is to increase the confidentiality by preventing the server from accessing the decrypted files.

Problem Statement:
------------------
Suppose we have a cloud that allows its users to store and share files among other users. How can we assure that unauthorized users and the cloud server itself do not have access to the content of the files?	

Description of the Proposed Solution:
-------------------------------------
We design an application for Android-based mobile devices that allows its users to store and share files with the other users as they would use current storage technologies like drop box or Google drive. But Our Application will act as security layer between those cloud storage and user. This layer will make sure no one except the user who upload file would be able to access the original content of file. We reach the level of confidentiality mentioned in the problem statement as follows:
Let u1, u2, ..., un be the users of the application.

- **Sign Up & Log In:**
Each user ui creates a password pi at the time of sign up where pi is hashed by a hash function h(n). Also, ui has a public key kpubi and a private key kprii. We then store ui, h(pi) and kpubi on the cloud server, and kprii on ui’s mobile device. When the user ui tries to log in, server checks if the entered username (u) and password (p) satisfies both u = ui and h(p) = h(pi). 
- **Uploading:**
When the user ui wants to upload a file f, a key kf is generated that is uniquely associated with the file f. Then, kf is used to encrypt f. The Encrypted key Enc(kf) is sent to server that acts as secure layer, and the encrypted file is sent to Google Drive. Along with the Enc(kf), checksum is also stored on secure server.
- **Downloading:**
User ui downloads the encrypted file Enckf(f) from the Google server and the application gets the corresponding Enc(kf) from secure server and decrypts it on ui’s mobile device.
- **Sharing:**
Suppose ui wants to share the file f with uj. On ui's mobile, the application generates (g^i mod P) where g and P are public values and i is secret value, sends public values with (g^i mod P) to uj. Now uj's mobile device receives g and P, generates and sends (g^j mod P); where j is secret value of uj. Now both have the same shared private key on the network using Diffie-Hellman Protocol. Which can be used to send Kf for shared file.

Details of Implementation:
--------------------------
We will use Android Mobile Device as our application platform and MySQL database for storing all of the user’s data, except files. We are using Web Socket Secure between Android application and SecureServer communication. Google drive API to store and retrieve files for each user. Mobile application implementation will be done in Java language using Android Studio IDE. We will use Oracle Java Development Kit (JDK) 8. Apache Tomcat 8 as our SecureServer with OpenSSL using Apache Portable Runtime API. Web socket on server will be implemented in Java language which will act as server's end point for mobile application.


Timeline and Work Distribution:
-------------------------------
> - Creating the UI for the application for enlisting files (9/30/15, Jay and Metehan)
> - Establish a server to manage all devices (10/7/15, Jay and Metehan)
> - Test the correctness of the encryption algorithms to be used in the application (10/14/15, Metehan)
> - Configure server for security issues (10/14/15, Jay)
> - MySQL Structures for storing the checksums, keyfiles, user account info, access control list (10/28/15, Jay)
> - Log in procedure (10/28/15, Jay)
> - Implement Secure Web Socket connection (11/4/15)
> - Implement Encryption algorithms (11/4/15, Jay and Metehan)
> - Uploading files to the Google drive (10/28/15, Metehan)
> - Downloading files from the Google drive (11/11/15, Metehan)
> - Sharing the files (11/18/15, Jay)
> - Testing and fixing bugs if found (11/25/15, Jay and Metehan) 
