HOP_LENGTH = 512
class FeatureAnalyzer:
    def __init__(self):
        self.audio_data = {}

    def load_audio_data(self, audio_array, sample_rate):
        total_duration = len(audio_array) / sample_rate
        self.audio_data = {
            'y': audio_array,
            'sr': sample_rate,
            'duration': total_duration
        }

    def compute_features(self):
        y = self.audio_data['y']
        sr_audio = self.audio_data['sr']
        f0, _, _ = librosa.pyin(
            y,
            fmin=librosa.note_to_hz('C2'),
            fmax=librosa.note_to_hz('C7'),
            sr=sr_audio, hop_length=HOP_LENGTH
        )
        intensity = librosa.feature.rms(y=y, hop_length=HOP_LENGTH)[0]
        self.audio_data['f0'] = f0
        self.audio_data['intensity'] = intensity

    def match_timestamps(self, word_timestamps, sample_rate):
        intervals = []
        total_duration = self.audio_data.get('duration', 0)
        for chunk in word_timestamps["chunks"]:
            start, end = chunk["timestamp"]
            start_sample = int(start * sample_rate)
            if end is None:
                end = total_duration
            end_sample = int(end * sample_rate)
            intervals.append([start_sample, end_sample])
        self.audio_data['intervals'] = intervals

    def analyze_segments(self, words):
        sr_audio = self.audio_data['sr']
        intervals = self.audio_data['intervals']
        f0 = self.audio_data.get('f0')
        intensity = self.audio_data.get('intensity')
        frame_rate = sr_audio / HOP_LENGTH
        results = []
        durations = []
        for i, interval in enumerate(intervals):
            start_sample, end_sample = interval
            start_time, end_time = start_sample / sr_audio, end_sample / sr_audio
            duration = end_time - start_time
            durations.append(duration)
            start_frame = int(start_time * frame_rate)
            end_frame = int(end_time * frame_rate)
            seg_pitch = f0[start_frame:end_frame]
            seg_intensity = intensity[start_frame:end_frame]
            valid_pitch = [p for p in seg_pitch if not np.isnan(p)]
            if len(valid_pitch) <= 1:
                intonation = "không xác định"
            else:
                intonation = "lên" if valid_pitch[-1] > valid_pitch[0] else "xuống" if valid_pitch[-1] < valid_pitch[0] else "bằng"
            avg_pitch = np.nanmean(valid_pitch) if valid_pitch else np.nan
            avg_intensity = np.mean(seg_intensity) if len(seg_intensity) > 0 else 0
            avg_pitch_variation = np.mean(np.abs(np.diff(valid_pitch))) if len(valid_pitch) > 1 else np.nan
            word = words[i] if i < len(words) else ""
            results.append({
                'word': word,
                'start_time': float(start_time),
                'end_time': float(end_time),
                'avg_pitch': float(avg_pitch) if not np.isnan(avg_pitch) else None,
                'avg_intensity': float(avg_intensity),
                'intonation': intonation,
                'avg_pitch_variation': float(avg_pitch_variation) if not np.isnan(avg_pitch_variation) else None
            })
        all_pitches = [res['avg_pitch'] for res in results if res['avg_pitch'] is not None]
        all_intensities = [res['avg_intensity'] for res in results]
        pitch_variations = [res['avg_pitch_variation'] for res in results if res['avg_pitch_variation'] is not None]
        max_pitch = max(all_pitches) if all_pitches else 1
        max_intensity = max(all_intensities) if all_intensities else 1
        max_duration = max(durations) if durations else 1
        max_pitch_var = max(pitch_variations) if pitch_variations else 1

        for i, res in enumerate(results):
            norm_pitch = (res['avg_pitch'] / max_pitch) if res['avg_pitch'] is not None else 0
            norm_intensity = (res['avg_intensity'] / max_intensity) if max_intensity != 0 else 0
            norm_duration = (durations[i] / max_duration) if max_duration != 0 else 0
            norm_pitch_var = (res['avg_pitch_variation'] / max_pitch_var) if res['avg_pitch_variation'] is not None else 0

            stress_score = (
                norm_pitch * STRESS_WEIGHTS["pitch"] +
                norm_intensity * STRESS_WEIGHTS["intensity"] +
                norm_duration * STRESS_WEIGHTS["duration"] +
                norm_pitch_var * STRESS_WEIGHTS["pitch_variation"]
            )
            results[i]['stress_score'] = float(stress_score)
            results[i]['duration'] = float(durations[i])
        return results

    def overall_pitch_analysis(self):
        f0 = self.audio_data.get('f0')
        valid_pitches = [p for p in f0 if not np.isnan(p)]
        if len(valid_pitches) > 1:
            intonation_overall = "lên giọng" if valid_pitches[-1] > valid_pitches[0] else "xuống giọng" if valid_pitches[-1] < valid_pitches[0] else "bằng"
            avg_pitch_overall = np.mean(valid_pitches)
        else:
            intonation_overall, avg_pitch_overall = "không xác định", np.nan
        print("\n--- Phân tích cao độ tổng thể ---")
        print(f"Cao độ trung bình: {float(avg_pitch_overall):.2f} Hz" if not np.isnan(avg_pitch_overall) else "Cao độ trung bình: không xác định")
        print(f"Ngữ điệu: {intonation_overall}")
