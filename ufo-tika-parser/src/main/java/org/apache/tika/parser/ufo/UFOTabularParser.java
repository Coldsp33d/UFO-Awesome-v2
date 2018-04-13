package org.apache.tika.parser.ufo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class UFOTabularParser extends AbstractParser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2205860816041465676L;
	private static final Set<MediaType> SUPPORTED_TYPES = Collections.singleton(MediaType.text("tabularufo"));
	public static final String UFO_MIME_TYPE = "text/tabularufo";

	@Override
	public Set<MediaType> getSupportedTypes(ParseContext arg0) {
		return SUPPORTED_TYPES;
	}

	@Override
	public void parse(InputStream is, ContentHandler handler, Metadata metadata, ParseContext context)
			throws IOException, SAXException, TikaException {
		BufferedReader br1 = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		ArrayList<String> arr1 = new ArrayList<String>();

		UFO ufo = new UFO();

		int page_break = -1;
		int linecount = 0;
		/** Read all the data into an array list */
		for (String x = br1.readLine(); x != null; x = br1.readLine()) {
			arr1.add(x);
			linecount++;
			if (x.contains("-------")) {
				page_break = linecount;
			}
		}

		int date_sighting_line = -1;
		int date_reported_line = -1;

		/** extract date of sighting */
		for (int i = 0; i < page_break; i++) {
			String date_sighted = extractDate(arr1.get(i));
			if (!date_sighted.equals("")) {
				UFODate date = new UFODate();
				String[] dateStrArray = date_sighted.split(" ");
				if (dateStrArray.length > 2) {
					date.setDate(parseDate(dateStrArray[0]));
					date.setMonth(dateStrArray[1]);
					date.setYear(dateStrArray[2]);
					ufo.setSightingDate(date);
					date_sighting_line = i;
					break;
				}
			}
		}

		/** extract date of report */
		for (int i = page_break + 1; i < arr1.size(); i++) {
			String date_reported = extractDate(arr1.get(i));
			if (!date_reported.equals("")) {
				UFODate date = new UFODate();
				String[] dateStrArray = date_reported.split(" ");
				if (dateStrArray.length > 2) {
					date.setDate(parseDate(dateStrArray[0]));
					date.setMonth(dateStrArray[1]);
					date.setYear(dateStrArray[2]);
					ufo.setReportDate(date);
					date_reported_line = i;
					break;
				}
			}
		}

		String jointdata = String.join(" ", arr1);
		/** Duration of sighting */
		ufo.setDuration(extractDuration(jointdata));

		/** Search ahead only if either date is found */
		if (date_sighting_line != -1) {
			/** Description */
			ufo.setDescription(extractDescription(arr1, date_sighting_line));
		}

		UFOParser.parseShape(jointdata, ufo);
		UFOParser.parseLocation(jointdata, ufo);

		if (ufo.getSightingDate() == null || ufo.getReportDate() == null)
			UFOGenericParser.parseUFOSightingAndReportDate(jointdata, ufo);
		
		XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
        xhtml.startDocument();
        xhtml.element("description", ufo.getDescription() != null && !ufo.getDescription().isEmpty() ? ufo.getDescription() : "\"\"");
        xhtml.element("duration", ufo.getDuration() != null && !ufo.getDuration().isEmpty() ? ufo.getDuration() : "\"\"");
        xhtml.element("location", ufo.getLocation() != null && !ufo.getLocation().isEmpty() ? ufo.getLocation() : "\"\"");
        xhtml.element("report-date", ufo.getReportDate() != null && !ufo.getReportDate().toString().isEmpty() ? ufo.getReportDate().toString() :  "\"\"");
        xhtml.element("sighting-date", ufo.getSightingDate() != null && !ufo.getSightingDate().toString().isEmpty() ? ufo.getSightingDate().toString() : "\"\"");
        xhtml.element("shape", ufo.getShape() != null && !ufo.getShape().isEmpty() ? ufo.getShape() : "\"\"");
        xhtml.endDocument();
	}

	public static String extractDate(String data) {
		Pattern monthYearPattern = Pattern.compile(
				"(\\b(\\d{1,6}.?) (?:JAN(?:UARY)?|FEB(?:RUARY)?|MAR(?:CH)?|APR(?:IL)?|MAY|JUN(?:E)?|JUL(?:Y)?|AUG(?:UST)?|SEP(?:TEMBER)?|OCT(?:OBER)?|(NOV|DEC)(?:EMBER)?)) (\\d{2}|\\d{4})? ");
		Matcher monthyear = monthYearPattern.matcher(data);
		if (monthyear.find()) {
			String globMonthYear = monthyear.group();
			return globMonthYear;
		}
		return "";
	}

	public static String extractDescription(ArrayList<String> data, int index) {
		int count1 = 0;
		int i1 = index + 1;
		String ans = "";
		while (i1 < data.size() && count1 < 3) {
			String currData = data.get(i1);
			if (extractDuration(currData).isEmpty()) {
				if (!currData.isEmpty() && !currData.trim().equalsIgnoreCase("Description of object")
						&& !currData.trim().contains("FIGHTING")) {
					ans += data.get(i1);
					count1++;
				}
			}
			i1++;
		}
		return ans;
	}

	public static String extractDuration(String data) {
		Pattern duration = Pattern
				.compile("(\\b(\\d{1,2}) (?:MIN(?:S)?|MIN(?:UTES)?|MIND|HOUR(?:S|SEC(?:S)|SECOND(?:S)|DAY(?:S))))");
		Matcher durationMatch = duration.matcher(data);
		if (durationMatch.find()) {
			return durationMatch.group();
		}
		return "";
	}

	private static String parseDate(String dateTimePortion) {
		String date = "";
		if (dateTimePortion.length() > 2) {
			Integer dateOption1 = Integer.parseInt(dateTimePortion.charAt(0) + "");
			Integer dayOption2 = Integer.parseInt(dateTimePortion.charAt(0) + "" + dateTimePortion.charAt(1) + "");
			if (dayOption2 > 30)
				date = dateOption1 + "";
			else
				date = dayOption2 + "";
		} else {
			date = dateTimePortion.charAt(0) + "";
		}
		return date;
	}
}
