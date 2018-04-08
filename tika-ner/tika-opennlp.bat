@echo off
SET TIKA_APP=target\tika-app-2.0.0-SNAPSHOT.jar
java -classpath %TIKA_APP%;. org.apache.tika.cli.TikaCLI --config=tika-config.xml -m http://people.apache.org/committer-index.html >> ner_open_nlp_output.txt