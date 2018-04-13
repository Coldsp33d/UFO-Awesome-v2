import re
import string
import os
import sys
from collections import Counter, defaultdict
from autocorrect import spell
# from autocorrect_norvig import correction as spell
import natsort
import glob

punctuation = string.punctuation + '\n \t\r\f'
unclassified_pattern = re.compile(r'^(?:U|UN)\s{0,1}[NCW]')
type_check_pattern = re.compile(r'B\.?\s+')
clean1_pattern = re.compile(r'[^A-Z0-9.:/]+')
clean2_pattern = re.compile(r'\.{1,}')

index = {
    'A.', 'B.', 'C.', 'D.', 'E.', 'F.', 
    'G.', 'H.', 'I.', 'J.', 'K.', 'L.', 
    'M.', 'N.', 'O.', 'P.', 'Q.', 'R.'
}

def process_list_files(text):
    for j, line in enumerate(text):
        line = line.strip()
        m = re.match(r'(A)\.?\s+\d+', line) or re.match(r'([A-P0])\.?\s+.*', line)
        if m and m.group(1).isalpha():
            break

    before = text[:j]
    text = text[j:]

    i, cur = 0, -1
    data = defaultdict(list)
    classified = False
    while i < len(text) and chr(cur + 65) < 'P':
        if 'classified' in text[i].lower():
            x = None
            while i < len(text) - 1:    
                x, y = re.match(r'(?:(.)\.?\s+)?(.*)', text[i]).groups()        

                if x:
                    break
                i += 1
                j += 1

        
        x, y = re.match(r'(?:(.)\.?\s+)?(.*)', text[i]).groups()
    
        if x and x.isalpha():
            cur += 1

        if chr(cur + 65) == 'I' and x != 'I':
            cur += 1

        data[chr(cur + 65)].append(y)
        i += 1
        j += 1

    new_data = {}
    for k, v in data.items():
        new_data[k] = ' '.join(v)

    out_sent = []
    for k, v in new_data.items():
        out_sent.append(k + ' ' + v)

    return before + out_sent + text[j + 1:]

def process(sent_list):
    ftype = 'generic'
    sent_list_clean = []
    for sent in sent_list:
        sent = sent.strip(punctuation)
        if not sent:
            continue
        elif unclassified_pattern.match(sent):
            sent = 'UNCLASSIFIED'
        else:
            word_list = []
            for word in clean2_pattern.sub('.', clean1_pattern.sub(" ", sent.strip())).split():
                if not (len(word) == 1 or word.isdigit() or word in index):
                    word = spell(word.lower())
                word_list.append(word)

            sent = ' '.join(word_list).upper()

            if ftype == 'generic': # check only once 
                if sent.startswith('CLOUDS') and 'ETC' in sent:
                    ftype = 'tabular'
                elif type_check_pattern.match(sent):
                    ftype = 'list'

        sent_list_clean.append(sent)

    if ftype == 'list':
        sent_list_clean = process_list_files(sent_list_clean)

    return ftype, sent_list_clean

if __name__ == "__main__":
    for base_path in glob.glob('Data/Resources/ocr-output/DEFE-*'):
        src_path = os.path.join(base_path, 'outtxt')
        dst_path = os.path.join(base_path, 'outtxt-clean')

        if not os.path.exists(dst_path):
            os.mkdir(dst_path)

        processed = set()
        for file in natsort.natsorted(os.listdir(src_path)):
            if file in processed:
                continue

            print(f"{os.path.join(src_path, file)}")
            processed.add(file)

            with open(os.path.join(src_path, file), 'r') as f:
                ftype, sent_list_clean = process(f.read().upper().splitlines())

            if ftype == 'tabular':
                sent_list_clean.append('------------------------------------------')
                file_p2 = '{}.txt'.format(int(file.split('.')[0]) + 1)
                processed.add(file_p2)

                with open(os.path.join(src_path, file_p2), 'r') as f:
                    
                    sent_list_clean.extend(process(f.read().upper().splitlines())[-1])

            if not any(sent_list_clean):
                continue
                
            with open(os.path.join(dst_path, '{}.{}.ufo'.format(file.split('.')[0], ftype)), 'w') as f:
                for sent in sent_list_clean:
                    f.write(sent + '\n')


