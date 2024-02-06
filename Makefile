build:
	javac src/*.java

serve:
	cd src && java Main ${ARGS}

clean:
	rm src/*.class
