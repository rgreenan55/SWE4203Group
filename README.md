# Client-Server Tic-Tac-Toe
The 2020-2021 SWE4203 lab project!

## Compilation
You will need to [install Java](https://www3.ntu.edu.sg/home/ehchua/programming/howto/JDK_Howto.html) before you can compile/run this application. Once installed, just run `javac`.

```
javac src/*.java
```

## Running
Just run the `Main.java` file!
```
cd src
java Main # optionally pass in the port e.g. java Main 5000
```

This will start a server at [`localhost:3000`](http://localhost:3000) if you used the default port. To play against yourself, you will need to open two instances of the application. In the first instance, click `"Host Game"` and then copy the access code that appeared. In the second window, paste the access code in the `Access Code` field and then click `"Find Game"`. After, you will be able to take turns placing your markers. 

> Remember, the host always goes first!

## Development
If you are on a Unix based system, you can use the following command to start a hot reload server.
```
# -r because the child process is persistent and -s because we are passing in a shell command
ls src/**/*.java src/**/*.html src/**/*.js src/**/*.css | entr -rs 'make && make serve'
```

Note that you will likely have to install [entr](http://eradman.com/entrproject/) before you can run the following command. It can be easily downloaded and installed using the link above or installed using your system's package manager. The following subsections show the commands for a few operating systems.

### Ubuntu
```
sudo apt-get update -y
sudo apt-get install -y entr
```

### Mac OS
```
brew install entr
```

## Swagger
This project defines a `swagger.yml` file which can be converted into a website for visualization.

To view this information, open up `index.html` in the browser of your choice. Alternatively, go to the [Swagger Editor](https://editor.swagger.io/), copy the contents of `swagger.yml` and paste the contents in the online editor.

<!-- #### Generation
First, ensure `redoc-cli` is installed.
```
npm install -g redoc-cli
```

Next, run:
```
redoc-cli bundle -o index.html swagger.yml
``` -->
