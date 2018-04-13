import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.tika.parser.ner.NERecogniser;
import org.apache.tika.parser.ner.corenlp.CoreNLPNERecogniser;

public class NERRecognizerCustom {

	private static final NERecogniser recogniser = new CoreNLPNERecogniser();

	public static void main(String[] args) throws IOException {

		String csvFile = args[0];
		Reader in = new FileReader(csvFile);

		String csvFile2 = args[1];

		BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFile2));
		CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("event_id", "ner"));

		CSVParser csvParser = new CSVParser(in,
				CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

		for (CSVRecord record : csvParser) {
			String desc = record.get("description");
			String id = record.get("event_id");
			Map<String, Set<String>> map = recogniser.recognise(desc);
			csvPrinter.printRecord(id, map);
		}
		csvPrinter.flush();
		csvPrinter.close();
		csvParser.close();
	}
}