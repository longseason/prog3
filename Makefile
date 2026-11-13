JFLAGS=-g

all: Parse/Grm.java Parse/Yylex.java
	javac ${JFLAGS} ErrorMsg/*.java Symbol/*.java Absyn/*.java Parse/*.java Temp/*.java Util/*.java Frame/*.java Mips/*.java Translate/*.java Types/*.java FindEscape/*.java Semant/*.java

# CUP builds Grm.java and sym.java. -expect 1 = the dangling-else S/R conflict.
# PackTables then shrinks the parse tables so javac does not fail with
# "code too large" (see README).
Parse/Grm.java: Parse/Grm.cup Tools/PackTables.class
	cd Parse; java java_cup.Main -parser Grm -expect 1 -compact_red -dump_grammar -dump_states <Grm.cup >Grm.out 2>Grm.err; s=$$?; grep -i "^error" Grm.err; tail -n 11 Grm.err; exit $$s
	java -cp Tools PackTables Parse/Grm.java

Tools/PackTables.class: Tools/PackTables.java
	javac Tools/PackTables.java

Parse/Yylex.java: Parse/C.lex
	cd Parse; java JLex.Main C.lex; mv C.lex.java Yylex.java

clean:
	rm -f */*.class Parse/Grm.java Parse/sym.java Parse/Yylex.java Parse/Grm.err Parse/Grm.out