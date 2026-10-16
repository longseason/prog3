JFLAGS=-g

Parse/*.class: Parse/*.java Parse/Yylex.java
	javac ${JFLAGS} Parse/*.java

Parse/Yylex.java: Parse/C.lex
	cd Parse; java JLex.Main C.lex; mv C.lex.java Yylex.java

clean:
	rm -f */*.class Parse/Yylex.java
