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

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.ocr.TesseractOCRParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.drew.lang.annotations.NotNull;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws Exception {
		/*
		Parser parser = new TesseractOCRParser();
		File file = new File("src//main//resources//Data//21.txt");
		TesseractOCRConfig config = new TesseractOCRConfig();
		config.setTesseractPath("C://Program Files (x86)//Tesseract-OCR");
		config.setEnableImageProcessing(1);
		config.setLanguage("eng");
		config.setApplyRotation(true);
		config.setImageMagickPath("C://Program Files//ImageMagick-7.0.7-Q16");
		FileInputStream fis = new FileInputStream(file);
		BodyContentHandler handler = new BodyContentHandler();
		ParseContext parseContext = new ParseContext();
		parseContext.set(TesseractOCRConfig.class, config);
		String ocr = App.getText(fis, new AutoDetectParser(), parseContext, new Metadata());
		System.out.println(ocr);
		*/
		//AutoDetectParser parser = new AutoDetectParser();
		//UFOParser parser = new UFOParser();
		List<File> files = getFiles("src//main//resources//NewData//172_Split//out3", false);
		TikaConfig config = new TikaConfig("src//main//resources//tika-config.xml");
		//Parser parser = new org.apache.tika.parser.ufo.UFOParser();
		//AutoDetectParser parser = new AutoDetectParser();
		Tika app = new Tika();
		
		for (File file : files) {
			FileInputStream fis = new FileInputStream(file);
		    BodyContentHandler handler = new BodyContentHandler();
		    Metadata metadata = new Metadata();
		    ParseContext parseContext = new ParseContext();
		    app.getParser().parse(fis, handler, metadata, parseContext);
		    System.out.println(app.detect(fis));
		    //System.out.println(handler.toString());
		}
	}

	public static List<File> getFiles(@NotNull String idn, @NotNull boolean recursive) {
		File dirI = new File(idn);
		List<File> files = new ArrayList<>();
		if (dirI.exists() && dirI.isDirectory()) {
			Iterator<File> iterator = FileUtils.iterateFiles(dirI, new String[] { "ufo" }, recursive);
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
	
	public static String getText(InputStream is, Parser parser, ParseContext context, Metadata metadata) throws Exception {
		ContentHandler handler = new BodyContentHandler(1000000);
		try {
			parser.parse(is, handler, metadata, context);
		} finally {
			is.close();
		}
		return handler.toString();
	}
}
