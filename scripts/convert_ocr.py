import re
import json
import sys
import os

input_path = r"src\test\resources\kiosks\commodity\texts\arc-l2-sell-1.txt"
output_path = r"src\test\resources\kiosks\commodity\jsons\arc-l2-sell-1.json"

pattern = re.compile(r"ppocr INFO: \[\[\[([\d.]+),\s*([\d.]+)\],\s*\[([\d.]+),\s*[\d.]+\],\s*\[[\d.]+,\s*([\d.]+)\],")

data = []
if not os.path.exists(input_path):
    print(f"Error: Input file NOT found at {input_path}")
    # try absolute path based on CWD
    input_path = os.path.abspath(input_path)
    print(f"Trying {input_path}")
    if not os.path.exists(input_path):
        print(f"Error: Input file still not found at {input_path}")
        sys.exit(1)

with open(input_path, 'r', encoding='utf-8') as f:
    for line in f:
        match = pattern.search(line)
        if match:
            x1 = float(match.group(1))
            y1 = float(match.group(2))
            x2 = float(match.group(3))
            y3 = float(match.group(4))
            
            part_after_coords = line.split("]], ('")[1]
            text = part_after_coords.rsplit("',", 1)[0]
            
            data.append({
                "Text": text,
                "X": x1,
                "Y": y1,
                "Width": x2 - x1,
                "Height": y3 - y1
            })

os.makedirs(os.path.dirname(output_path), exist_ok=True)

with open(output_path, 'w', encoding='utf-8') as f:
    json.dump(data, f, indent=2)

print(f"converted {len(data)} lines to {output_path}")
