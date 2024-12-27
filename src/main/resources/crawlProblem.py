from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager
import time
import json
import sys
import io


def searchProblem(userId, problemNumber, targetSubmissionId):


    # ChromeOptions 객체 생성 및 헤드리스 모드 설정
    options = Options()
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--disable-gpu")
    options.add_argument("--window-size=1920,1080")  # 화면 크기 설정
    options.add_argument("--disable-blink-features=AutomationControlled")  # 자동화 제어 방지
    options.add_argument("headless")  # 헤드리스 모드 활성화
    options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.20 Safari/537.36");

    # Web Driver 설정
    service = Service(ChromeDriverManager().install())
    driver = webdriver.Chrome(service=service, options=options)


    def searchProblem(userId, problemNumber, targetSubmissionId):
        options = Options()
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--disable-gpu")
    options.add_argument("--window-size=1920,1080")
    options.add_argument("--disable-blink-features=AutomationControlled")
    options.add_argument("headless")
    options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.20 Safari/537.36")

    service = Service(ChromeDriverManager().install())
    driver = webdriver.Chrome(service=service, options=options)

    try:
        driver.get('https://www.acmicpc.net/status')

        WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, '/html/body/div[2]/div[2]/div/div[4]/div/form/input[1]')))

        searchBox = driver.find_element(By.XPATH, '/html/body/div[2]/div[2]/div/div[4]/div/form/input[1]')
        searchBox.send_keys(problemNumber)

        searchBoxId = driver.find_element(By.XPATH, '/html/body/div[2]/div[2]/div/div[4]/div/form/input[2]')
        searchBoxId.send_keys(userId)
        searchBox.send_keys(Keys.RETURN)

        WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.CSS_SELECTOR, 'tbody tr')))

        rows = driver.find_elements(By.CSS_SELECTOR, 'tbody tr')
        result_json = {
            'submissionId': targetSubmissionId,
            'result': '제출 기록이 없습니다.',
            'memory': '',
            'time': '',
            'codeType': '',
            'codeLength': ''
        }

        for row in rows:
            submissionId = row.find_element(By.CSS_SELECTOR, 'td:nth-child(1)').text
            if submissionId == targetSubmissionId:
                result_json = {
                    'submissionId': submissionId,
                    'result': row.find_element(By.CSS_SELECTOR, 'td:nth-child(4)').text,
                    'memory': row.find_element(By.CSS_SELECTOR, 'td:nth-child(5)').text,
                    'time': row.find_element(By.CSS_SELECTOR, 'td:nth-child(6)').text,
                    'codeType': row.find_element(By.CSS_SELECTOR, 'td:nth-child(7)').text,
                    'codeLength': row.find_element(By.CSS_SELECTOR, 'td:nth-child(8)').text
                }
                break

        print(json.dumps(result_json, ensure_ascii=False, indent=4))
    finally:
        driver.quit()
if __name__ == "__main__":
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    userId = sys.argv[1]
    problemNumber = sys.argv[2]
    targetSubmissionId = sys.argv[3]
    searchProblem(userId, problemNumber, targetSubmissionId)