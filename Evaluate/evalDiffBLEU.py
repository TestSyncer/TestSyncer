import json
import os
import difflib
from utils.formatter import format_java_code
from utils.calculator import calculate_sentencebleu

def unified_diff(str1, str2, fromfile='String1', tofile='String2'):
    str1_lines = str1.splitlines(keepends=True)
    str2_lines = str2.splitlines(keepends=True)
    diff = difflib.unified_diff(
        str1_lines, str2_lines,
        fromfile=fromfile,
        tofile=tofile,
        lineterm='',
        n=0
    )
    return ''.join(diff)

if __name__ == "__main__":
    with open(r'...\gen\test.json', 'r', encoding='utf-8') as file: # The path to test.json(test.json is provided in master branch)
        data = json.load(file)
    extracted_data = []
    for item in data:
        focal_src = item.get("focal_src")
        focal_tgt = item.get("focal_tgt")
        test_src = item.get("test_src")
        test_tgt = item.get("test_tgt")
        test_db = item.get("test_db", [])
        extracted_item = {
            "focal_src": focal_src,
            "focal_tgt": focal_tgt,
            "test_src": test_src,
            "test_tgt": test_tgt,
            "t_file_path": test_db[5]
        }
        extracted_data.append(extracted_item)

    base_path = '.../PromptAndResult' # The path to the output folder

    generated_file_name = 'SmartTestSyncer_Claude-3.5-sonnet.txt'
    # generated_file_name = 'SmartTestSyncer_GPT-4o.txt'

    # generated_file_name = 'SmartTestSyncer_without_types_Claude-3.5-sonnet.txt'
    # generated_file_name = 'SmartTestSyncer_without_dependency_Claude-3.5-sonnet.txt'
    # generated_file_name = 'SmartTestSyncer_without_caller_Claude-3.5-sonnet.txt'

    # generated_file_name = 'SynTeR_Claude-3.5-sonnet.txt'
    # generated_file_name = 'SynTeR_GPT-4o.txt'

    # generated_file_name = 'NAIVELLM_Claude-3.5-sonnet.txt'
    # generated_file_name = 'NAIVELLM_GPT-4o.txt'

    # generated_file_name = 'CEPROT.txt'

    sum_DiffBLEU = 0.0
    test_size = 0
    for i in range(1, 521):
        folder_path = base_path + "/" + str(i)
        generated_file_path = os.path.join(folder_path, generated_file_name)
        old_t_content = extracted_data[i-1]["test_src"]
        new_t_content = extracted_data[i-1]["test_tgt"]
        old_t_content = format_java_code(old_t_content)
        new_t_content = format_java_code(new_t_content)
        if os.path.isfile(generated_file_path):
            with open(generated_file_path, 'r', encoding='utf-8') as file:
                java_code = file.read()
                generated_method = format_java_code(java_code)
                change1 = unified_diff(old_t_content, new_t_content)
                change2 = unified_diff(old_t_content, generated_method)
                DiffBleu = calculate_sentencebleu(change1, change2)
                sum_DiffBLEU += DiffBleu
                test_size += 1
                print(f"{i}: diffbleu = {DiffBleu}")
        else:
            print(f"The file does not exist")

    print(f"test_size: {test_size}, average diffbleu: {sum_DiffBLEU/ test_size}.")



