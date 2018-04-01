@echo off
SET TIKA_APP=target\tika-app-2.0.0-SNAPSHOT.jar
SET CORE_NLP_JAR=target\tika-ner-corenlp-addon-1.0-SNAPSHOT-jar-with-dependencies.jar

java -Dner.impl.class=org.apache.tika.parser.ner.corenlp.CoreNLPNERecogniser -classpath %TIKA_APP%;%CORE_NLP_JAR% org.apache.tika.cli.TikaCLI --config=tika-config.xml -m http://www.hawking.org.uk >> ner_core_nlp_output.txt