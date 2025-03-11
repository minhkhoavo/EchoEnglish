from flask import Flask, request, render_template_string, jsonify
import os
import torch
import librosa
import base64
import tempfile
import editdistance
import eng_to_ipa as ipa
import wave
import json
import torchaudio
import torchaudio.transforms as T
import soundfile as sf
from transformers import Wav2Vec2Processor, Wav2Vec2ForCTC
from vosk import Model as VoskModel, KaldiRecognizer
from pydub import AudioSegment
import subprocess

# Thiết lập chế độ offline
os.environ["HF_HUB_OFFLINE"] = "1"
os.environ["HF_DATASETS_OFFLINE"] = "1"

# Load model và processor từ thư mục cục bộ
processor = Wav2Vec2Processor.from_pretrained("./local_model", local_files_only=True)
model = Wav2Vec2ForCTC.from_pretrained("./local_model", local_files_only=True)
model.eval()

app = Flask(__name__)

# HTML giao diện (giữ nguyên để tương thích, nhưng không bắt buộc nếu chỉ dùng API)
HTML_TEMPLATE = '''...'''  # Giữ nguyên HTML_TEMPLATE từ code gốc

# Hàm loại bỏ dấu trọng âm từ IPA
def remove_stress_marks(ipa_str):
    ipa_str = ipa_str.strip('/')
    return ipa_str.replace('ˈ', '').replace('ˌ', '')

phonemes = ['b', 'd', 'f', 'g', 'h', 'dʒ', 'k', 'l', 'm', 'n', 'ŋ', 'p',
        'r', 's', 't', 'v', 'w', 'z', 'ʒ', 'tʃ', 'θ', 'ð', 'j', 'ʃ', 'æ', 'eɪ', 
        'ɛ', 'i:', 'ɪ', 'aɪ', 'ɒ', 'oʊ', 'ʊ', 'ʌ', 'u:', 'ɔɪ', 'aʊ', 'ə', 'eəʳ',
        'ɑ:', 'ɜ:ʳ', 'ɔ:', 'ɪəʳ', 'ʊəʳ']


def split_ipa(ipa_str):
    ipa_str = ipa_str.strip('/').replace('ˈ', '').replace('ˌ', '')  # Remove slashes and stress marks
    result = []
    i = 0
    while i < len(ipa_str):
        # Kiểm tra âm ba
        if i + 2 < len(ipa_str) and ipa_str[i:i+3] in phonemes:
            result.append(ipa_str[i:i+2])
            i += 3
        # Kiểm tra âm đôi
        elif i + 1 < len(ipa_str) and ipa_str[i:i+2] in phonemes:
            result.append(ipa_str[i:i+2])
            i += 2
        # Kiểm tra âm vị đơn
        elif ipa_str[i] in phonemes:
            result.append(ipa_str[i])
            i += 1
        else:
            i += 1  # Bỏ qua ký tự không nhận dạng được
    return result

# Recognizing audio of a single word
def transcribe_audio_file(file_path):
    audio_input, sr = librosa.load(file_path, sr=16000)
    input_values = processor(audio_input, sampling_rate=sr, return_tensors="pt").input_values
    with torch.no_grad():
        logits = model(input_values).logits
    predicted_ids = torch.argmax(logits, dim=-1)
    transcription = processor.batch_decode(predicted_ids)
    return transcription[0]

# Hàm xử lý audio kiểu sentence: tách từng từ và nhận dạng
def process_sentence_audio(file_path):
    converted_path = file_path + "_converted.wav"
    subprocess.run(["ffmpeg", "-y", "-i", file_path, converted_path], check=True)
    audio = AudioSegment.from_file(converted_path, format="wav")
    wf = wave.open(converted_path, "rb")
    vosk_model = VoskModel("./local_model/vosk-model")
    rec = KaldiRecognizer(vosk_model, wf.getframerate())
    rec.SetWords(True)

    word_timestamps = []
    while True:
        data = wf.readframes(4000)
        if len(data) == 0:
            break
        if rec.AcceptWaveform(data):
            result = json.loads(rec.Result())
            if "result" in result:
                word_timestamps.extend(result["result"])
    final_result = json.loads(rec.FinalResult())
    if "result" in final_result:
        word_timestamps.extend(final_result["result"])

    word_transcriptions = []
    for idx, word_info in enumerate(word_timestamps):
        start_ms = int(word_info["start"] * 1000)
        end_ms = int(word_info["end"] * 1000)
        word_audio = audio[start_ms:end_ms]
        temp_filename = f"temp_word_{idx}.wav"
        word_audio.export(temp_filename, format="wav")
        transcription = transcribe_audio_file(temp_filename)
        word_transcriptions.append({"word": word_info["word"], "transcription": transcription})
        os.remove(temp_filename)

    os.remove(converted_path)
    return word_transcriptions

# Hàm tính độ tương đồng
def calculate_similarity(transcription, target_ipa):
    transcription_no_stress = remove_stress_marks(transcription)
    target_ipa_no_stress = remove_stress_marks(target_ipa)
    distance = editdistance.eval(transcription_no_stress.replace(" ", ""), target_ipa_no_stress.replace(" ", ""))
    max_len = max(len(transcription_no_stress), len(target_ipa_no_stress))
    return 1 - (distance / max_len) if max_len > 0 else 1.0

# Endpoint chính: /api/transcribe
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
    # Xử lý audio từ ghi âm (base64)
    elif 'audio_blob' in request.form and request.form['audio_blob'] != '':
        audio_data = request.form['audio_blob']
        header, encoded = audio_data.split(',', 1)
        audio_bytes = base64.b64decode(encoded)
        with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
            tmp.write(audio_bytes)
            tmp_path = tmp.name
    else:
        return jsonify({"error": "Cannot recognize audio"}), 400

    try:
        if audio_type == 'single':
            transcription = transcribe_audio_file(tmp_path)
            target_ipa = ipa.convert(target_word)
            if target_ipa.startswith('*') and target_ipa.endswith('*'):
                return jsonify({"error": f"IPA not found for '{target_word}'"}), 400

            similarity = calculate_similarity(transcription, target_ipa)
            result = {
                "target_word": target_word,
                "target_ipa": target_ipa,
                "target_ipa_no_stress": split_ipa(target_ipa),
                "transcription": transcription,
                "transcription_no_stress": split_ipa(transcription),
                "similarity": similarity
            }
        elif audio_type == 'sentence':
            target_words = target_word.split()
            word_transcriptions = process_sentence_audio(tmp_path)
            words_result = []
            for i, target in enumerate(target_words):
                target_ipa = ipa.convert(target)
                if i < len(word_transcriptions):
                    transcription = word_transcriptions[i]["transcription"]
                    similarity = calculate_similarity(transcription, target_ipa)
                    words_result.append({
                        "word": target,
                        "target_ipa": target_ipa,
                        "target_ipa_no_stress": split_ipa(target_ipa),
                        "transcription": transcription,
                        "transcription_no_stress": split_ipa(transcription),
                        "similarity": similarity
                    })
                else:
                    words_result.append({
                        "word": target,
                        "target_ipa": target_ipa,
                        "target_ipa_no_stress": split_ipa(target_ipa),
                        "transcription": "",
                        "transcription_no_stress": "",
                        "similarity": 0.0
                    })

            # Xử lý trường hợp audio có nhiều từ hơn target
            for i in range(len(target_words), len(word_transcriptions)):
                words_result.append({
                    "word": word_transcriptions[i]["word"],
                    "target_ipa": "",
                    "target_ipa_no_stress": "",
                    "transcription": word_transcriptions[i]["transcription"],
                    "transcription_no_stress": split_ipa(word_transcriptions[i]["transcription"]),
                    "similarity": 0.0
                })

            overall_similarity = sum(w["similarity"] for w in words_result) / len(target_words) if target_words else 0.0
            result = {
                "target_sentence": target_word,
                "words": words_result,
                "overall_similarity": overall_similarity
            }
        else:
            return jsonify({"error": "audio_type not valid"}), 400

        os.remove(tmp_path)
        return jsonify(result)

    except Exception as e:
        os.remove(tmp_path)
        return jsonify({"error": str(e)}), 500

# Giữ route gốc để tương thích giao diện HTML (tùy chọn)
@app.route('/')
def index():
    return render_template_string(HTML_TEMPLATE)

@app.route('/upload', methods=['POST'])
def upload():
    # Có thể giữ lại để tương thích giao diện HTML, nhưng không bắt buộc
    pass

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)