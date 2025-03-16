import eng_to_ipa as ipa
import re

def split_ipa(ipa_str):
    ipa_str = ipa_str.strip('/').replace('ˈ', '').replace('ˌ', '')  # Loại bỏ dấu trọng âm
    result = []
    i = 0
    while i < len(ipa_str):
        # Kiểm tra âm 3 ký tự (vd: 'eəʳ')
        if i + 2 < len(ipa_str) and ipa_str[i:i+3] in phonemes:
            result.append(ipa_str[i:i+3])
            i += 3
        # Kiểm tra âm 2 ký tự (vd: 'dʒ')
        elif i + 1 < len(ipa_str) and ipa_str[i:i+2] in phonemes:
            result.append(ipa_str[i:i+2])
            i += 2
        # Kiểm tra âm đơn
        elif ipa_str[i] in phonemes:
            result.append(ipa_str[i])
            i += 1
        else:
            print(ipa_str[i])
            i += 1  # Bỏ qua ký tự không xác định
    return result

phonemes = ['b', 'd', 'f', 'g', 'h', 'dʒ', 'ʤ', 'k', 'l', 'm', 'n', 'ŋ', 'p', 'o', 'a', 'y',
        'r', 's', 't', 'v', 'w', 'z', 'ʒ', 'ʧ', "tʃ", 'θ', 'ð', 'j', 'ʃ', 'ɑ', 'æ', 'eɪ',
        'ɛ', 'i:', 'i', 'ɪ', 'aɪ', 'ɒ', 'oʊ', 'ʊ', 'ʌ', 'u:', 'ɔː', 'u', 'ɔ', 'ɔɪ', 'aʊ', 
        'ə', 'eəʳ', 'ɑ:', 'ɜ:ʳ', 'ɔ:', 'ɪəʳ', 'ʊəʳ']
grapheme_ipa_map = {
    "ph": "f", "sh": "ʃ", "ch": "tʃ", "th": "θ", "ng": "ŋ",
    "kn": "n", "wr": "r", "wh": "w", "gh": "", "ck": "k",
    "ea": "i", "ou": "aʊ", "igh": "aɪ", "ough": "ʌf", "qu": "kw",
    "ss": "s", "ee": "i", "oo": "u", "ps": "s",  
    "ious": "əs"
}

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
