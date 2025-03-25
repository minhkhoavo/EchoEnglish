import os
import tempfile
import base64
import json
import numpy as np
import librosa
import soundfile as sf
from pydub import AudioSegment
from phoneme_utils import * 

# Hàm xử lý pause (không thay đổi logic)
def adjust_pauses_for_hf_pipeline_output(pipeline_output, split_threshold=0.12):
    adjusted_chunks = pipeline_output["chunks"].copy()
    for i in range(len(adjusted_chunks) - 1):
        current_chunk = adjusted_chunks[i]
        next_chunk = adjusted_chunks[i + 1]
        current_start, current_end = current_chunk["timestamp"]
        next_start, next_end = next_chunk["timestamp"]
        pause_duration = next_start - current_end
        if pause_duration > 0:
            distribute = (split_threshold / 2) if pause_duration > split_threshold else (pause_duration / 2)
            adjusted_chunks[i]["timestamp"] = (current_start, current_end + distribute)
            adjusted_chunks[i + 1]["timestamp"] = (next_start - distribute, next_end)
    pipeline_output["chunks"] = adjusted_chunks
    return pipeline_output

class AudioHandler:
    @staticmethod
    def load_audio_file(file_path):
        audio = AudioSegment.from_file(file_path)
        audio = audio.normalize()
        audio.export(file_path, format="wav")
        return file_path

    @staticmethod
    def save_temp_file_from_bytes(audio_bytes, suffix=".wav"):
        with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as tmp:
            tmp.write(audio_bytes)
            return tmp.name

    @staticmethod
    def split_audio_by_timestamps(audio_array, sample_rate, word_timestamps, output_dir="words_output"):
        os.makedirs(output_dir, exist_ok=True)
        word_transcriptions = []
        for i, chunk in enumerate(word_timestamps["chunks"]):
            start, end = chunk["timestamp"]
            if end is None:
                end = len(audio_array) / sample_rate
            word_text = chunk["text"].strip().replace(" ", "_")
            start_sample = int(start * sample_rate)
            end_sample = int(end * sample_rate)
            word_audio = audio_array[start_sample:end_sample]
            safe_word = word_text if word_text else f"word_{i}"
            filename = os.path.join(output_dir, f"{i}_{safe_word}.wav")
            sf.write(filename, word_audio, sample_rate)
            word_transcriptions.append({
                "file": filename,
                "text": chunk["text"]
            })
        return word_transcriptions

class Transcriber:
    def __init__(self, model_manager):
        self.processor = model_manager.wav2vec_processor
        self.model = model_manager.wav2vec_model
        # device đã được khai báo trong models.py nên có thể import lại nếu cần
        from models import device
        self.device = device

    def transcribe_audio_file(self, file_path):
        audio_input, sr = librosa.load(file_path, sr=16000)
        input_values = self.processor(audio_input, sampling_rate=sr, return_tensors="pt").input_values
        input_values = input_values.to(self.device)
        import torch
        with torch.no_grad():
            logits = self.model(input_values).logits
        predicted_ids = torch.argmax(logits, dim=-1)
        transcription = self.processor.batch_decode(predicted_ids)
        return transcription[0].strip()

class TimestampExtractor:
    def __init__(self, model_manager):
        # self.pipe = model_manager.timestamp_pipe
        pass

    def extract_timestamps(self, file_path):
        audio_array, sample_rate = sf.read(file_path)
        if len(audio_array.shape) > 1:
            audio_array = np.mean(audio_array, axis=1)
        sample = {"array": audio_array, "sampling_rate": sample_rate}
        hf_pipeline_output = self.pipe(sample)
        adjusted_result = adjust_pauses_for_hf_pipeline_output(hf_pipeline_output)
        return adjusted_result, audio_array, sample_rate
