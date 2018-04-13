import pandas as pd
import numpy as np
import json 
import os 
import xml.etree.ElementTree as ET
import glob
import random
import string
import ast

def id_generator(size=6, chars=string.ascii_uppercase + string.digits):
    return ''.join(random.SystemRandom().choices(chars, k=size))


def get_ufo_stalker_data():
    # --- aggregate UFO Stalker data --- #
    if os.path.exists('Data/ufo_stalker.csv'):
        df = pd.read_csv('Data/ufo_stalker.csv', compression='gzip') 
        df['urls'] = [ast.literal_eval(u) for u in df['urls'].tolist()]
        return df
        
    if not os.path.exists('Data/Resource/ufo_stalker.json'):
        data = []
        for file in os.listdir('Data/Input/ufo-stalker-json'):
            with open('Data/Input/ufo-stalker-json/{}'.format(file), 'r') as f:
                data.extend(json.load(f)['content'])

        with open('Data/Resources/ufo_stalker.json', 'w') as f:
            for d in data:                           
                f.write(json.dumps(d) + '\n') 

    columns = [
         'city',
         'country',
         'detailedDescription',
         'duration',
         'latitude',
         'longitude',
         'summary',
         'shape',
         'urls',
         'id',
         'submitted',
         'occurred' 
    ]

    valid_formats = {
        'jpg', 'jpeg', 'png', 'bmp', 'gif', 'tiff'
    }

    mapping = {
        'city'      : 'municipality', 
        'detailedDescription' 
                    : 'description', 
        'id'        : 'event_id',
        'submitted' : 'sighted_on',
        'occurred'  : 'reported_on'
    }

    df = (pd.read_json('Data/Resources/ufo_stalker.json', lines=True)
            .reindex(columns=columns)
            .rename(columns=mapping)
            .drop_duplicates(subset=['event_id'])
    )
    # remove invalid links
    df['urls'] = [
        [j for j in i if j.lower().rsplit('.', 1)[-1] in valid_formats] if i else [] for i in df.urls
    ]
    # country
    df['country'] = df['country'].replace({'US' : 'United States', 'UK' : 'United Kingdom', 'BR' : 'Brazil'})
    df.loc[df['country'].str.len().eq(2), 'country'] = np.nan 
    # convert epoch time to datetime
    df['sighted_on'] = pd.to_datetime(df['sighted_on'], errors='coerce', unit='ms')
    df['reported_on'] = pd.to_datetime(df['reported_on'], errors='coerce', unit='ms')
    # fix shape column
    df['shape'] = df['shape'].str.strip().str.replace(r'(?:,\s*)?N,\s*A', '').str.replace('Rectagular', 'Rectangular')
    df.loc[df['shape'].str.len().eq(0), 'shape'] = np.nan
    # load caption and object files

    if os.path.exists('Data/Resources/cap.txt') and os.path.exists('Data/Resources/obj.txt'):
        cap = pd.read_csv('Data/Resources/cap.txt', usecols=['caption', 'event_id'])
        obj = pd.read_csv('Data/Resources/obj.txt', usecols=['label', 'event_id'])
        # merge event data with obeject and caption data
        df = df.merge(cap.merge(obj, on='event_id', how='outer'), on='event_id', how='left')
    # save to CSV
    df.to_csv('Data/ufo_stalker.csv', compression='gzip', index=False)
        
    return df


def get_british_ufo_data():
    if os.path.exists('Data/ufo_british.csv'):
        return pd.read_csv('Data/ufo_british.csv', compression='gzip') 

    records = []
    for base_path in glob.glob('Data/Resources/ocr-output/DEFE-*'):
        root = os.path.join(base_path, 'outtxt-clean-tika')
        for file in os.listdir(root):
            try:
                r = ET.parse(os.path.join(root, file)).getroot()
                records.append([tag.text for tag in r[1]])
            except ET.ParseError:
                continue

    df = (pd.DataFrame(
            records, 
            columns=[
                'description', 'duration', 'location', 'reported_on', 'sighted_on', 'shape'
            ]   
    ).apply(lambda x: x.str.title())
     .replace('""', np.nan)
    )
    df = df.dropna(
        subset=df.columns.difference(['description']).tolist(), 
        how='all'
    )
    df['description'] = df['description'].str.strip('Split By Pdf Splitter\n').str.replace('\n', ' ')
    df[['sighted_on', 'reported_on']] = df[['sighted_on', 'reported_on']].apply(pd.to_datetime, errors='coerce')
    
    m = df.sighted_on > df.reported_on
    df.loc[m, 'sighted_on'], df.loc[m, 'reported_on'] = df.loc[m, 'reported_on'], df.loc[m, 'sighted_on']

    df.to_csv('Data/ufo_british.csv', compression='gzip', index=False)

    return df


def get_ufo_awesome_data():
    return pd.read_csv('Data/ufo_awesome.csv', compression='gzip')

if __name__ == '__main__':
    df_list = []
    for x, y in [
        ('UFO Stalker', get_ufo_stalker_data), 
        ('UFO British', get_british_ufo_data), 
        ('UFO Awesome', get_ufo_awesome_data)]:
        print(f'Loading {x} data...\t', end='\r')

        df_list.append(y())

        print(f'Loading {x} data...\tDONE')

    df = pd.concat(df_list, ignore_index=True).sort_index(axis=1)

    print('Generating random IDs...\t', end='\r')
    df['event_id'] = [id_generator() if pd.isnull(x) else x for x in df['event_id'].tolist()]
    print('Generating random IDs...\tDONE', end='\r')

    df.to_csv('Data/ufo_awesome_v2.csv', compression='gzip', index=False)


