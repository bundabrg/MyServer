# Paper v1.0
Provides a Vanilla Minecraft server, patched to accept IP-Forwarding with [VanillaCord](https://github.com/ME1312/VanillaCord).


## Template Summary

* The template file should be called `template.yml` and exist somewhere under MyServer's `templates` folder.
* The name field should start with `server/vanilla/1.0`. 
* If a `files` folder exists in the same folder as `template.yml` it will be copied to the newly created
server location. 
* Anything listed as a dynamic template file will be copied each time the server starts and additionally be parsed for 
tags and replaced with the tag value. 
* Anything listed as a static template file will only be copied and parsed when the server is created

## Built-in Tags
| Tag       | Description     
| :------------- | :----------  
| `DATE_GENERATED` | The current date and time in format `yyyy/mm/dd hh:mm:ss`  
| `MC_SERVER_IP` | The allocated IP address of the server 
| `MC_SERVER_PORT` | The allocated port of the server 


## Structure

```yaml
# Template Name
name: server/vanilla/1.0/...

# Description of Template
description: My description

# What other templates does this template inherit from
parents:
  - template/name/of/parent

# Server Settings
server:
  start:
    # How to execute the server
    execute: java ...
    
    # What commands to send to server after execution as the console
    commands:
      - cmd1
      - cmd2
          
    # How long in seconds to delay after startup till sending commands
    delay: 5
  stop:
    # Commands to send to server to stop
    commands:
      - stop
    
    # How long in seconds to wait for shutdown otherwise the server is murdered
    wait: 60 

# Vanilla Settings
vanilla:
  # Version of the Vanilla Server Desired
  version: 1.16.3

# Tag definitions - Define all tags here
tags:
  TAG_NAME:
    # Short description of this tag
    description: Description for Tag
    
    # If true then the tag must have a value set else the server won't start
    required: true
    
    # Default value of tag if not set
    default:

    # Type of data. Can be string, int, boolean, choice
    type: choice

    # Permission to set tag. Can be admin, owner
    permission: owner

    # If type choice then the list of choices
    choices:
      - choice1
      - choice2

# Which files to treat as templates relative to the `files` folder
templates:
  # Static Template files are parsed only when the server is initially created
  static: [ ]

  # Dynamic Template files are parsed each time the server is started
  dynamic:
    - eula.txt
    - server.properties

# Triggers are performed when text is received that match a trigger. The command is sent to the console of the server. Tags can
# be used as well
triggers:
  notify_join:
    match: 'UUID of player (\w+)'
    commands:
      - 'tellraw $1 ["",{"text":"Welcome to "},{"text":"{{MC_SERVER_NAME}}","color":"red"}]'
```

