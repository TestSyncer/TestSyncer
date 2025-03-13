import requests
import re
import json
import os
import zipfile

output_folder = "...\\Projects" # The path to the Projects folder(Projects folder is provided in master branch)
json_path = "...\\test.json" # The path to test.json(test.json is provided in master branch)

with open(json_path, 'r', encoding='utf-8') as file:
    data = json.load(file)
extracted_data = []
for item in data:
    focal_db = item.get("focal_db", [])
    test_db = item.get("test_db", [])
    focal_src = item.get("focal_src", [])
    test_src = item.get("test_src", [])
    if len(focal_db) >= 6 and len(test_db) >= 6 :
        extracted_item = {
            "repo_name": focal_db[1],
            "pro_commit_id": focal_db[3],
            "test_commit_id": test_db[3],
            "test_path": test_db[5],
            "focal_path": focal_db[5],
            "production_method": focal_src.split('{')[0],
            "test_case": test_src.split('{')[0]
        }
        extracted_data.append(extracted_item)

def download_github_zip(url, output_path):
    try:
        pattern = r"github\.com/([^/]+)/([^/]+)/tree/([^/]+)"
        match = re.search(pattern, url)
        if not match:
            raise ValueError("Invalid GitHub tree URL format")
        user, repo, commit = match.groups()
        zip_url = f"https://github.com/{user}/{repo}/archive/{commit}.zip"
        response = requests.get(zip_url, stream=True)
        response.raise_for_status()
        os.makedirs(os.path.dirname(output_path), exist_ok=True)
        with open(output_path, 'wb') as f:
            for chunk in response.iter_content(chunk_size=8192):
                f.write(chunk)
        print(f"The file has been downloaded to: {output_path}")
        return output_path
    except requests.exceptions.RequestException as e:
        print(f"Download Failure: {e}")
    except Exception as e:
        print(f"An error occurred: {e}")

def extract_zip(zip_path, extract_to):
    try:
        with zipfile.ZipFile(zip_path, 'r') as zip_ref:
            zip_ref.extractall(extract_to)
        print(f"The file has been unzipped to: {extract_to}")
    except zipfile.BadZipFile:
        print(f"File {zip_path} is not a valid ZIP file")
    except Exception as e:
        print(f"Unzip failed: {e}")

if __name__ == "__main__":
    for i in range(1, 521):
        repo_name = extracted_data[i - 1]["repo_name"]
        commit_id = extracted_data[i - 1]["test_commit_id"]
        old_url = f"https://github.com/{repo_name}/tree/{commit_id}^"
        new_url = f"https://github.com/{repo_name}/tree/{commit_id}"
        pattern = r"github\.com/([^/]+)/([^/]+)/tree/([^/]+)"
        match = re.search(pattern, new_url)
        if not match:
            raise ValueError("Invalid GitHub tree URL format")
        user, repo, commit = match.groups()
        old_output_dir = f"{output_folder}\\{i}\\old\\{user}.zip"
        new_output_dir = f"{output_folder}\\{i}\\new\\{user}.zip"
        if not os.path.exists(f"{output_folder}\\{i}\\old\\{user}"):
            download_github_zip(old_url, old_output_dir)
        if not os.path.exists(f"{output_folder}\\{i}\\new\\{user}"):
            download_github_zip(new_url, new_output_dir)
        if not os.path.exists(f"{output_folder}\\{i}\\old\\{user}") and os.path.isfile(old_output_dir):
            extract_zip(old_output_dir, f"{output_folder}\\{i}\\old\\{user}")
        if not os.path.exists(f"{output_folder}\\{i}\\new\\{user}") and os.path.isfile(new_output_dir):
            extract_zip(new_output_dir, f"{output_folder}\\{i}\\new\\{user}")
        if os.path.exists(f"{output_folder}\\{i}\\old\\{user}"):
            for folder_name in os.listdir(f"{output_folder}\\{i}\\old\\{user}"):
                folder_path = os.path.join(f"{output_folder}\\{i}\\old\\{user}", folder_name)
                if os.path.isdir(folder_path) and folder_name.startswith(repo):
                    new_folder_path = os.path.join(f"{output_folder}\\{i}\\old\\{user}", repo)
                    if not os.path.exists(new_folder_path):
                        os.rename(folder_path, new_folder_path)
                        print(f'Renamed: "{folder_path}" -> "{new_folder_path}"')
        if os.path.exists(f"{output_folder}\\{i}\\new\\{user}"):
            for folder_name in os.listdir(f"{output_folder}\\{i}\\new\\{user}"):
                folder_path = os.path.join(f"{output_folder}\\{i}\\new\\{user}", folder_name)
                if os.path.isdir(folder_path) and folder_name.startswith(repo):
                    new_folder_path = os.path.join(f"{output_folder}\\{i}\\new\\{user}", repo)
                    if not os.path.exists(new_folder_path):
                        os.rename(folder_path, new_folder_path)
                        print(f'Renamed: "{folder_path}" -> "{new_folder_path}"')
        if os.path.exists(f"{output_folder}\\{i}\\old\\{user}.zip"):
            os.remove(f"{output_folder}\\{i}\\old\\{user}.zip")
        if os.path.exists(f"{output_folder}\\{i}\\new\\{user}.zip"):
            os.remove(f"{output_folder}\\{i}\\new\\{user}.zip")