import requests

f1 = open('Input.txt','r')
urls = f1.read().split('\n')

#r=requests.get('http://localhost:8764/inception/v3/caption/image?url=https://www.mufoncms.com/files_jeud8334j/91154_submitter_file2__IMG2007.PNG')
ip = 'http://localhost:8764/inception/v3/caption/image?url='
outputText = ""
count = 0 
#https://www.mufoncms.com/files_jeud8334j/91082_submitter_file2__114AC76BDD4F467987291036E310EE8B.jpeg')
#https://www.mufoncms.com/files_jeud8334j/91154_submitter_file6__IMG2055.PNG')

for url in urls:
	if "png" in url.lower() or "jpg" in url.lower() or "jpeg" in url.lower():
		try:
			getRequest = ip + url.strip()
			r=requests.get(getRequest)
			dict2=r.json()
			dlist=dict2['captions']
			dict3=dlist[0]
			caption=dict3['sentence']
			count+=1	
			print(count,"------------>",caption)
			outputText += url.strip('\n')+ ","+ caption+"\n"
		except Exception as e:
			print("error: ",e)

'''
dict2=r.json()
#print(dict2['captions'])
file=open("outputCaptioner1.txt","w")
dlist=dict2['captions']
#compare the caption values and pick the highest confidence value or take default 1st caption as seen in html output
dict3=dlist[0]
#print(dict3['sentence'])
caption=dict3['sentence']
'''

file=open("Output.txt","w")
file.write(outputText)
file.close()

