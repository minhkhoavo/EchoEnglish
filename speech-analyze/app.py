# app.py
import os
import json
import base64
import tempfile
from flask import Flask, request, render_template_string, jsonify
import math
import warnings
import numpy as np
import librosa
import soundfile as sf
import uuid 
import threading 
import time
from phoneme_utils import *
from models import ModelManager
from analysis_controller import APIController
from audio_utils import AudioHandler
from analysis import *

app = Flask(__name__)
HTML_TEMPLATE = '<h1>OK</h1>'

# Khởi tạo ModelManager và APIController
LOCAL_MODEL_PATH = "./local_model"
TIMESTAMP_MODEL_ID = "nyrahealth/CrisperWhisper" 
# TOKEN = "<Hugging Face Token>"
TOKEN = "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"
RESULTS_DIR = "./results_cache" 
POLLING_TIMEOUT = 25 
CHECK_INTERVAL = 2 
os.makedirs(RESULTS_DIR, exist_ok=True)


model_manager = ModelManager(LOCAL_MODEL_PATH, TIMESTAMP_MODEL_ID, TOKEN)
api_controller = APIController(model_manager)

def process_sentence_background(audio_tmp_path, target_word, result_file_path):
    """Hàm chạy trong luồng nền để xử lý câu."""
    try:
        result_data = api_controller.process_sentence(audio_tmp_path, target_word)
        result_payload = {"status": "completed", "result": result_data}
        with open(result_file_path, 'w', encoding='utf-8') as f:
            json.dump(result_payload, f, ensure_ascii=False, indent=4)
    except Exception as e:
        error_payload = {"status": "error", "error": str(e)}
        try:
            with open(result_file_path, 'w', encoding='utf-8') as f:
                json.dump(error_payload, f, ensure_ascii=False, indent=4)
        except Exception as write_e:
            # Ghi log lỗi nếu không thể ghi file trạng thái lỗi
            print(f"CRITICAL: Failed to write error status to {result_file_path}: {write_e}. Original error: {e}")
    finally:
        # Luôn xóa file audio tạm sau khi xử lý xong
        if os.path.exists(audio_tmp_path):
            try:
                os.remove(audio_tmp_path)
            except Exception as remove_e:
                 print(f"Error removing temporary audio file {audio_tmp_path}: {remove_e}")


# ---- API Endpoints ----

@app.route('/api/transcribe', methods=['POST'])
def transcribe():
    target_word = request.form.get('target_word', '').strip()
    if not target_word:
        return jsonify({"error": "Please enter target word"}), 400

    audio_type = request.form.get('audio_type', 'single')
    tmp_path = None

    try:
        if 'audio_file' in request.files and request.files['audio_file'].filename != '':
            audio_file = request.files['audio_file']
            with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
                audio_file.save(tmp.name)
                tmp_path = tmp.name
            if os.path.getsize(tmp_path) == 0:
                return jsonify({"error": "Audio file is empty"}), 400
        elif 'audio_blob' in request.form and request.form['audio_blob'] != '':
            audio_data = request.form['audio_blob']
            header, encoded = audio_data.split(',', 1)
            audio_bytes = base64.b64decode(encoded)
            tmp_path = AudioHandler.save_temp_file_from_bytes(audio_bytes, suffix=".wav")
        else:
            return jsonify({"error": "Cannot recognize audio source"}), 400

        if audio_type == 'single':
            result = api_controller.process_single_word(tmp_path, target_word)
            os.remove(tmp_path) 
            return jsonify(result)

        elif audio_type == 'sentence':
            task_id = str(uuid.uuid4())
            result_file_path = os.path.join(RESULTS_DIR, f"{task_id}.json")

            initial_status = {"status": "processing"}
            with open(result_file_path, 'w', encoding='utf-8') as f:
                json.dump(initial_status, f, ensure_ascii=False, indent=4)

            thread = threading.Thread(target=process_sentence_background,
                                      args=(tmp_path, target_word, result_file_path),
                                      daemon=True)
            thread.start()

            return jsonify({"task_id": task_id, "status": "processing"}), 202

        else:
            os.remove(tmp_path)
            return jsonify({"error": "audio_type not valid"}), 400

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/result/<task_id>', methods=['GET'])
def get_result(task_id):
    try:
        uuid.UUID(task_id, version=4)
    except ValueError:
        return jsonify({"error": "Invalid task ID format"}), 400

    result_file_path = os.path.join(RESULTS_DIR, f"{task_id}.json")

    if not os.path.exists(result_file_path):
        return jsonify({"error": "Task ID not found or expired"}), 404

    start_time = time.time()
    while time.time() - start_time < POLLING_TIMEOUT:
        try:
            with open(result_file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)

            status = data.get('status')
            if status in ['completed', 'error']:
                os.remove(result_file_path)
                return jsonify(data)
            elif status == 'processing':
                time.sleep(CHECK_INTERVAL)
                continue 
            else:
                return jsonify({"status": "error", "error": "Unknown task status encountered"}), 500

        except FileNotFoundError:
            return jsonify({"error": "Task result file vanished unexpectedly"}), 404
        except json.JSONDecodeError:
            time.sleep(CHECK_INTERVAL / 2)
            continue
        except Exception as e:
            return jsonify({"status": "error", "error": f"Failed to read result file: {e}"}), 500

    return jsonify({"status": "processing"})


@app.route('/')
def index():
    return "API Server is running."

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, threaded=True)
