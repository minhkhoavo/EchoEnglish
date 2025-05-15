# Báo cáo phân tích chức năng Speech Analyze

## Tổng quan kiến trúc

Chức năng phân tích giọng nói được triển khai theo kiến trúc microservice với backend Java và backend Python.
- Backend Java (`SpeechAnalyzeController.java` và `SpeechAnalyzeService`) xử lý các yêu cầu API từ frontend, tương tác với dịch vụ phân tích giọng nói Python và lưu trữ kết quả vào cơ sở dữ liệu (entity `SentenceAnalysisResult` và `SentenceAnalysisResultRepository`).
- Backend Python (thư mục `speech-analyze`) là một ứng dụng Flask thực hiện xử lý giọng nói, phiên âm, trích xuất dấu thời gian, phân tích đặc trưng và so sánh âm vị bằng cách sử dụng các thư viện và mô hình được đào tạo trước khác nhau.

## Backend (Java)

- `SpeechAnalyzeController.java`:
    - Cung cấp ba endpoint dưới `/speech`:
        - `POST /analyze/word`: Để phân tích các từ đơn lẻ. Nhận `target_word` và `audio_file` làm đầu vào. Gọi `SpeechAnalyzeService.analyzeSpeech` và trả về danh sách `PhonemeComparisonDTO`.
        - `POST /analyze/sentences`: Để phân tích giọng nói cho các câu. Nhận `target_word` (có thể là văn bản câu mục tiêu) và `audio_file`. Gọi `SpeechAnalyzeService.analyzeSentence` và trả về một chuỗi (dựa trên mã Python, đây có thể là ID tác vụ cho xử lý bất đồng bộ).
        - `GET /result/my`: Truy xuất kết quả phân tích câu cho người dùng hiện tại đã xác thực bằng cách gọi `SpeechAnalyzeService.getSentenceResultsByCurrentUser`.
    - Sử dụng `SpeechAnalyzeService` để ủy quyền logic cốt lõi.
    - Xử lý tải lên tệp (`MultipartFile`).

- `SpeechAnalyzeService` (không đọc trực tiếp, nhưng suy ra từ việc sử dụng controller):
    - Hoạt động như một trung gian giữa controller và dịch vụ Python.
    - Có khả năng xử lý việc tạo các yêu cầu HTTP đến các endpoint `/api/transcribe` và `/api/result/<task_id>` của ứng dụng Flask Python.
    - Đối với phân tích câu, nó có thể bắt đầu tác vụ bất đồng bộ và sau đó thăm dò endpoint `/api/result/<task_id>` để lấy kết quả cuối cùng.
    - Ánh xạ dữ liệu nhận được từ dịch vụ Python sang các DTO và entity Java (như `PhonemeComparisonDTO` và `SentenceAnalysisResult`).
    - Tương tác với `SentenceAnalysisResultRepository` để lưu kết quả phân tích cho các câu.

- Entity `SentenceAnalysisResult`:
    - Đại diện cho kết quả phân tích câu được lưu trữ trong cơ sở dữ liệu.

## Thư mục Speech-Analyze (Python)

- `app.py`:
    - Điểm vào chính của ứng dụng Flask.
    - Khởi tạo `ModelManager` và `APIController`.
    - Định nghĩa các endpoint `/api/transcribe` và `/api/result/<task_id>`.
    - Xử lý việc nhận dữ liệu âm thanh (dưới dạng tệp hoặc blob base64) và văn bản mục tiêu.
    - Đối với phân tích từ đơn lẻ, nó gọi trực tiếp `APIController.process_single_word`.
    - Đối với phân tích câu, nó bắt đầu một luồng nền (`process_sentence_background`) gọi `APIController.process_sentence` và trả về một `task_id` để xử lý bất đồng bộ.
    - Hàm `process_sentence_background` lưu kết quả hoặc lỗi vào một tệp JSON được đặt tên theo `task_id` trong thư mục `results_cache`.
    - Endpoint `/api/result/<task_id>` cho phép thăm dò kết quả của tác vụ phân tích câu bằng cách đọc tệp JSON tương ứng.

- `analysis_controller.py`:
    - Định nghĩa lớp `APIController`, điều phối quá trình phân tích giọng nói.
    - Sử dụng các instance của `ModelManager`, `Transcriber`, `TimestampExtractor`, `AudioHandler` và `FeatureAnalyzer`.
    - `process_single_word`:
        - Phiên âm âm thanh.
        - Chuyển đổi từ mục tiêu sang IPA.
        - Tính toán độ tương đồng phát âm bằng cách sử dụng khoảng cách chỉnh sửa giữa IPA đã phiên âm và IPA mục tiêu (sau khi loại bỏ dấu trọng âm).
        - Sử dụng `phoneme_utils.compare_phonemes` để có được so sánh chi tiết cấp âm vị và ánh xạ tới các ký tự của từ gốc.
    - `process_sentence`:
        - Trích xuất dấu thời gian từ âm thanh.
        - Tải dữ liệu âm thanh vào `FeatureAnalyzer`.
        - Phân tích các đặc trưng âm thanh (cao độ, cường độ, biến thể) cho từng đoạn từ bằng cách sử dụng `FeatureAnalyzer`.
        - Tách âm thanh thành các tệp từ riêng lẻ.
        - Phiên âm từng tệp từ và thực hiện phân tích phát âm từ đơn lẻ cho từng từ bằng cách sử dụng `process_single_word`.
        - Định dạng đầu ra cuối cùng bằng cách sử dụng `ResultFormatter`, bao gồm phân tích cấp đoạn và thống kê tóm tắt tổng thể (tốc độ nói, tần suất từ, v.v.).

- `feature_analyzer.py`:
    - Định nghĩa lớp `FeatureAnalyzer` để trích xuất và phân tích các đặc trưng âm thanh.
    - Sử dụng `librosa` và `numpy`.
    - Tính toán tần số cơ bản (f0) và cường độ (RMS).
    - Khớp dấu thời gian với các khung âm thanh.
    - Phân tích các đoạn (từ) để tính toán cao độ trung bình, cường độ trung bình, ngữ điệu và biến thể cao độ.
    - Tính toán `stress_score` cho mỗi từ dựa trên cao độ, cường độ và thời lượng đã chuẩn hóa.

- `models.py`:
    - Định nghĩa lớp `ModelManager` để tải và quản lý các mô hình nhận dạng giọng nói từ Hugging Face.
    - Sử dụng thư viện `transformers`.
    - Tải `facebook/wav2vec2-lv-60-espeak-cv-ft` để phiên âm chung.
    - Tải `nyrahealth/CrisperWhisper` để trích xuất dấu thời gian, được cấu hình như một pipeline nhận dạng giọng nói với dấu thời gian từ.
    - Cấu hình các mô hình để sử dụng GPU nếu có.

- `phoneme_utils.py`:
    - Cung cấp các hàm tiện ích để làm việc với âm vị và ánh xạ văn bản sang IPA.
    - Sử dụng `eng_to_ipa`.
    - `split_ipa`: Tách chuỗi IPA thành danh sách các âm vị.
    - `remove_stress_marks`: Loại bỏ dấu trọng âm khỏi IPA.
    - `split_into_graphemes`: Tách từ thành các grapheme.
    - `map_text_to_ipa`: Ánh xạ grapheme sang âm vị IPA.
    - `compare_phonemes`: So sánh âm vị mục tiêu và âm vị thực tế, xác định tính đúng đắn và ánh xạ kết quả trở lại các chỉ số ký tự trong từ gốc.

- `audio_utils.py`:
    - Cung cấp các lớp tiện ích để xử lý tệp âm thanh và chuẩn bị dữ liệu cho các mô hình ML.
    - `AudioHandler`:
        - `load_audio_file`: Tải tệp âm thanh bằng `pydub`, chuẩn hóa âm lượng và lưu lại dưới định dạng WAV.
        - `save_temp_file_from_bytes`: Lưu dữ liệu âm thanh dạng byte vào một tệp tạm thời.
        - `split_audio_by_timestamps`: Tách mảng dữ liệu âm thanh thành các đoạn nhỏ hơn dựa trên dấu thời gian được cung cấp (thường là dấu thời gian từ), lưu mỗi đoạn thành một tệp WAV riêng biệt trong thư mục `words_output`.
    - `Transcriber`:
        - Sử dụng mô hình `wav2vec2` được tải bởi `ModelManager`.
        - `transcribe_audio_file`: Tải tệp âm thanh bằng `librosa` (với tốc độ lấy mẫu 16000 Hz), xử lý đầu vào bằng `wav2vec_processor` và sử dụng `wav2vec_model` để tạo phiên âm văn bản.
    - `TimestampExtractor`:
        - Sử dụng pipeline `CrisperWhisper` được tải bởi `ModelManager`.
        - `extract_timestamps`: Tải tệp âm thanh bằng `soundfile`, xử lý bằng `timestamp_pipe` để lấy dấu thời gian cho từng từ. Sau đó, nó gọi `adjust_pauses_for_hf_pipeline_output` để điều chỉnh dấu thời gian, có thể để xử lý tốt hơn các khoảng dừng. Trả về kết quả từ pipeline, mảng âm thanh và tốc độ lấy mẫu.
    - `adjust_pauses_for_hf_pipeline_output`: Hàm trợ giúp để điều chỉnh dấu thời gian từ đầu ra của pipeline Hugging Face, phân phối lại thời gian của các khoảng dừng giữa các từ.

## Luồng dữ liệu và lưu trữ chi tiết

1. **Yêu cầu từ Frontend:** Yêu cầu phân tích giọng nói (từ đơn lẻ hoặc câu) được gửi từ frontend đến `SpeechAnalyzeController` của backend Java. Yêu cầu này bao gồm tệp âm thanh và văn bản mục tiêu (từ hoặc câu).
2. **Xử lý tại Java Backend:**
    - `SpeechAnalyzeController` nhận yêu cầu và ủy quyền xử lý cho `SpeechAnalyzeService`.
    - `SpeechAnalyzeService` chuẩn bị dữ liệu và tạo một yêu cầu HTTP POST đến endpoint `/api/transcribe` của dịch vụ Python. Dữ liệu âm thanh có thể được gửi dưới dạng `MultipartFile` hoặc base64 encoded blob.
3. **Xử lý tại Python Backend (`app.py`):**
    - Endpoint `/api/transcribe` nhận yêu cầu.
    - Dữ liệu âm thanh được lưu tạm thời vào một tệp WAV.
    - Dựa vào tham số `audio_type`:
        - **Phân tích từ đơn lẻ (`single`):** `APIController.process_single_word` được gọi đồng bộ. Kết quả phân tích được trả về trực tiếp trong phản hồi HTTP (thường là 200 OK). Tệp âm thanh tạm thời được xóa ngay sau khi xử lý.
        - **Phân tích câu (`sentence`):** Một `task_id` duy nhất được tạo. Một luồng nền mới được bắt đầu để chạy hàm `process_sentence_background` với các tham số cần thiết (đường dẫn tệp âm thanh tạm thời, câu mục tiêu, đường dẫn tệp kết quả). Một phản hồi HTTP 202 Accepted được trả về ngay lập tức cho backend Java, chứa `task_id` và trạng thái "processing". Tệp âm thanh tạm thời sẽ được xóa bởi luồng nền sau khi hoàn thành xử lý.
4. **Xử lý bất đồng bộ (chỉ cho phân tích câu):**
    - Luồng nền chạy `process_sentence_background` gọi `APIController.process_sentence`.
    - `APIController.process_sentence` thực hiện các bước phân tích chi tiết (trích xuất dấu thời gian, phân tích đặc trưng, phân tích phát âm từng từ).
    - Kết quả phân tích cuối cùng (hoặc thông báo lỗi nếu có) được lưu vào một tệp JSON trong thư mục `results_cache` với tên là `<task_id>.json`. Trạng thái trong tệp được cập nhật thành "completed" hoặc "error".
5. **Truy xuất kết quả (chỉ cho phân tích câu):**
    - Backend Java (`SpeechAnalyzeService`) hoặc frontend (tùy thuộc vào cách triển khai thăm dò) gửi yêu cầu HTTP GET đến endpoint `/api/result/<task_id>` của dịch vụ Python, sử dụng `task_id` đã nhận trước đó.
    - Endpoint `/api/result/<task_id>` đọc tệp JSON tương ứng trong `results_cache`.
    - Nó thăm dò tệp này trong một khoảng thời gian nhất định (`POLLING_TIMEOUT`).
    - Nếu trạng thái trong tệp là "processing", nó chờ một khoảng thời gian ngắn (`CHECK_INTERVAL`) và đọc lại.
    - Nếu trạng thái là "completed" hoặc "error", nó trả về nội dung của tệp JSON trong phản hồi HTTP và xóa tệp kết quả khỏi `results_cache`.
    - Nếu hết thời gian thăm dò mà trạng thái vẫn là "processing", nó trả về trạng thái "processing".
6. **Lưu trữ dữ liệu tại Java Backend:**
    - Sau khi `SpeechAnalyzeService` của Java nhận được kết quả phân tích câu cuối cùng từ dịch vụ Python (thông qua phản hồi đồng bộ cho từ đơn lẻ hoặc thông qua thăm dò cho câu), nó ánh xạ dữ liệu nhận được sang entity `SentenceAnalysisResult`.
    - Entity `SentenceAnalysisResult` này sau đó được lưu vào cơ sở dữ liệu bằng cách sử dụng `SentenceAnalysisResultRepository`.
7. **Truy xuất kết quả đã lưu:**
    - Endpoint `GET /speech/result/my` trong `SpeechAnalyzeController` cho phép frontend truy xuất các kết quả phân tích câu đã lưu trữ cho người dùng hiện tại bằng cách gọi `SpeechAnalyzeService.getSentenceResultsByCurrentUser`, dịch vụ này sẽ truy vấn `SentenceAnalysisResultRepository`.

Tóm lại, tính năng phân tích giọng nói tận dụng dịch vụ Python để thực hiện các tác vụ xử lý và phân tích âm thanh nặng nề bằng cách sử dụng các mô hình học máy và các thư viện xử lý tín hiệu số. Backend Java đóng vai trò là cổng API, điều phối các cuộc gọi đến dịch vụ Python (bao gồm cả cơ chế bất đồng bộ cho phân tích câu) và quản lý việc lưu trữ kết quả phân tích câu vào cơ sở dữ liệu. Giao tiếp giữa hai dịch vụ được thực hiện thông qua các yêu cầu HTTP.
