import pandas as pd
import numpy as np
import json 
import os 
import xml.ElementTree as ET
import glob

# --- aggregate UFO Stalker data --- #
if not os.path.exists('Data/ufo_stalker.csv'):
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
    # convert epoch time to datetime
    df['sighted_on'] = pd.to_datetime(df['sighted_on'], errors='coerce', unit='ms')
    df['reported_on'] = pd.to_datetime(df['reported_on'], errors='coerce', unit='ms')
    # fix shape column
    df['shape'] = df['shape'].str.strip().str.replace(r'(?:,\s*)?N,\s*A', '').str.replace('Rectagular', 'Rectangular')
    df.loc[df['shape'].str.len().eq(0), 'shape'] = np.nan
    # load caption and object files
    cap = pd.read_csv('Data/Resources/cap.txt', usecols=['caption', 'event_id'])
    obj = pd.read_csv('Data/Resources/obj.txt', usecols=['label', 'event_id'])
    # merge event data with obeject and caption data
    df = df.merge(cap.merge(obj, on='event_id', how='outer'), on='event_id', how='left')
    # save to CSV
    df.to_csv('Data/ufo_stalker.csv', compression='gzip', index=False)
else:
    # TODO - uncomment this out later
    # df = pd.read_csv('Data/ufo_stalker.csv', compression='gzip')
    pass


records = []
for file in os.listdir('xml_shiva'):
    r = ET.parse('xml_shiva/{}'.format(file)).getroot()
    records.append([tag.text for tag in r[1]])

df = pd.DataFrame(
        records, 
        columns=[
            'description', 'duration', 'location', 'reported_on', 'sighted_on', 'shape'
        ]   
).applymap(str.title).replace('""', np.nan)



