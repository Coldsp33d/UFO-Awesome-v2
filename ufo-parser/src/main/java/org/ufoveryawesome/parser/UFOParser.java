package org.ufoveryawesome.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.drew.lang.annotations.NotNull;

public class UFOParser extends AbstractParser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Set<MediaType> SUPPORTED_TYPES = Collections.singleton(MediaType.application("ufo"));
	public static final String HELLO_MIME_TYPE = "application/ufo";

	private static final Pattern datePattern = Pattern.compile("(\\b\\d{5,})");
	private static final Pattern monthYearPattern = Pattern.compile(
			"(\\b(?:JAN(?:UARY)?|FEB(?:RUARY)?|MAR(?:CH)?|APR(?:IL)?|MAY|JUN(?:E)?|JUL(?:Y)?|AUG(?:UST)?|SEP(?:TEMBER)?|OCT(?:OBER)?|(NOV|DEC)(?:EMBER)?)) (\\d{2}|\\d{4})? ");
	private static final Pattern dayMonthYearPattern = Pattern.compile(
			"(\\b\\d{1,2}\\D{0,3})?(?:JAN(?:UARY)?|FEB(?:RUARY)?|MAR(?:CH)?|APR(?:IL)?|MAY|JUN(?:E)?|JUL(?:Y)?|AUG(?:UST)?|SEP(?:TEMBER)?|OCT(?:OBER)?|(NOV|DEC)(?:EMBER)?)(\\D{0,3})?(\\d{2}|\\d{4})?");
	private static final Pattern dateTimeDurationSightingPattern = Pattern.compile("(?m)(^A(\\.)? \\d+.+)");
	private static final Pattern dateOfReportPattern = Pattern.compile("(?m)(^P(\\.)? .+)");
	private static final Pattern descriptionSightingPattern = Pattern.compile("(?m)(^B(\\.)?) .+");
	private static final Pattern durationSightingPattern = Pattern.compile("(\\b(\\d{1,2}) (?:MIN(?:S)?|MIN(?:UTES)?|MIND|HOUR(?:S|SEC(?:S)|SECOND(?:S)|DAY(?:S))))");

	public Set<MediaType> getSupportedTypes(ParseContext arg0) {
		return SUPPORTED_TYPES;
	}

	public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
			throws IOException, SAXException, TikaException {
		/*
		 * AutoDetectParser parser = new AutoDetectParser();
		 * parser.parse(stream, handler, metadata); String data =
		 * handler.toString();
		 */
		/*
		 * XHTMLContentHandler xhtml = new XHTMLContentHandler(handler,
		 * metadata); xhtml.startDocument(); xhtml.endDocument();
		 */
	}

	public static List<File> getFiles(@NotNull String idn, @NotNull boolean recursive) {
		File dirI = new File(idn);
		List<File> files = new ArrayList<>();
		if (dirI.exists() && dirI.isDirectory()) {
			Iterator<File> iterator = FileUtils.iterateFiles(dirI, new String[] { "txt" }, recursive);
			while (iterator.hasNext()) {
				files.add(iterator.next());
			}
			Collections.sort(files, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					int n1 = extractNumber(o1.getName());
					int n2 = extractNumber(o2.getName());
					return n1 - n2;
				}

				private int extractNumber(String name) {
					int i = 0;
					try {
						int s = 0;
						int e = name.indexOf('_') - 1;
						String number = name.substring(s, e);
						i = Integer.parseInt(number);
					} catch (Exception e) {
						i = 0;
					}
					return i;
				}
			});
			return files;
		} else
			return null;
	}

	public static void main(String[] args) {
		List<File> files = getFiles("src//main//resources//NewData//172_Split//out2", false);
		List<String> type1files = new ArrayList<>();
		List<String> type2files = new ArrayList<>();

		for (File file : files) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				if (file.length() > 0) {
					AutoDetectParser parser = new AutoDetectParser();
					BodyContentHandler handler = new BodyContentHandler();
					Metadata metadata = new Metadata();
					parser.parse(fis, handler, metadata);
					String data = handler.toString();
					if (data.contains("CLOUDS HAZE MIST ETC ")) {
						type1files.add(file.getName());
					} else if (data.startsWith("CLASSIFIED")) {
						type2files.add(file.getName());

						System.out.println("**************");
						System.out.println(file.getName());

						String[] globalDate = new String[2];
						Matcher monthyear = monthYearPattern.matcher(data);
						if (monthyear.find()) {
							String globMonthYear = monthyear.group();
							globalDate = parseMonthYearFromDocument(globMonthYear);
						}
						System.out.println(globalDate[0] + " " + globalDate[1]);
						UFO ufo = new UFO();
						parseDateTimeDurationOfSighting(data, ufo, globalDate);
						parseDateOfReport(data, ufo, globalDate);
						parseDescription(data, ufo);
						System.out.println(ufo);
						// FileUtils.copyFileToDirectory(file, new
						// File("src//main//resources//NewData//172_Split//out2"));
					}
				}
			} catch (IOException | TikaException | SAXException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fis != null)
						fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Type 1 : " + type1files);
		System.out.println("Type 2 : " + type2files);
		System.out.println(type2files.size());
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
				ufo.setDuration(durationData.split(" ")[0]);
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
		System.out.println(inputLine);
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