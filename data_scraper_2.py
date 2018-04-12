import os
import time

from selenium import webdriver
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.chrome.options import Options

def write_to_file(filepath, data):
    with open(filepath, 'w', encoding='utf8') as f:
        f.write(str(data) + '\n')


executable_path = "Data/Resources/chromedriver"
os.environ["webdriver.chrome.driver"] = executable_path
chrome_options = Options()
proxy_iterator = 0
driver = webdriver.Chrome(executable_path=executable_path, chrome_options=chrome_options)

driver.wait = WebDriverWait(driver, 60)

url = 'http://www.ufostalker.com/'
driver.get(url)
time.sleep(3)

if not os.path.exists('Data/Resources/ufo-stalker-json'):
    os.mkdir('Data/Resources/ufo-stalker-json')

for i in range(1, 2760):
    try:
        pagination_scope = driver.execute_script('window.pagination = angular.element(document.querySelector(".pagination")).scope();')
        driver.execute_script('window.pagination.setCurrent(' + str(i) +');window.pagination.onPageChange()')
        time.sleep(4)
        scope = driver.execute_script('window.scope = angular.element(document.querySelector(".event-table")).scope()')
        driver.execute_script('window.scope.$apply();')
        events = driver.execute_script("return JSON.stringify(window.scope.events)")
        outfilename = 'Data/Resources/ufo-stalker-json/page_no_' + str(i)
        write_to_file('Data/Resources/ufo-stalker-json/page_no_' + str(i) + '.json', events)
        print(str(i))
    except Exception as e:
        print(str(e))

driver.close()