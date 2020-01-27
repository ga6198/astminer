import csv
import sys
import pandas as pd
maxInt = sys.maxsize

#Get command line arguments
PATH_CONTEXTS_DIR = sys.argv[1] #'path_contexts_0_1.csv'
TOKENS_DIR = sys.argv[2] #'tokens_1.csv'
PATHS_DIR = sys.argv[3] #'paths_1.csv'
NODE_TYPES_DIR = sys.argv[4] #'node_types_1.csv'
OUTPUT_FILE = sys.argv[5] #'output.txt' #'test.c2v'

#Use this code to handle the error with column size being too big for the csv
while True:
    try:
        csv.field_size_limit(maxInt)
        break
    except OverflowError:
        maxInt = int(maxInt/10)

def open_csv(directory):
    #Get all of the csv files' values
    """with open(directory) as csv_file:
        csv_reader = csv.reader(csv_file, delimiter=',')
        line_count=0
        for row in csv_reader:
            if line_count == 0:
                print(f'Column names are {", ".join(row)}')
                line_count += 1
            else:
                print(f'\t Label: {row[0]} Values: {row[1]}.')
                line_count += 1
        print(f'Processed {line_count} lines.')"""

    #df = pd.read_csv(directory, quotechar="'")
    df = pd.read_csv(directory, quotechar="~") #Use ~ as a separator
    print(df)
    #print(type(df['path_contexts'][0]))
    #second column is a string

    #need to return a dataframe or something
    return df

if __name__ == "__main__":
    #Get all of the csv files' values
    pathContexts = open_csv(PATH_CONTEXTS_DIR)
    tokens = open_csv(TOKENS_DIR)
    paths = open_csv(PATHS_DIR)
    node_types = open_csv(NODE_TYPES_DIR)

    pathContextsLabels = pathContexts['label']

    #get ids for pathContexts
    pathContextsIds = pathContexts['path_contexts'] #column of path context ids
    print(pathContextsIds)
    print(type(pathContextsIds)) #pandas series
    #print(pathContextsIds.data)
    pathContextsArray = []
    for i in range(len(pathContextsIds)):
        line = pathContextsIds[i]
        line = line.split(';') #divide lines based on semicolon
        #line is now a list of path contexts ex. ['1 2 3', '34 56 23', '2 67 39']
        print(pathContextsIds[i])
        print(line)
        pathContextsArray.append(line)
        #pathContextsArray is list of each pathContextIdRow
        #ex. [['1 2 3', '5 7 4'], ['4 6 2'], ['3 23 1', '5 34 1']]
        # each represents the path contexts for the current file
        #still need to split between spaces

    #for line in

    #print(tokens)

    #try to turn token data frame into dictionary?
    #tokensDict = tokens.to_dict()
    tokensDict = dict(zip(tokens.id, tokens.token)) #make dict w/ id as key
    print(tokensDict)
    #print(tokensDict[1]) #defined
    #print(tokensDict[296]) #nan (aka NULL)
    #for currentFilePathContexts in pathContextsArray:

    #dictionaries
    pathsDict = dict(zip(paths.id, paths.path))

    #turn nodes into dictionary as well
    nodeTypesDict = dict(zip(node_types.id, node_types.node_type))

    htmlElementsCounter = 0
    
    for i in range(len(pathContextsArray)):
        currentFilePathContexts = pathContextsArray[i]
        
        with open(OUTPUT_FILE, 'w') as resultsFile:
            #get the function label w/ |
            #resultsFile.write(pathContextsLabels[i])
            #resultsFile.write("|")
            #IMPORTANT: THIS DOES NOT ACTUALLY GET THE FUNCTION NAME AND INSTEAD GETS THE FILE NAME
            
            for pathContext in currentFilePathContexts:
                #separate each of the ids
                indivIds = pathContext.split(' ') #['1', '5', '34']
                print(indivIds)
                firstTokenId = int(indivIds[0]) #need to switch string to int
                pathId = int(indivIds[1])
                lastTokenId = int(indivIds[2])

                #get the first token
                #print(firstTokenId)
                print("First Token value")
                print(tokensDict[firstTokenId])
                resultsFile.write(str(tokensDict[firstTokenId]))

                resultsFile.write(',')

                #get the paths
                print("Path Ids")
                #print(tokens[pathId])
                pathTokens = pathsDict[pathId] #string of mult ids in the nodes file
                indivPathIds = pathTokens.split() #['110', '2', '3', '16']
                for i in range(len(indivPathIds)):
                    print('current node id')
                    currentNodeId = int(indivPathIds[i])
                    print(currentNodeId)

                    nodeValue = nodeTypesDict[currentNodeId]
                    resultsFile.write(str(nodeValue))

                    if "htmlElements" in str(nodeValue):
                        htmlElementsCounter += 1

                    
                    if i != len(indivPathIds) - 1: #if on the last value, don't print
                        resultsFile.write('|')

                resultsFile.write(',')

                #get the last token
                #print(lastTokenId)
                print("Last Token value")
                print(tokensDict[lastTokenId])
                resultsFile.write(str(tokensDict[lastTokenId]))

                #resultsFile.write(',')
                resultsFile.write('\n')

    print("Html Elements counter:", htmlElementsCounter)
            

    
