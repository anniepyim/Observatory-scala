import cv2

years = ["2001","2002","2003","2004","2005","2006","2007","2008"]

for year in years:
	
	im =  cv2.imread(year+"/0/0-0.png", cv2.IMREAD_UNCHANGED)

	imgheight=im.shape[0]
	imgwidth=im.shape[1]

	for n in range(1,5):

		scale = 2**n

		y1 = 0
		M = imgheight//scale
		N = imgwidth//scale

		for y in range(0,imgheight,M):
		    for x in range(0, imgwidth, N):
		        y1 = y + M
		        x1 = x + N
		        tiles = im[y:y+M,x:x+N]

		        yy = int(y/M)
		        xx = int(x/M)

		        #cv2.rectangle(im, (x, y), (x1, y1), (255, 255, 255),0)
		        cv2.imwrite(year+"/"+ str(n) +"/" + str(xx) + '-' + str(yy)+".png",tiles)
