import eng_to_ipa as ipa
import re
import json

phonemes = ['b', 'd', 'f', 'g', 'h', 'dʒ', 'ʤ', 'k', 'l', 'm', 'n', 'ŋ', 'p', 'o', 'a', 'y',
        'r', 's', 't', 'v', 'w', 'z', 'ʒ', 'ʧ', "tʃ", 'θ', 'ð', 'j', 'ʃ', 'ɑ', 'æ', 'eɪ',
        'ɛ', 'i:', 'i', 'ɪ', 'aɪ', 'ɒ', 'oʊ', 'ʊ', 'ʌ', 'u:', 'ɔː', 'u', 'ɔ', 'ɔɪ', 'aʊ', 'ɚ',
        'ə', 'eəʳ', 'ɑ:', 'ɜ:ʳ', 'ɔ:', 'ɪəʳ', 'ʊəʳ', 'ɹ']
grapheme_ipa_map = {
    "ph": "f", "sh": "ʃ", "ch": "tʃ", "th": "θ", "ng": "ŋ",
    "kn": "n", "wr": "r", "wh": "w", "gh": "", "ck": "k",
    "ea": "i", "ou": "aʊ", "igh": "aɪ", "ough": "ʌf", "qu": "kw",
    "ss": "s", "ee": "i", "oo": "u", "ps": "s",  
    "ious": "əs"
}
phoneme_set = set(phonemes)
with open("./local_model/vocab.json", "r", encoding="utf8") as f:
    vocab = json.load(f)
vocab_keys = set(vocab.keys())
phoneme_set = phoneme_set.union(vocab_keys)

def split_ipa(ipa_str):
    # Loại bỏ dấu gạch và dấu trọng âm
    ipa_str = ipa_str.strip('/').replace('ˈ', '').replace('ˌ', '').replace('ː', '')
    print(ipa_str)
    result = []
    i = 0

    while i < len(ipa_str):
        if ipa_str[i] == 'ː':  
            i += 1
        # Ưu tiên kiểm tra chuỗi 3 ký tự (ví dụ: 'eəʳ')
        elif i + 2 < len(ipa_str) and ipa_str[i:i+3] in phoneme_set:
            result.append(ipa_str[i:i+3])
            i += 3
        # Sau đó kiểm tra chuỗi 2 ký tự (ví dụ: 'dʒ')
        elif i + 1 < len(ipa_str) and ipa_str[i:i+2] in phoneme_set:
            result.append(ipa_str[i:i+2])
            i += 2
        # Cuối cùng kiểm tra 1 ký tự
        elif ipa_str[i] in phoneme_set:
            result.append(ipa_str[i])
            i += 1
        else:
            # Nếu không tìm thấy, in ký tự đó ra và bỏ qua
            if ipa_str[i] != " ": print(f"Không xác định: {ipa_str[i]}")
            i += 1
            
    return result

# Hàm loại bỏ dấu trọng âm từ IPA
def remove_stress_marks(ipa_str):
    ipa_str = ipa_str.strip('/')
    return ipa_str.replace('ˈ', '').replace('ˌ', '')

grapheme_priority = sorted(grapheme_ipa_map.keys(), key=len, reverse=True)

def split_into_graphemes(word):
    graphemes = []
    i = 0
    while i < len(word):
        matched = False
        for g in grapheme_priority:
            if word[i:].startswith(g):
                graphemes.append(g)
                i += len(g)
                matched = True
                break
        if not matched:
            graphemes.append(word[i])
            i += 1
    return graphemes

def map_text_to_ipa(text):
    raw_ipa = ipa.convert(text)
    ipa_array = split_ipa(ipa.convert(text))  # Chuyển thành array phonemes

    graphemes = split_into_graphemes(text)
    len_g = len(graphemes)
    len_ipa = len(ipa_array)

    if len_g == 0 or len_ipa == 0:
        return []

    ratio = len_ipa / len_g
    mapping = []

    for i, g in enumerate(graphemes):
        if g in grapheme_ipa_map:
            # Xử lý các trường hợp đặc biệt
            mapped_ipa = grapheme_ipa_map[g]
            mapping.append((g, mapped_ipa))
        else:
            # Ánh xạ dựa trên tỷ lệ
            start = int(round(i * ratio))
            end = int(round((i + 1) * ratio))
            start = max(0, min(start, len_ipa))
            end = max(0, min(end, len_ipa))
            if start >= end:
                end = start + 1 if start < len_ipa else start
            ipa_slice = ipa_array[start:end]
            mapping.append((g, "".join(ipa_slice)))

    return mapping

# def merge_er_tokens(ipa_tokens_str):
#     result = list(ipa_tokens_str)
#     if len(result) >= 2 and result[-2] in ['e', 'ə'] and result[-1] == 'r':
#         result[-2:] = ['ɚ']

#     merged = []
#     i = 0
#     while i < len(result):
#         if i < len(result) - 1 and result[i] == 'e' and result[i+1] == 'r':
#             merged.append('ɚ')
#             i += 2
#         else:
#             merged.append(result[i])
#             i += 1

#     return "".join(merged)

# def format_ipa(ipa_str):
#     replacements = {
#         'ː': '',  # Loại bỏ dấu kéo dài
#         'ɹ': 'r', # Thay ɹ bằng r
#         'ɡ': 'g', # Thay ɡ bằng g
#         'ɐ': 'ə',  # Thay ɐ bằng ə,
#         'ʧ': 'tʃ',
#         'ʤ': 'dʒ'
#     }
#     for old, new in replacements.items():
#         ipa_str = ipa_str.replace(old, new)

#     return merge_er_tokens(ipa_str)

def compare_phonemes(target_phonemes, actual_phonemes, target_word, graphene_ipa_mapping):
    """
    So sánh âm vị mục tiêu với âm vị thực tế và trả về kết quả dạng JSON,
    kèm theo vị trí bắt đầu và kết thúc của các ký tự trong target_word tương ứng với mỗi âm vị.

    Args:
        target_phonemes (list): Danh sách các âm vị đúng (ví dụ: ["k", "ə", "m", "ju", "n"]).
        actual_phonemes (list): Danh sách các âm vị do người dùng cung cấp (ví dụ: ["k", "ə", "m", "j", "u", "n"]).
        target_word (str): Từ gốc (ví dụ: "communication").
        graphene_ipa_mapping (list): Danh sách ánh xạ các ký tự của target_word với âm vị.
            Ví dụ:
            [
                ["c", "k"],
                ["o", "ə"],
                ["m", "m"],
                ["m", "ju"],
                ["u", "ju"],
                ["n", "n"],
                ["i", "ə"],
                ["c", "k"],
                ["a", "eɪ"],
                ["t", "ʃ"],
                ["i", "ʃ"],
                ["o", "ə"],
                ["n", "n"]
            ]

    Returns:
        str: Chuỗi JSON chứa các trường "correct_phoneme", "actual_phoneme", "result",
            "start_index" và "end_index".
    """
    # Tạo danh sách chứa các cặp (start_index, end_index) ứng với từng âm vị trong target_phonemes
    mapping_indices = []
    mapping_idx = 0
    for phon in target_phonemes:
        if mapping_idx >= len(graphene_ipa_mapping):
            mapping_indices.append((None, None))
            continue
        start = mapping_idx
        # Nhóm các entry liên tiếp trong graphene_ipa_mapping có âm vị bằng phon
        while mapping_idx < len(graphene_ipa_mapping) and graphene_ipa_mapping[mapping_idx][1] == phon:
            mapping_idx += 1
        end = mapping_idx - 1 if mapping_idx > start else start
        mapping_indices.append((start, end))
    
    result = []
    target_idx = 0  # chỉ số cho target_phonemes
    actual_idx = 0  # chỉ số cho actual_phonemes

    # Duyệt qua target_phonemes
    while target_idx < len(target_phonemes):
        # Lấy vị trí ánh xạ của âm vị hiện tại (nếu có)
        start_idx, end_idx = mapping_indices[target_idx] if target_idx < len(mapping_indices) else (None, None)
        
        if actual_idx < len(actual_phonemes):
            # Trường hợp 1: Khớp trực tiếp
            if target_phonemes[target_idx] == actual_phonemes[actual_idx]:
                result.append({
                    "correct_phoneme": target_phonemes[target_idx],
                    "actual_phoneme": actual_phonemes[actual_idx],
                    "result": "correct",
                    "start_index": start_idx,
                    "end_index": end_idx
                })
                target_idx += 1
                actual_idx += 1
            # Trường hợp 2: Âm vị đa ký tự trong target (ví dụ: "ju" khớp với ["j", "u"])
            elif (len(target_phonemes[target_idx]) > 1 and
                  actual_idx + len(target_phonemes[target_idx]) <= len(actual_phonemes) and
                  actual_phonemes[actual_idx:actual_idx + len(target_phonemes[target_idx])] == list(target_phonemes[target_idx])):
                actual_combined = ''.join(actual_phonemes[actual_idx:actual_idx + len(target_phonemes[target_idx])])
                result.append({
                    "correct_phoneme": target_phonemes[target_idx],
                    "actual_phoneme": actual_combined,
                    "result": "correct",
                    "start_index": start_idx,
                    "end_index": end_idx
                })
                target_idx += 1
                actual_idx += len(target_phonemes[target_idx - 1])
            # Trường hợp 3: Không khớp
            else:
                result.append({
                    "correct_phoneme": target_phonemes[target_idx],
                    "actual_phoneme": actual_phonemes[actual_idx],
                    "result": "incorrect",
                    "start_index": start_idx,
                    "end_index": end_idx
                })
                target_idx += 1
                actual_idx += 1
        else:
            # Nếu actual_phonemes đã hết nhưng target_phonemes chưa, đánh dấu "N/A"
            result.append({
                "correct_phoneme": target_phonemes[target_idx],
                "actual_phoneme": "N/A",
                "result": "incorrect",
                "start_index": start_idx,
                "end_index": end_idx
            })
            target_idx += 1

    return result
            