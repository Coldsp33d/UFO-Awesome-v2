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


## Part 3 - Dataset Joining

Before getting to this part, please ensure that the scripts in Part 1 and Part 2 have successfully run and generated their output.

The command

    python aggregator.py

Will preprocess and join all the intermediate datasets (UFO Stalker data, British UFO data, and UFO Awesome data) and output `Data/ufo_awesome_v2.csv`.

## Part 4 - Tika NER 
The tika-ner folder contains the required jars (tika-app and tika-core-nlp) in the target folder. They have been built from the source and would be required for performing named entity recognition on the UFO Data Set. The open nlp models have also been added on to the repository under - org\apache\tika\parser\ner\opennlp.
Two batch files have been addded to run NER on http://people.apache.org/committer-index.html (OpenNLP + TIKA) and http://www.hawking.org.uk (CoreNLP + Tika) and the outputs are being written in two files - ner_core_nlp_output.txt and ner_open_nlp_output.txt. The batch files can be executed directly to see the results and it doesn't require and other dependency to be downloaded separately. This has been done just for the purposes of demonstration and any of the artifacts and binaries would not be a part of the final deliverable. (Reference - https://wiki.apache.org/tika/TikaAndNER)








