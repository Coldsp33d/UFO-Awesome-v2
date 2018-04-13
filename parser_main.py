import os
import glob
import parser

cmd = 'java -classpath ' \
	  '"tika-ner-corenlp-addon-1.0-SNAPSHOT-jar-with-dependencies.jar":"tika-app-2.0.0-SNAPSHOT.jar" ' \
	  '"org.apache.tika.cli.TikaCLI" '\
	  '"{}" '\
	  '"{}" '


# part 1 - run spell check preprocessor
parser.main()

# part 2 - run tika parser on cleaned files
for base_path in glob.glob('Data/Resources/ocr-output/DEFE-*'):
	os.system(cmd.format(os.path.join(base_path, 'outtxt-clean'), os.path.join(base_path, 'outtxt-clean-tika')))
