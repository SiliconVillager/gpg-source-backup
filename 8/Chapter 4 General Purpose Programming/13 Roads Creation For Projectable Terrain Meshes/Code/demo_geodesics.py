import Image
import ImageDraw
import math

class GeodesicsCalculator:
    """ Demonstrates calculation of geodesic curve connecting 
    points A and B on the terrain defined by its height map hMap,
    which is a grey scale image supported via PIL (Python Image Library).
    Parameter lmbda is non-negative float value parameter that penalizes 
    altitude variation along the curve.
    """

    def __init__(self, hMap, drawImg, a, b, lmbda,  
                maxSubdivisions = 30, #max subdivisions when optimzing the curve        
                lengthThreshold = 6, #shortest sqaured length of an edge that can be sudbivided
                maxDistFromMiddlePoint = 2): # restricts distance from middle point when optimizing single node
        """Initialization: setting up list for nodes, etc"""
        self.hMap = hMap.convert("L") # ensure it's grey scale!
        self.drawImg = drawImg
        self.width = hMap.size[0]
        self.height = hMap.size[1]
        self.a = a
        self.b = b
        self.lmbda = lmbda
        self.maxSubdivs = maxSubdivisions
        self.lengthThreshold = lengthThreshold 
        self.maxDistFromMiddlePoint = 2
        # set up nodes of discrete representaion 
        # of the geodesic: 
        self.nodes = []
        self.nodes.append(self.a)
        self.nodes.append(self.b)
        self.draw = ImageDraw.Draw(self.drawImg)
        self.color = "green"
    
    def calculate(self):
        """The top level function calculating geodesic curve 
        approximation for the current parameters."""
        for i in range(self.maxSubdivs):
            if self.subdivide() > 0:
                self.optimize(1.0, 10)
            else: # no more subidivsions
                break
            
    def drawCurve(self):
        """Draws the piece-wise linear curve using calculated nodes."""
        for i in range(len(self.nodes)-1):
            self.draw.line((self.nodes[i][0], self.nodes[i][1], self.nodes[i+1][0], self.nodes[i+1][1]), fill = self.color)
        
    def subdivide(self):
        """Inserts new nodes where distance between nodes is large enoough."""
        nPrevNodes = len(self.nodes)
        newNodes = []
        for i in range(len(self.nodes)):
            newNodes.append(self.nodes[i])
            if i < len(self.nodes)-1:
                dx = self.nodes[i][0] - self.nodes[i+1][0]
                dy = self.nodes[i][1] - self.nodes[i+1][1]
                length = dx*dx + dy*dy
                if length >= self.lengthThreshold:
                    middle = [(self.nodes[i][0] + self.nodes[i+1][0])/2, (self.nodes[i][1] + self.nodes[i+1][1])/2]
                    newNodes.append(middle)
        nNextNodes = len(newNodes)
        self.nodes = newNodes
        return nNextNodes  - nPrevNodes
    
    def optimize(self, threshold, maxIters):
        """Iteratively optimizes curve by optimizing each node while
        the number of iterations less than maxIters and cost change 
        is larger then threshold."""
        costChange = threshold + 1
        iter = 0
        while costChange > threshold and iter < maxIters:
            costChange = 0
            iter += 1
            for i in range(len(self.nodes)):
                costChange += self.optimizeAtNode(i)
    
    def optimizeAtNode(self, i):
        """ Optimizes location of the node i and returns difference 
        between original cost of the curve at node i and the new cost."""
        
        # end points are fixed:
        if i == 0 or i == len(self.nodes)-1:
            return 0
        
        # get original cost:
        cost0 = self.costFunctionSquared(self.nodes[i-1], self.nodes[i]) + self.costFunctionSquared(self.nodes[i], self.nodes[i+1])
        
        # obtain mid point  between nodes i-1 and i+1
        midpoint = [(self.nodes[i-1][0] + self.nodes[i+1][0])/2, (self.nodes[i-1][1] + self.nodes[i+1][1])/2]
        
        # get normalized vector (Dy, -Dx) perpendicular to the line cut 
        # connecting node[i-1], node[i+1]:
        Dx = self.nodes[i+1][0] - self.nodes[i-1][0]
        Dy = self.nodes[i+1][1] - self.nodes[i-1][1]

        Norm = math.sqrt(Dx*Dx + Dy*Dy) + 1e-5 # ensure no division by 0
        Dx /= Norm
        Dy /= Norm

        # iterate over pixels on the median line:
        nPoints = int(self.maxDistFromMiddlePoint * Norm)  
        bestResult = None
        for j in range(-nPoints, nPoints):
            testPoint = [midpoint[0] + j * Dy, midpoint[1] - j * Dx]
            
            # ensure we are still within terrain limits
            if testPoint[0] < 0 or testPoint[1] < 0 or testPoint[0] >= self.width or testPoint[1] >= self.height:
                continue
            
            costSquared = self.costFunctionSquared(testPoint, self.nodes[i-1])
            costSquared += self.costFunctionSquared(testPoint, self.nodes[i+1])
            if bestResult == None or bestResult[1] > costSquared:
                bestResult = [j, costSquared, testPoint]

        if bestResult != None:
            self.nodes[i] = bestResult[2]
            newCost = bestResult[1]
            return math.sqrt(newCost) - math.sqrt(cost0)
            
        return 0
        
    def costFunctionSquared(self, p, q):
        """ Calculates cost function for a curve with two nodes p and q. """
        Dx = p[0] - q[0]
        Dy = p[1] - q[1]
        pz = self.hMap.getpixel((p[0],p[1]))
        qz = self.hMap.getpixel((q[0],q[1]))
        Dz = pz - qz
        return Dx*Dx + Dy*Dy + self.lmbda * Dz*Dz

# Run the test:

inputHeightMapFile = "ridge.png"
outputDebugImageFile = "ridge_path.png"

# set of lambdas to test
lmbdas = [0, 0.5, 4, 128, 512] 

# open heigh tmap:
img = Image.open(inputHeightMapFile) 

# allocate debug draw image
drawImg = Image.new("L", img.size, 255) # Use: Image.open("ridge.png") if you want to draw into the original image

p0 = [220, 10]  # road start 
p1 = [420, 500] # road end 

for i in range(len(lmbdas)):
    gc = GeodesicsCalculator(img, drawImg, p0, p1, lmbdas[i] )
    gc.calculate()
    gc.drawCurve()
    print len(gc.nodes)
drawImg.save(outputDebugImageFile)
    
