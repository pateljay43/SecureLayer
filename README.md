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
When the user ui wants to upload a file f, a key kf is generated that is uniquely associated with the file f. Then, kf is used to encrypt f. The key kf is sent to server that acts as secure layer, and the encrypted file is sent to Google Drive. Along with the kf, checksum is also stored on secure server.
- **Downloading:**
User ui downloads the encrypted file Enckf (f) from the Google server and the application gets the corresponding kf from secure server and decrypts it on ui’s mobile device.
- **Sharing:**
Suppose ui wants to share the file f with uj. The application on ui’s mobile device gets uj’s public key kj and uses it to encrypt kf. The encrypted key file is sent to uj’s mobile device together with the link to Enckf (f). Then, uj downloads Enckf (f) and decrypts it using kf, hence has full access to the content of f. File sharing is completed without compromising confidentiality. 

Details of Implementation:
--------------------------
We will use Android Mobile Device as our application platform and mongo dB for storing all of the user’s data, except files, and Java WebSocket for sending/receiving data between device and secure server. Google drive API to store and retrieve files for each user. Application implementation will be done in Java language using Android Studio IDE. We will use Oracle Java Development Kit (JDK) 8. Last is github for source control.


Timeline and Work Distribution:
-------------------------------
> - Creating the UI for the application for enlisting files (9/30/16, Jay and Metehan)
> - Establish a server to manage all devices (10/7/16, Jay and Metehan)
> - Test the correctness of the encryption algorithms to be used in the application (10/14/16, Metehan)
> - Configure server for security issues (10/14/16, Jay)
> - Mongo DB Structures for storing the checksums, keyfiles, user account info (10/28/16, Jay)
> - Uploading files to the Google drive (10/28/16, Metehan)
> - Implement Encryption algorithms (11/4/16, Jay and Metehan)
> - Sign up and Log in procedure (11/4/16, Jay)
> - Downloading files from the Google drive (11/11/16, Metehan)
> - Sharing the files (11/18/16, Jay)
> - Testing and fixing bugs if found (11/25/16, Jay and Metehan) 
