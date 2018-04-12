package org.ufoveryawesome.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
		UFOParser parser = new UFOParser();
	    BodyContentHandler handler = new BodyContentHandler();
	    Metadata metadata = new Metadata();
	    ParseContext parseContext = new ParseContext();
	    File file = new File("src//main//resources//Data//21.txt");
	    FileInputStream fis = new FileInputStream(file);
	    parser.parse(fis, handler, metadata, parseContext);
	    System.out.println(handler.toString());
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
