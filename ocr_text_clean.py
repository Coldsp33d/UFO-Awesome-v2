import re,string,os,sys
from collections import Counter
from autocorrect import spell
punctuation = string.punctuation

#Spell check done with Peter Norvig's Rule Based Spell Checker
def words(text): return re.findall(r'\w+', text.lower())

WORDS = Counter(words(open('big.txt').read()))

def P(word, N=sum(WORDS.values())):
    "Probability of `word`."
    return WORDS[word] / N

def correction(word):
    "Most probable spelling correction for word."
    return max(candidates(word), key=P)

def candidates(word):
    "Generate possible spelling corrections for word."
    return (known([word]) or known(edits1(word)) or known(edits2(word)) or [word])

def known(words):
    "The subset of `words` that appear in the dictionary of WORDS."
    return set(w for w in words if w in WORDS)

def edits1(word):
    "All edits that are one edit away from `word`."
    letters    = 'abcdefghijklmnopqrstuvwxyz'
    splits     = [(word[:i], word[i:])    for i in range(len(word) + 1)]
    deletes    = [L + R[1:]               for L, R in splits if R]
    transposes = [L + R[1] + R[0] + R[2:] for L, R in splits if len(R)>1]
    replaces   = [L + c + R[1:]           for L, R in splits if R for c in letters]
    inserts    = [L + c + R               for L, R in splits for c in letters]
    return set(deletes + transposes + replaces + inserts)

def edits2(word):
    "All edits that are two edits away from `word`."
    return (e2 for e1 in edits1(word) for e2 in edits1(e1))

def cleanFile(list):
    list = [sent.strip(punctuation and "\n") for sent in list]
    cleanedList =[]
    for sent in list:
        if sent != "\n" and len(sent) != 1:
            sent = re.sub("[^A-Z0-9$.:/]+", " ", sent.upper()).lstrip("\n").strip()
            sent = re.sub(r'\.{1,}','.',sent)
            if sent!='':
                cleanedList.append(sent)
    return  cleanedList

def spellCheck(cleanedList):
    cleaned =[]
    for sent in cleanedList:
        unclassified_check = re.compile('^[U|UN]{1}[\s]{0,1}[N|C|W]{1}')
        if re.match(unclassified_check, sent):
            sent = 'UNCLASSIFIED'
        sent = sent.strip(punctuation)
        wordList = sent.split()

        temp_sent = ""
        index = ['A.', 'B.', 'C.', 'D.', 'E.', 'F.', 'G.', 'H.', 'I.', 'J.', 'K.', 'L.', 'M.', 'N.', 'O.', 'P.', 'Q.',
                 'R.']
        for word in wordList:
            print(word)
            if len(word) > 1 and not word.isdigit() and word not in index:  # and not re.match(index,word): not word.isalnum() and
                print(word)
                word = spell(word.lower())
                word = correction(word.lower())
            temp_sent += word + " "
        cleaned.append(temp_sent.upper() + "\n\n")
    return  cleaned

def processFiles(base_path):
    files = os.listdir(base_path)
    for f in files:
        list = open(base_path + "\\" + f, 'r').readlines()
        cleanedList = cleanFile(list) #removes all garbled data
        spellChecked = spellCheck(cleanedList) #performs spelling correction
        outf = open(base_path + "\\" + f.strip(".txt") + "_cleaned.txt", 'w+')
        for line in spellChecked:
            outf.write(line)

if __name__ == "__main__":
 base_path = sys.argv[1]
 #base_path = "C:\\Users\\Dell\\Desktop\\SEM2\\CSCI 599- Content Detection & Big data\Assignments\\2\\Final\\173_Split\\out"
 processFiles(base_path)
