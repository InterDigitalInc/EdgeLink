# Orchestrator

## Goal
- Guidance on installing and configuring the orchestrator

## Pre-requisites
- [Setup runtime environment(Ubuntu/Java)](../setenv.md)

## HTTP Server
An HTTP server is required to host the orchestrator web pages. The next few steps shows how to install a tomcat version 8 http server on the nerwork controller machine

HTTP server version we use:
- Tomcat v8.5.39

How we do it:

Run a script to install the tomcat server and the orchestrator.
```
./scripts/setup_tomcatv8.sh
```

The script downloads the tomcat tar file, install and configure it to use the port **8888**.

## Orchestrator
Run a script to install the orchestrator.
```
./scripts/setup_orchestrator.sh
```

The script copies the orchestrator where it can be hosted by the tomcat server.
