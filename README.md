# UFO-Very-Awesome
CSCI 599 - Assignment 2 - featuring data from the British Council of UFO 
## Named Entity Recognition
The tika-ner folder contains the required jars (tika-app and tika-core-nlp) in the target folder. They have been built from the source and would be required for performing named entity recognition on the UFO Data Set. The open nlp models have also been added on to the repository under - org\apache\tika\parser\ner\opennlp.
Two batch files have been addded to run NER on http://people.apache.org/committer-index.html (OpenNLP + TIKA) and http://www.hawking.org.uk (CoreNLP + Tika) and the outputs are being written in two files - ner_core_nlp_output.txt and ner_open_nlp_output.txt. The batch files can be executed directly to see the results and it doesn't require and other dependency to be downloaded separately. This has been done just for the purposes of demonstration and any of the artifacts and binaries would not be a part of the final deliverable. (Reference - https://wiki.apache.org/tika/TikaAndNER)

## UFO Stalker Process and Setup
In order to scrap the data from the UFO stalker website we had came up with various strategies and each new one was an improved version of the previous version.

### Version 1:
Since all the events had unique IDs, we decided to iterate over the ufo website iteratively 
''' 
http://www.ufostalker.com/event/1 ---------------   http://www.ufostalker.com/event/91148 

'''
Issues: We faced the issues of getting blocked after 200 events. In order to solve this issue we had to install the VPNs. 

### Version 2
In this version 2, we automated the process of installing the VPN i.e the VPNs were a part of the script.Since the websites had lot of ads, we added ad blocker which improved the efficiency of the scraping process

Issues: We still faced the issue of getting blocked

### Golden File
In this release, according to our script we had to hit the website once. We utilized angularJS and selenium methods.

Methodology: 
In this file, our script gets the details of a particular page in an in-memory object. This object  also contains the information of all the events on the page.
The in-memory object was then saved in the file which further helped us the scrape the event details. This process was then iterate over all the pages and we could scrap 89000 events very easily

Advantages: The entire process was completed within an hour’s time.Removed the a lot of boilerplate code.

Steps to run the file:

'''
pip install selenium
python data_scraper_2.py

'''

## Docker Setup
Install the Tika Dockers using git clone for docker file image for image and object identification on the local machine


### Build the Inception v4 model Image detection capability
'''
docker build -f InceptionRestDockerfile -t uscdatascience/inception-rest-tika
'''

### Build and Tell model Image Text Captioning capability

'''
docker build -f Im2txtRestDockerfile -t uscdatascience/im2txt-rest-tika
'''

### Optional (Build a Docker with OpenCV and Tensorflow that can be used to idenitfy objects in Videos)

'''
docker build -f InceptionVideoRestDockerfile -t uscdatascience/inception-video-rest-tika
'''

### Steps to run the docker

1. Get the docker running on port 8764
'''
docker run -it -p 8764:8764 uscdatascience/inception-rest-tika
'''

2. Get the docker for captions running on port 8764

'''
gets the docker for captions running on port 8764

'''

## Tensor Flow Image Recognition Module in Tika

1. Install Apache Maven with Java 8 and set up Tika on local machine
2. Ensure the docker is up and running on port 8764
3. Set/modify parameters in the Tika-Config XML to enable Tensorflow parser

### Steps for detecting the object
Run the Objects.py file to generate object tags for the input image URLS

### Steps for captioning the image
Run the Captioner.py file to generate captions for the input image URLS

## Report:
Task: To sample a subset of the UFO images and analyse the results of variations wrt to parameters.
Following are the observations
Confidence measure





