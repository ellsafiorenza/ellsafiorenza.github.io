from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.common.by import By

options = Options()
options.add_argument('--no-sandbox')
#options.binary_location = r'/Users/ellsafiorenza/Downloads/Chrome.app'
driver = webdriver.Chrome(chrome_options = options, executable_path='/Users/ellsafiorenza/Downloads/chromedriver')
driver.get("https://www.tomtom.com/en_gb/traffic-index/jakarta-traffic/")

# proses hover
daily_congenstion = driver.find_elements_by_tag_name('circle')

for daily in daily_congenstion:
    print(daily.text)
    hover = ActionChains(driver).move_to_element(daily)
    hover.perform()