import os
import json
from utils.formatter import format_code_to_single_line

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

    same_num = 0
    test_size = 0
    for i in range(1, 521):
        folder_path = base_path + "/" + str(i)
        generated_file_path = os.path.join(folder_path, generated_file_name)
        old_t_content = extracted_data[i-1]["test_src"]
        new_t_content = extracted_data[i-1]["test_tgt"]
        old_t_content = format_code_to_single_line(old_t_content)
        new_t_content = format_code_to_single_line(new_t_content)
        if os.path.isfile(generated_file_path):
            with open(generated_file_path, 'r', encoding='utf-8') as file:
                java_code = file.read()
                generated_method = format_code_to_single_line(java_code)
                if new_t_content == generated_method:
                    value = 1
                    same_num += 1
                    print(f"{i}: identical")
                test_size += 1
        else:
            print(f"The file does not exist")

    print(f"test_size: {test_size}, accuracy: {same_num/test_size}.")