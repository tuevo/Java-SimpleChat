# Chat for Fun 💬

## 1. About
- <b>Java application</b>
- <b>Version</b>: 1.0.0
- <b>Author</b>: Vo Thai Minh Tue - 19424008 - 19HCB, HCMUS.

## 2. Requirements
- <b>Java OpenJDK 14.0.1 </b>
- <b>Java Ant</b>

## 3. Quick start
### <b>Step 1</b>: Run Chat Server
- If you are using <b>Windows</b>, run the file <b>chat-server.jar</b> to open the Chat Server UI.
- If you are using <b>Linux</b>, run this below command from <b>root</b> folder to open the Chat Server UI:
  
    ```
    java -jar chat-server.jar
    ```

- Click the button <b>START</b> to run the Chat Server

    ## ![chat-server-start](resource/images/chat-server-start-screen.png)

  
### <b>Step 2</b>: Run Chat Widget
- If you are using <b>Windows</b>, run the file <b>chat-widget.jar</b> to open the Chat Widget UI.
- If you are using <b>Linux</b>, open another terminal and run this below command from <b>root</b> folder to open the Chat Widget UI:
  
    ```
    java -jar chat-widget.jar
    ```

- Chat Widget welcome screen
    ## ![chat-widget-welcome](resource/images/chat-widget-welcome-screen.png)

- If you have no an account, click the button <b>Create an Account</b> to register a chatting account.
    ## ![chat-widget-register](resource/images/chat-widget-register-screen.png)

- After registration, start chatting right now!
    ## ![chat-widget-chatting](resource/images/chat-widget-chatting-screen.png)

## 4. End-user distribution
- Run this below command from <b>root</b> folder to build project:

  ```
  ant
  ```
- A folder named <b>dist</b> will be created at <b>root</b> folder and available to distribute to end-user.
  ```
  dist
  |__ db
    |__ members.txt
  |
  |__ library
  |__ resource
      |__ avatars
      |__ icons
      |__ images
  |
  |__ upload
  |__ chat-server.jar
  |__ chat-widget.jar
  ```

## 5. File Structure

```
19424008
|__ src
    |__ main
    |__ classes
|
|__ bin
    |__ main
    |__ classes
|
|__ db
    |__ members.txt
|
|__ library
|__ resource
    |__ avatars
    |__ icons
    |__ images
|
|__ upload
|__ build.xml
|__ README.md

```