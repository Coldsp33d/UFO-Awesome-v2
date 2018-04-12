from PIL import Image
import os

maxsize = (256, 256)
for root, _, files in os.walk('Data/Input/ufo-stalker-images'):
    for f in files:
    	path = os.path.join(root, f)
    	ext = f.split('.')[-1] 
    	ext = 'jpeg' if ext == 'jpg' else ext

    	try:
    		im = Image.open(path)
    		im.thumbnail(maxsize, Image.ANTIALIAS)
    		im.save(path, ext)
    		print('Resized {}'.format(path))
    	except (IOError, ValueError):
    		pass

