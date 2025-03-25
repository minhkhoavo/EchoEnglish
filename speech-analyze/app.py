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
TOKEN = "<Hugging Face Token>"

model_manager = ModelManager(LOCAL_MODEL_PATH, TIMESTAMP_MODEL_ID, TOKEN)
api_controller = APIController(model_manager)

@app.route('/api/transcribe', methods=['POST'])
def transcribe():
    target_word = request.form.get('target_word', '').strip()
    if not target_word:
        return jsonify({"error": "Please enter target word"}), 400

    audio_type = request.form.get('audio_type', 'single')

    # Xử lý audio từ file upload
    if 'audio_file' in request.files and request.files['audio_file'].filename != '':
        audio_file = request.files['audio_file']
        with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
            audio_file.save(tmp.name)
            tmp_path = tmp.name
        if os.path.getsize(tmp_path) == 0:
            return jsonify({"error": "Audio file is empty"}), 400
    # Xử lý audio từ ghi âm (base64)
    elif 'audio_blob' in request.form and request.form['audio_blob'] != '':
        audio_data = request.form['audio_blob']
        header, encoded = audio_data.split(',', 1)
        audio_bytes = base64.b64decode(encoded)
        tmp_path = AudioHandler.save_temp_file_from_bytes(audio_bytes, suffix=".wav")
    else:
        return jsonify({"error": "Cannot recognize audio"}), 400

    try:
        if audio_type == 'single':
            result = api_controller.process_single_word(tmp_path, target_word)
        elif audio_type == 'sentence':
            result = api_controller.process_sentence(tmp_path, target_word)
        else:
            os.remove(tmp_path)
            return jsonify({"error": "audio_type not valid"}), 400

        os.remove(tmp_path)
        with open('result.json', 'w', encoding='utf-8') as file:
            json.dump(result, file, ensure_ascii=False, indent=4)
        return jsonify(result)
    except Exception as e:
        if os.path.exists(tmp_path):
            os.remove(tmp_path)
        return jsonify({"error": str(e)}), 500

@app.route('/')
def index():
    return render_template_string(HTML_TEMPLATE)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
