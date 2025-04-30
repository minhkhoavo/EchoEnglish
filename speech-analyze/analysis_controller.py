from feature_analyzer import *
from phoneme_utils import *
import math

class ResultFormatter:
    @staticmethod
    def safe_round(value, ndigits):
        if value is None or not isinstance(value, (int, float)):
            return None
        if math.isnan(value):
            return None
        return round(value, ndigits)

    def format_output(self, transcription, analysis_results, audio_duration, pronunciation):
        filter_words = {"[UM]", "[UH]"}
        chunks_output = []
        prev_word = None
        analyzed_count = 0
        word_freq = {}
        for i, res in enumerate(analysis_results):
            word = res['word']
            chunk_dict = {
                "text": word,
                "start_time": round(res['start_time'], 2),
                "end_time": round(res['end_time'], 2)
            }
            if word in filter_words:
                chunk_dict["error"] = "filter_word"
                chunk_dict["analysis"] = {}
                chunks_output.append(chunk_dict)
                continue
            error_flag = "none"
            if prev_word is not None and word.lower() == prev_word.lower():
                error_flag = "duplicated"
            else:
                analyzed_count += 1
                lw = word.lower()
                word_freq[lw] = word_freq.get(lw, 0) + 1

            stress_score = res.get('stress_score', 0)
            if stress_score > 0.75:
                stress_level = "high"
            elif stress_score >= 0.6:
                stress_level = "low"
            else:
                stress_level = "none"
            chunk_dict["analysis"] = {
                "pitch": self.safe_round(res['avg_pitch'], 2),
                "intensity": self.safe_round(res['avg_intensity'], 4),
                "stress_level": stress_level,
                "variation": self.safe_round(res['avg_pitch_variation'], 2)
            }
            chunk_dict["error"] = error_flag
            chunk_dict["pronunciation"] = pronunciation[i]
            chunks_output.append(chunk_dict)
            prev_word = word

        speaking_rate = (analyzed_count / audio_duration * 60) if audio_duration > 0 else 0
        filter_word_count = sum(1 for w in transcription.split() if w in filter_words)
        word_freq_list = [{"word": w, "count": count} for w, count in word_freq.items()]
        output = {
            "text": transcription,
            "chunks": chunks_output,
            "summary": {
                "total_duration": round(audio_duration, 2),
                "word_count": analyzed_count,
                "speaking_rate_wpm": round(speaking_rate, 2),
                "filter_word_count": filter_word_count,
                "word_freq": word_freq_list
            }
        }
        return output

# APIController sử dụng các lớp ở trên; lưu ý import từ audio_utils cho AudioHandler, Transcriber, TimestampExtractor
from audio_utils import AudioHandler, Transcriber, TimestampExtractor

class APIController:
    def __init__(self, model_manager):
        self.model_manager = model_manager
        self.transcriber = Transcriber(model_manager)
        self.timestamp_extractor = TimestampExtractor(model_manager)
        self.audio_handler = AudioHandler()
        self.feature_analyzer = FeatureAnalyzer()
        self.result_formatter = ResultFormatter()

    def process_single_word(self, file_path, target_word):
        import editdistance
        import eng_to_ipa as ipa
        transcription = self.transcriber.transcribe_audio_file(file_path)
        target_ipa = ipa.convert(target_word)
        if target_ipa.startswith('*') and target_ipa.endswith('*'):
            raise ValueError(f"IPA not found for '{target_word}'")
        def format_ipa(ipa_str):
            replacements = {
                'ː': '',  # Loại bỏ dấu kéo dài
                'ɹ': 'r', # Thay ɹ bằng r
                'ɡ': 'g', # Thay ɡ bằng g
                'ɐ': 'ə',  # Thay ɐ bằng ə,
                'ʧ': 'tʃ',
                'ʤ': 'dʒ'
            }
            def merge_er_tokens(ipa_tokens_str):
                tokens = list(ipa_tokens_str)
                merged = []
                i = 0
                while i < len(tokens):
                    # Kiểm tra nếu còn ít nhất 2 phần tử và cặp hiện tại là 'e/ə' + 'r'
                    if i + 1 < len(tokens) and tokens[i] in ['e', 'ə'] and tokens[i + 1] == 'r':
                        merged.append('ɚ')
                        i += 2  # Nhảy qua 2 phần tử đã merge
                    else:
                        merged.append(tokens[i])
                        i += 1
                return "".join(merged)
            for old, new in replacements.items():
                ipa_str = ipa_str.replace(old, new)
            return merge_er_tokens(ipa_str)
        transcription_no_stress = format_ipa(remove_stress_marks(transcription).replace(" ", ""))
        target_ipa_no_stress = format_ipa(remove_stress_marks(target_ipa).replace(" ", ""))
        distance = editdistance.eval(transcription_no_stress, target_ipa_no_stress)
        similarity = max(0, min(1, 1 - (distance / len(target_ipa_no_stress))))
        result = {
            "target_word": target_word,
            "target_ipa": target_ipa,
            "target_ipa_no_stress": split_ipa(target_ipa),
            "transcription_ipa": transcription,
            "transcription_no_stress": split_ipa(transcription),
            "similarity": similarity,
            "mapping": compare_phonemes(split_ipa(target_ipa), split_ipa(transcription), target_word, map_text_to_ipa(target_word))
        }
        return result

    def process_sentence(self, file_path, target_sentence):
        word_timestamps, audio_array, sample_rate = self.timestamp_extractor.extract_timestamps(file_path)
        transcription = " ".join([chunk["text"].strip() for chunk in word_timestamps["chunks"]])
        words = transcription.split()
        total_duration = len(audio_array) / sample_rate
        self.feature_analyzer.load_audio_data(audio_array, sample_rate)
        self.feature_analyzer.compute_features()
        self.feature_analyzer.match_timestamps(word_timestamps, sample_rate)
        analysis_results = self.feature_analyzer.analyze_segments(words)
        pronunciations = []
        word_transcriptions = []
        chunks_info = AudioHandler.split_audio_by_timestamps(audio_array, sample_rate, word_timestamps)
        for info in chunks_info:
            trans = self.transcriber.transcribe_audio_file(info["file"])
            word_transcriptions.append({"word": info["text"], "transcription": trans})
            single_word_result = self.process_single_word(info["file"], info["text"])
            pronunciations.append(single_word_result)
        result = self.result_formatter.format_output(transcription, analysis_results, total_duration, pronunciations)
        result["word_transcriptions"] = word_transcriptions
        return result
