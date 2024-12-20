JAVAC = javac
SRC = src/TankYouNext
LIB = libs/robocode.jar
TARGET_DIR = C:\robocode\robots\TankYouNext

all: compile copy

compile:
	$(JAVAC) -cp $(LIB) $(SRC)/MissleToe.java

copy:
	mkdir -p $(TARGET_DIR)
	cp $(SRC)/MissleToe.class $(TARGET_DIR)

clean:
	Remove-Item -Recurse -Force $(SRC)/*.class, $(TARGET_DIR)

.PHONY: all compile copy clean