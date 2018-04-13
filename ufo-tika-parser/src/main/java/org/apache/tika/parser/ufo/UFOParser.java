package org.apache.tika.parser.ufo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ner.NERecogniser;
import org.apache.tika.parser.ner.corenlp.CoreNLPNERecogniser;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class UFOParser extends AbstractParser {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3737259883907039930L;
	
	private static final Set<MediaType> SUPPORTED_TYPES = Collections
			.singleton(MediaType.text("listufo"));
	public static final String UFO_MIME_TYPE = "text/listufo";

	private static final Pattern datePattern = Pattern.compile("(\\b\\d{5,})");
	private static final Pattern monthYearPattern = Pattern.compile(
			"(\\b(?:JAN(?:UARY)?|FEB(?:RUARY)?|MAR(?:CH)?|APR(?:IL)?|MAY|JUN(?:E)?|JUL(?:Y)?|AUG(?:UST)?|SEP(?:TEMBER)?|OCT(?:OBER)?|(NOV|DEC)(?:EMBER)?)) (\\d{2}|\\d{4})? ");
	private static final Pattern dayMonthYearPattern = Pattern.compile(
			"(\\b\\d{1,2}\\D{0,3})?(?:JAN(?:UARY)?|FEB(?:RUARY)?|MAR(?:CH)?|APR(?:IL)?|MAY|JUN(?:E)?|JUL(?:Y)?|AUG(?:UST)?|SEP(?:TEMBER)?|OCT(?:OBER)?|(NOV|DEC)(?:EMBER)?)(\\D{0,3})?(\\d{2}|\\d{4})?");
	private static final Pattern dateTimeDurationSightingPattern = Pattern.compile("(?m)(^A(\\.)? \\d+.+)");
	private static final Pattern dateOfReportPattern = Pattern.compile("(?m)(^P(\\.)? .+)");
	private static final Pattern descriptionSightingPattern = Pattern.compile("(?m)(^B(\\.)?) .+");
	public static final Pattern durationSightingPattern = Pattern.compile("(\\b(\\d{1,2}) (?:MIN(?:S)?|MIN(?:UTES)?|MIND|HOUR(?:S|SEC(?:S)|SECOND(?:S)|DAY(?:S))))");
	private static final String[] shapes = {"BLIMP","BOOMERANG","BULLET","CHEVRON","CIGAR","CIRCLE","CONE","CROSS","CYLINDER","DIAMOND","DISC","DUMBBELL","EGG","FIREBALL","FLASH","MISSILE","OTHER","OVAL","RECTANGULAR","SATURN-LIKE","SPHERE","SQUARE","STAR-LIKE","TEARDROP","TRIANGLE","UNKNOWN","ROUND","BRIGHT","WHITISH","SILVER","LIGHT","RED", "V SHAPED", "GREEN", "RED", "WHITE", "STAR", "HELICOPTER", "REEPPISH", "PINK", "ARC","BUCKET","ORANGE"};
	private static final Set<String> shapesSet = new HashSet<>(Arrays.asList(shapes));
	private static final NERecogniser recogniser = new CoreNLPNERecogniser();

	@Override
	public Set<MediaType> getSupportedTypes(ParseContext context) {
		return SUPPORTED_TYPES;
	}

	@Override
	public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
			throws IOException, SAXException, TikaException {
		String data = IOUtils.toString(stream, "UTF-8");
		metadata.set(Metadata.CONTENT_TYPE, UFO_MIME_TYPE);
		String[] globalDate = new String[2];
		Matcher monthyear = monthYearPattern.matcher(data);
		if (monthyear.find()) {
			String globMonthYear = monthyear.group();
			globalDate = parseMonthYearFromDocument(globMonthYear);
		}
		UFO ufo = new UFO();
		parseDateTimeDurationOfSighting(data, ufo, globalDate);
		parseDateOfReport(data, ufo, globalDate);
		parseDescription(data, ufo);
		parseLocation(data, ufo);
		parseShape(data, ufo);
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
	
	public static void parseShape(String data, UFO ufo) {
		Set<String> dataSet = new HashSet<>(Arrays.asList(data.split(" ")));
		dataSet.retainAll(shapesSet);
		ufo.setShape(StringUtils.join(dataSet, ','));
	}
	
	public static void parseLocation(String data, UFO ufo) {
		Map<String, Set<String>> keys = recogniser.recognise(data);
		Set<String> locations = keys.get("LOCATION");
		if (locations != null && locations.size() > 0)
			ufo.setLocation(StringUtils.join(locations, ','));
	}

	private static void parseDescription(String data, UFO ufo) {
		Matcher description = descriptionSightingPattern.matcher(data);
		if (description.find()) {
			ufo.setDescription(description.group().split(" ", 2)[1]);
		}
	}

	private static void parseDateTimeDurationOfSighting(String data, UFO ufo, String[] globalDate) {
		String dateTimeDurationLine = "";
		Matcher dateTimeDurationMatcher = dateTimeDurationSightingPattern.matcher(data);
		if (dateTimeDurationMatcher.find()) {
			dateTimeDurationLine = dateTimeDurationMatcher.group();
			Matcher durationSightingPatternMatcher = durationSightingPattern.matcher(dateTimeDurationLine);
			if (durationSightingPatternMatcher.find()) {
				String durationData = durationSightingPatternMatcher.group();
				//ufo.setDuration(durationData.split(" ")[0]);
				ufo.setDuration(durationData);
			}
		}
		UFODate date = parseDate(dateTimeDurationLine, globalDate);
		ufo.setSightingDate(date);
	}

	private static void parseDateOfReport(String data, UFO ufo, String[] globalDate) {
		String dateOfReportLine = "";
		Matcher dateOfReportMatcher = dateOfReportPattern.matcher(data);
		if (dateOfReportMatcher.find()) {
			dateOfReportLine = dateOfReportMatcher.group();
		}
		UFODate date = parseDate(dateOfReportLine, globalDate);
		ufo.setReportDate(date);
	}

	private static UFODate parseDate(String inputLine, String[] globalDate) {
		UFODate sightingDate = new UFODate();
		sightingDate.setMonth(globalDate[0] == null ? null : globalDate[0]);
		sightingDate.setYear(globalDate[1] == null ? null : globalDate[1]);
		//System.out.println(inputLine);
		if (inputLine != null && !inputLine.isEmpty()) {
			Matcher datePatternMatcher = datePattern.matcher(inputLine);
			if (datePatternMatcher.find()) {
				sightingDate.setDate(parseDate(datePatternMatcher.group()));
			}

			Matcher sightingDayMonthYearMatcher = dayMonthYearPattern.matcher(inputLine);
			if (sightingDayMonthYearMatcher.find()) {
				String sightingDayMonthYear = sightingDayMonthYearMatcher.group();
				String[] sightingDayMonthYearArray = sightingDayMonthYear.split(" ");
				if (sightingDayMonthYearArray.length > 2) {
					sightingDate.setDate(sightingDayMonthYearArray[0].trim());
					sightingDate.setMonth(sightingDayMonthYearArray[1].trim());
					sightingDate.setYear(sightingDayMonthYearArray[2].trim());
				}
			}
		}
		return sightingDate;
	}

	private static String[] parseMonthYearFromDocument(String globMonthYear) {
		String[] globalMonthYear = new String[2];
		String globMonthYearDataYear[] = globMonthYear.split(" ");
		if (globMonthYearDataYear.length > 1) {
			globalMonthYear[0] = globMonthYearDataYear[0];
			globalMonthYear[1] = globMonthYearDataYear[1];
		}
		globalMonthYear[0] = globMonthYearDataYear[0];
		return globalMonthYear;
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