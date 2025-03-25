import torch
from transformers import (
    Wav2Vec2ForCTC,
    Wav2Vec2Processor,
    AutoProcessor,
    AutoModelForSpeechSeq2Seq,
    pipeline
)

# Cấu hình thiết bị và kiểu dữ liệu
device = "cuda:0" if torch.cuda.is_available() else "cpu"
torch_dtype = torch.float16 if torch.cuda.is_available() else torch.float32

class ModelManager:
    def __init__(self, local_model_path, timestamp_model_id, token):
        # Load model nhận dạng từ (Wav2Vec2)
        self.wav2vec_processor = Wav2Vec2Processor.from_pretrained("./local_model", local_files_only=True)
        self.wav2vec_model = Wav2Vec2ForCTC.from_pretrained("./local_model", local_files_only=True)
        self.wav2vec_model.eval()
        self.wav2vec_model.to(device)
        
        # Load model timestamp (CrisperWhisper)
        # self.timestamp_processor = AutoProcessor.from_pretrained(timestamp_model_id, token=token)
        # self.timestamp_model = AutoModelForSpeechSeq2Seq.from_pretrained(
        #     timestamp_model_id,
        #     torch_dtype=torch_dtype,
        #     low_cpu_mem_usage=True,
        #     use_safetensors=True,
        #     token=token
        # )
        # self.timestamp_model.to(device)
        # # Tạo pipeline cho timestamp
        # self.timestamp_pipe = pipeline(
        #     "automatic-speech-recognition",
        #     model=self.timestamp_model,
        #     tokenizer=self.timestamp_processor.tokenizer,
        #     feature_extractor=self.timestamp_processor.feature_extractor,
        #     chunk_length_s=20,
        #     batch_size=12,
        #     generate_kwargs={
        #         "num_beams": 5,
        #         "temperature": 0.0,
        #         "language": "en",
        #     },
        #     return_timestamps='word',
        #     torch_dtype=torch_dtype,
        #     device=0 if device.startswith("cuda") else -1
        # )
