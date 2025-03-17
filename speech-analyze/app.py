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
import whisper_timestamped as whisper
import soundfile as sf
from transformers import Wav2Vec2Processor, Wav2Vec2ForCTC
from vosk import Model as VoskModel, KaldiRecognizer
from pydub import AudioSegment
import subprocess
import numpy as np
from phoneme_utils import *

# Thiết lập chế độ offline
os.environ["HF_HUB_OFFLINE"] = "1"
os.environ["HF_DATASETS_OFFLINE"] = "1"

# Load model và processor từ thư mục cục bộ
processor = Wav2Vec2Processor.from_pretrained("./local_model", local_files_only=True)
model = Wav2Vec2ForCTC.from_pretrained("./local_model", local_files_only=True)
model.eval()

app = Flask(__name__)

HTML_TEMPLATE = '<h1>OK</h1>'  


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
    import subprocess
    import numpy as np
    from pydub import AudioSegment
    import whisper_timestamped as whisper
    audio = AudioSegment.from_file(file_path)
    audio = audio.normalize()  # Chuẩn hóa âm lượng
    audio.export(file_path, format="wav")  # Ghi đè file đã chuẩn hóa

    # Chuyển đổi file gốc sang định dạng WAV nếu cần
    converted_path = file_path + "_converted.wav"

    subprocess.run(["ffmpeg", "-y", "-i", file_path, converted_path], check=True)
    
    # Load audio bằng whisper-timestamped (trả về numpy array, resample về 16kHz)
    audio = whisper.load_audio(converted_path)
    sample_rate = 16000  # mặc định của whisper-timestamped

    # Load model (ở đây dùng "tiny", có thể thay thành "base" nếu máy đủ mạnh)
    model = whisper.load_model("medium", device="cpu")
    result = whisper.transcribe(model, audio, language="en", beam_size=5, best_of=5)    
    # Trích xuất word-level timestamps từ kết quả (từ key "words" của các segment)
    word_timestamps = []
    for segment in result.get("segments", []):
        for word in segment.get("words", []):
            word_timestamps.append(word)

    word_transcriptions = []
    # Load lại file audio convert dùng pydub để cắt đoạn dựa trên timestamp
    audio_segment = AudioSegment.from_file(converted_path, format="wav")
    
    # Duyệt qua từng từ, cắt đoạn audio dựa trên timestamp và lưu tạm ra file WAV
    for idx, word_info in enumerate(word_timestamps):
        start_ms = int(word_info["start"] * 1000)
        end_ms = int(word_info["end"] * 1000)
        word_audio = audio_segment[start_ms:end_ms]
        temp_filename = f"temp_word_{idx}.wav"
        word_audio.export(temp_filename, format="wav")
        
        # Kiểm tra đoạn audio (chuyển sang numpy để xác định số mẫu)
        samples = np.array(word_audio.get_array_of_samples())
        if word_audio.channels > 1:
            samples = samples.reshape((-1, word_audio.channels)).mean(axis=1)
        if len(samples) < 320:
            print(f"Bỏ qua từ '{word_info['text']}' vì audio quá ngắn.")
            continue
        
        # Ở đây, transcription đã có sẵn từ kết quả của whisper-timestamped
        transcription = transcribe_audio_file(temp_filename)
        word_transcriptions.append({"word": word_info["text"], "transcription": transcription})
        # os.remove(temp_filename)  # nếu muốn xóa file tạm sau khi sử dụng

    os.remove(converted_path)  # xóa file convert nếu không cần thiết nữa
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

        if os.path.getsize(tmp_path) == 0:
            return jsonify({"error": "Audio file is empty"}), 400
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
                "transcription_ipa": transcription,
                "transcription_no_stress": split_ipa(transcription),
                "similarity": similarity
                # "graphene_ipa_mapping": map_text_to_ipa(target_word)
            }
        elif audio_type == 'sentence':
            target_words = target_word.split()
            word_transcriptions = process_sentence_audio(tmp_path)
            print(word_transcriptions)
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
                        # "graphene_ipa_mapping": map_text_to_ipa(target_word)
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
        with open('result.json', 'w', encoding='utf-8') as file:
            json.dump(result, file, ensure_ascii=False, indent=4)

        return jsonify(result)

    except Exception as e:
        os.remove(tmp_path)
        return jsonify({"error": str(e)}), 500

# Giữ route gốc để tương thích giao diện HTML (tùy chọn)
@app.route('/')
def index():
    return render_template_string(HTML_TEMPLATE)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)