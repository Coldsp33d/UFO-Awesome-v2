# Content Detection Assignment - 2 - 

Data is extracted from 
1. British UFO data files, and 
2. [UFO Stalker](http://www.ufostalker.com)

This data is then joined with the UFO-Awesome Data from the first assignment. 



## Team Members

1.  Akashdeep Singh
2.  Koustav Mukherjee
3.  Sayali Ghaisas
4.  Shiva Deviah
5.  Vritti Rohira

## Getting Started

This project was written in `python-3.6`. To install all required modules, please run

    pip install --upgrade -r requirements.txt 
    

## Part 1 - British UFO Data

### Optical Character Recognition

(pdf splitting, imagemagik, ms tool, tesseract using LSTM --- enumerate!!!!)

The OCR'd data is in `Data/Resources/DEFE-*/outtxt/`.


### Preprocessing

To preprocess the OCR'd data, run

    python parser_main.py

This will in turn run the code in `parser.py` to clean and spell check the OCR'd data, after which it invokes our custom Tika parser to perform content extraction and generate output XML files. These XML files are stored in `Data/Resources/DEFE-*/outtxt-clean-tika/`. Once these files are aggregated, they will contain over **_1,300_** records.


## Part 2A - UFO Stalker Data

### Webscraping

Webscraping is done in two stages. First, extract the event data from UFO stalker - 

    python data_scraper_2.py
    
This takes a few hours, but will successfully scrape the entire website using `selenium` and `angular` and generate JSON files in `Data/Input/ufo-stalker-json`. Once this data is aggregated, it will contain over **_87,000_** records.

A previous, much slower iteration of this script used AdBlock and AnonymoX proxy plugins to scrape data, and was not as efficient. 

Once the URLs are scraped, run 

    python image_scraper.py
    

This will pick one image URL from every event ID and attempt to download the images to retrain the models. Each image is downloaded into `Data/Input/ufo-stalker-images/<Shape>` where `<Shape>` is the shape of the UFO captured in that image.  

Finally, you'll need to resize the images (the original images are too large for the models to handle). Run

    python resize.py
    
Note: you may run into errors during model retraining if any of the images are corrupt/partially downloaded. Another possible cause for failure is if your individual shape folders have lesser than 30 images inside, in which case the model cannot train on such categories - you'll have to remove them before re-training. 

## Part 2B - Image Recognition

#### Performing Image Recognition

1. Install Apache Maven with Java 8 and setup Tika on local machine
2. Download, build, and run the docker images on port `8764`
3. Set/modify parameters in the Tika-Config XML to enable the Tensorflow parser
4. `cd` into the `img2txt/` folder in our project using
   
       cd img2txt
       
To caption images, run

    python captioner.py
    
The outputs can be found in `Data/Resources/cap.txt`.

To label objects in images, run

    python objects.py  

The outputs can be found in `Data/Resources/obj.txt`.

Some of the examples were as follows:

eg: URL: https://www.mufoncms.com/files_jeud8334j/91154_submitter_file6__IMG2055.PNG
0.03 ---> max allowable
0.04 does not work

````
<title>500 Internal Server Error</title>
<h1>Internal Server Error</h1>
<p>The server encountered an internal error and was unable to complete your request.  Either the server is overloaded or there is an error in the application.</p>

INFO  Time taken 18751ms
WARN  NO objects
````

Parameters for Captions:
Captions- number of captions returned with corresponding confidence values in descending order. The highest confidence caption is returned first.
maxCaptionLength- maximum length defsult set to 15.
Getting the top 3 captions:(caption word limit set to 30)
Eg1: https://www.mufoncms.com/files_jeud8334j/91198_submitter_file5__A915CA3A9D784CD19AC565CB5398C055.png

```
</head>
<body><ol id="captions">    <li id="0"> a view of a street from a plane . [eng](confidence = 0.000014)</li>
    <li id="1"> a view of a city street from a plane . [eng](confidence = 0.000007)</li>
    <li id="2"> a view of a city street from a car . [eng](confidence = 0.000002)</li>
</ol>
</body></html>
```

Eg2: https://www.mufoncms.com/files_jeud8334j/91154_submitter_file2__IMG2007.PNG

```
INFO  Available = true, API Status = HTTP/1.0 200 OK
INFO  Captions = 3, MaxCaptionLength = 30
INFO  Recogniser = org.apache.tika.parser.captioning.tf.TensorflowRESTCaptioner
INFO  Recogniser Available = true
INFO  Time taken 14419ms
INFO  Add Caption{sentence='a person flying a kite in a field .' (eng), confidence=4.968589056338163E-4}
INFO  Add Caption{sentence='a person flying a kite in the sky .' (eng), confidence=4.8810816172524497E-4}
INFO  Add Caption{sentence='a man flying a kite in a park .' (eng), confidence=4.261081032342596E-4}
<?xml version="1.0" encoding="UTF-8"?><html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta name="org.apache.tika.parser.recognition.object.rec.impl" content="org.apache.tika.parser.captioning.tf.TensorflowRESTCaptioner"/>
<meta name="X-Parsed-By" content="org.apache.tika.parser.CompositeParser"/>
<meta name="X-Parsed-By" content="org.apache.tika.parser.recognition.ObjectRecognitionParser"/>
<meta name="resourceName" content="91154_submitter_file2__IMG2007.PNG"/>
<meta name="Content-Length" content="139385"/>
<meta name="CAPTION" content="a person flying a kite in a field . (0.00050)"/>
<meta name="CAPTION" content="a person flying a kite in the sky . (0.00049)"/>
<meta name="CAPTION" content="a man flying a kite in a park . (0.00043)"/>
<meta name="Content-Type" content="image/jpeg"/>
<title/>
</head>
<body><ol id="captions">    <li id="0"> a person flying a kite in a field . [eng](confidence = 0.000497)</li>
    <li id="1"> a person flying a kite in the sky . [eng](confidence = 0.000488)</li>
    <li id="2"> a man flying a kite in a park . [eng](confidence = 0.000426)</li>
</ol>
```



## Part 3 - Dataset Joining

Before getting to this part, please ensure that the scripts in Part 1 and Part 2 have successfully run and generated their output.

The command

    python aggregator.py

Will preprocess and join all the intermediate datasets (UFO Stalker data, British UFO data, and UFO Awesome data) and output `Data/ufo_awesome_v2.csv`.

## Part 4 - Tika NER 
We have used Stanford CORE NLP to get the best NER Results from our UFO Data Set. A new column "ner" has been added to the data set and they correspond to all the named entities found in the description column. The following command can be executed to generate the "ner" columns.
	
	java -classpath NERRecognizerUFO.jar;commons-csv-1.4.jar;tika-app-2.0.0-SNAPSHOT.jar;tika-ner-corenlp-addon-1.0-SNAPSHOT-jar-with-dependencies.jar NERRecognizerCustom <path_to_ufo_awesome_v2.csv> <output_path_ufo_awesome_v2_ner.csv>

The tika-ner folder contains the required jars (tika-app and tika-core-nlp) in the target folder. They have been built from the source and would be required for performing named entity recognition on the UFO Data Set. The open nlp models have also been added on to the repository under - org\apache\tika\parser\ner\opennlp.
Two batch files have been addded to run NER on http://people.apache.org/committer-index.html (OpenNLP + TIKA) and http://www.hawking.org.uk (CoreNLP + Tika) and the outputs are being written in two files - ner_core_nlp_output.txt and ner_open_nlp_output.txt. The batch files can be executed directly to see the results and it doesn't require and other dependency to be downloaded separately.
