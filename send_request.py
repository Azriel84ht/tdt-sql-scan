import json
import requests

with open('test_full.bteq', 'r') as f:
    script_content = f.read()

payload = {
    "script_content": script_content
}

try:
    response = requests.post(
        "http://localhost:8080/api/v1/parse/poc",
        headers={"Content-Type": "application/json"},
        data=json.dumps(payload)
    )
    print(f"Status Code: {response.status_code}")
    print("Response Text:")
    print(response.text)
    if response.status_code == 200:
        print("\nResponse JSON:")
        # Pretty print the JSON
        parsed_json = json.loads(response.text)
        print(json.dumps(parsed_json, indent=4))

except requests.exceptions.ConnectionError as e:
    print(f"Connection Error: {e}")
except Exception as e:
    print(f"An error occurred: {e}")
