import requests

f1 = open('Data/Resources/urls.txt', 'r')
urls = f1.read().split('\n')

ip = 'http://localhost:8764/inception/v3/caption/image?url='
count = 0 
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

			outputText = url.strip('\n')+ ","+ caption+"\n"
			file=open("Data/Resurces/cap.txt","a+")
			file.write(outputText)
			file.close()

			if count==2100:
				break
		except Exception as e:
			print("error: ",e)

