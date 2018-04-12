import pandas as pd
import numpy as np
import json 


'''
# --- process event data from JSON --- #
# read each JSON record into its own dataframe
df_list = []                                                                                         
with open('Data/Resources/eventData.json', 'r') as f:
    for line in f:
        df_list.append(pd.DataFrame.from_dict(json.loads(line), orient='index'))

df = (pd.concat(df_list, axis=0)                # concatenate all dataframes together
        .groupby(level=0)                       # remove duplicates 
        .last()
        .drop('url', 1)                         # drop the URL column 
        .rename({                               # rename columns for consistency with our UFO-Awesome dataset
               'SightedOn' : 'sighted_on', 
               'ReportedOn' : 'reported_on'
            }, 
            axis=1
        )
)
# --- datetime operations ---
df.duration = pd.to_timedelta(df.duration, errors='coerce')
df.sighted_on = pd.to_datetime(df.sighted_on, errors='coerce') 
df.reported_on = pd.to_datetime(df.reported_on.str.split('(').str[0], errors='coerce')

# extract relevant data from demographics column 
d_terms = ['City', 'Region', 'Coutntry', 'Latitude', 'Longitude'] 
i = df.pop('demographics').str.join(',').str.extractall(r'(?:{})(.*?)(?=,|$)'.format('|'.join(d_terms)))[0].unstack()
i.columns = ['municipality', 'state', 'country', 'latitude', 'longitude']

# extract relevant data from sighting_specifics column - mainly duration and shape
s_terms = ['Sighting Duration', 'Object Shape']
j = df.pop('sighting_specifics').str.join(',').str.extractall(r'(?:{})(.*?)(?=,|$)'.format('|'.join(s_terms)))[0].unstack()
j.columns = ['duration', 'shape']

# fix duration column in df
j['duration'] = pd.to_timedelta(j.duration, errors='coerce')
df['duration'] = df['duration'].combine_first(j['duration'])
j = j.drop('duration', axis=1)

# concatenate expanded demographics and sightings data with the original dataframe 
df = pd.concat([df, i, j], axis=1)

# cleanup string columns
df[['municipality', 'state', 'country', 'shape', 'description']] = \
df[['municipality', 'state', 'country', 'shape', 'description']].apply(lambda x: x.str.strip()) 

# remove state if the country is not the US
df['state'] = np.where(df['country'].str.startswith('United States'), df['state'], np.nan)

# --- process object and caption data from tika-docker outputs --- #
# load caption and object files
cap = pd.read_csv('Data/Resources/cap.txt', header=None, names=['url', 'caption'])
obj = pd.read_csv('Data/Resources/cap.txt', header=None, names=['url', 'object'])

# merge caption and object on URL
v = cap.merge(obj, on='url').sort_values(by='url')

# set the index to be the event ID from the url
v.index = v['url'].str.split('/', n=4).str[4].str.split('_').str[0].values

# group by eventID and collapse data into lists of columns
v = v.groupby(level=0, sort=False).agg(pd.Series.tolist)

# merge event data with obeject and caption data
df = df.merge(v, left_index=True, right_index=True, how='outer')

# save to CSV
df.to_csv('Data/ufo_stalker.csv')
'''

import os
import json
import pandas as pd

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
df['sighted_on'] = pd.to_datetime(df['sighted_on'], errors='coerce', unit='s')
df['reported_on'] = pd.to_datetime(df['reported_on'], errors='coerce', unit='s')
# fix shape column
df['shape'] = df['shape'].str.strip().str.replace(r'(?:,\s*)?N,\s*A', '').str.replace('Rectagular', 'Rectangular')
df.loc[df['shape'].str.len().eq(0), 'shape'] = np.nan
# load caption and object files
cap = pd.read_csv('Data/Resources/cap.txt', header=None, names=['url', 'caption'])
obj = pd.read_csv('Data/Resources/cap.txt', header=None, names=['url', 'object'])
# merge caption and object on URL
v = cap.merge(obj, on='url').sort_values(by='url')
# set the index to be the event ID from the url
v['event_id'] = v['url'].str.split('/', n=4).str[4].str.split('_').str[0].values
# group by eventID and collapse data into lists of columns
v = v.groupby('event_id', sort=False).agg(pd.Series.tolist).reset_index()
# merge event data with obeject and caption data
df = df.merge(v, on='event_id', how='outer')
# save to CSV
df.to_csv('Data/ufo_stalker.csv')


