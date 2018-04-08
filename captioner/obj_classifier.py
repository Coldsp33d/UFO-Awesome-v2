import requests

f1 = open('Input.txt','r')
urls = f1.read().split('\n')

#r=requests.get('http://localhost:8764/inception/v4/classify/image?url=https://www.mufoncms.com/files_jeud8334j/91154_submitter_file2__IMG2007.PNG')

ip = 'http://localhost:8764/inception/v4/classify/image?url='
outputText = ""
count = 0 
for url in urls:
	if "png" in url.lower() or "jpg" in url.lower() or "jpeg" in url.lower():
		try:
			getRequest = ip + url.strip()
			r=requests.get(getRequest)
			mydict = r.json()
			myList=mydict['classnames']
			myObject=myList[0]
			count+=1	
			print(count,"------------>",myObject)
			outputText += url.strip('\n')+ ","+myObject+"\n"  #.strip('\n')
		except Exception as e:
			print("error: ",e)

#https://www.mufoncms.com/files_jeud8334j/91082_submitter_file2__114AC76BDD4F467987291036E310EE8B.jpeg')
#https://www.mufoncms.com/files_jeud8334j/91154_submitter_file6__IMG2055.PNG') 
'''
mydict=r.json()
#print(mydict['classnames'])
myList=mydict['classnames']
object=myList[0]
'''
file=open("Output.txt","w")
file.write(outputText)
file.close()



