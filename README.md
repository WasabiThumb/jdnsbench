# jDNSBench
A fast, extensive & transparent DNS benchmark that runs in your terminal!
Made in Java & C for Windows & Linux x64.

![Image of Benchmark](https://raw.githubusercontent.com/WasabiThumb/jdnsbench/refs/heads/master/doc/4.png)

## Requirements
- **Java 17**
- **C-ARES** (Linux only)
  - This is a common library. It is required by NodeJS, so you can install that if you are having trouble installing C-ARES specifically.

## Installing
### Download the JAR
Fetch a build from the Releases tab or CI, and follow [Usage](#usage). You may create
a shortcut the JAR file for ease of use. An example XDG [Desktop Entry](https://wiki.archlinux.org/title/Desktop_entries):
```text
[Desktop Entry]
Name=jDNSBench
Exec=/usr/bin/java -jar /usr/share/jdnsbench/jdnsbench-app-VERSION.jar
Terminal=true
Type=Application
```

### From the AUR (Arch Linux)
Install either the ``jdnsbench`` or ``jdnsbench-bin`` packages from the [AUR](https://aur.archlinux.org/), for example
``yay jdnsbench``. The ``-bin`` package, as per convention, installs a pre-built JAR from GitHub. The base package
downloads the source code from this repository, and builds the application using Gradle.

## Usage
### From Console
```shell
java -jar jdnsbench-app-VERSION.jar
```

### From Desktop
This is not a supported method of launching the application, but using the system
opener (e.g. double click) on the JAR file will attempt to spawn a new terminal running the
application.

## Roadmap
- Better error handling for native errors
- Improve look
- Fix IPv6 on Windows


## Gallery
![Image of Welcomer](https://raw.githubusercontent.com/WasabiThumb/jdnsbench/refs/heads/master/doc/1.png)
![Image of Configuration](https://raw.githubusercontent.com/WasabiThumb/jdnsbench/refs/heads/master/doc/2.png)
![Image of Configuration](https://raw.githubusercontent.com/WasabiThumb/jdnsbench/refs/heads/master/doc/3.png)