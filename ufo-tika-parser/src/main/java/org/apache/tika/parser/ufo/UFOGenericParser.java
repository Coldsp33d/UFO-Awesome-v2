package org.apache.tika.parser.ufo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class UFOGenericParser extends AbstractParser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1679664775528811076L;
	private static final Pattern dateMonthYearPattern = Pattern.compile(
			"(\\b(\\d{1,2})? (?:JAN(?:UARY)?|FEB(?:RUARY)?|MAR(?:CH)?|APR(?:IL)?|MAY|JUN(?:E)?|JUL(?:Y)?|AUG(?:UST)?|SEP(?:TEMBER)?|OCT(?:OBER)?|(NOV|DEC)(?:EMBER)?)) (\\d{2}|\\d{4}) ");
	private static final Set<MediaType> SUPPORTED_TYPES = Collections
			.singleton(MediaType.text("genericufo"));
	public static final String UFO_MIME_TYPE = "text/genericufo";
	
	@Override
	public Set<MediaType> getSupportedTypes(ParseContext arg0) {
		return SUPPORTED_TYPES;
	}

	@Override
	public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
			throws IOException, SAXException, TikaException {
		String data = IOUtils.toString(stream, "UTF-8");
		//metadata.set(Metadata.CONTENT_TYPE, UFO_MIME_TYPE);
		
		UFO ufo = new UFO();
		parseUFOSightingAndReportDate(data, ufo);
		Matcher durationSightingPatternMatcher = UFOParser.durationSightingPattern.matcher(data);
		if (durationSightingPatternMatcher.find()) {
			String durationData = durationSightingPatternMatcher.group();
			ufo.setDuration(durationData);
		}
		//String[] decriptionArray = data.split(" ");
		//ufo.setDescription(String.join(" ", Arrays.asList(decriptionArray).subList(0, Math.min(decriptionArray.length, 50))));
		ufo.setDescription(data);
		UFOParser.parseLocation(data, ufo);
		UFOParser.parseShape(data, ufo);
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

	public static void parseUFOSightingAndReportDate(String data, UFO ufo) {
		UFODate date = new UFODate();
		
		Matcher dateMonthYearPatternMatcher = dateMonthYearPattern.matcher(data);
		if (dateMonthYearPatternMatcher.find()) {
			String[] dateMonthYearArray = dateMonthYearPatternMatcher.group().split(" ");
			if(ufo.getSightingDate() == null)
				populateDate(date, dateMonthYearArray);
		}
		ufo.setSightingDate(date);
		if (dateMonthYearPatternMatcher.find()) {
			date = new UFODate();
			String[] dateMonthYearArray = dateMonthYearPatternMatcher.group().split(" ");
			populateDate(date, dateMonthYearArray);
		}
		if(ufo.getReportDate() == null)
			ufo.setReportDate(date);
	}

	private static void populateDate(UFODate date, String[] dateMonthYearArray) {
		if(dateMonthYearArray.length > 2) {
			date.setDate(dateMonthYearArray[0]);
			date.setMonth(dateMonthYearArray[1]);
			date.setYear(dateMonthYearArray[2]);
		}
		else {
			date.setMonth(dateMonthYearArray[0]);
			date.setYear(dateMonthYearArray[1]);
		}
	}

}
