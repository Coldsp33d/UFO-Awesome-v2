import requests

f1 = open('../Data/Resources/urls.txt', 'r')
urls = f1.read().split('\n')

ip = 'http://localhost:8764/inception/v4/classify/image?url='
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

			outputText = url.strip()+ ","+myObject+"\n"  #.strip('\n')
			file=open("../Data/Resources/obj.txt", "a+")
			file.write(outputText)
			file.close()

			if count==2100:
				break
		except Exception as e:
			print("error: ",e)






