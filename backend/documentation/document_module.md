# Tài liệu Kỹ thuật: Module Document

## Tổng quan

Module Document chịu trách nhiệm tìm nạp, xử lý và phục vụ các bài báo web, chủ yếu được lấy từ các nguồn cấp dữ liệu RSS. Nó bao gồm chức năng truy xuất bản ghi (transcript) video YouTube và kích hoạt quy trình quét và xử lý bài báo. Một tính năng chính là sử dụng AI để kiểm duyệt và tùy chọn tóm tắt nội dung bài báo nhằm đảm bảo tính phù hợp cho người học tiếng Anh.

## Các thành phần

### `DocumentController`

Controller REST này xử lý các yêu cầu HTTP đến liên quan đến tài liệu và bài báo.

**Ánh xạ Yêu cầu Cơ sở (Base Request Mapping):** `/document`

**Các Endpoint:**

*   **`GET /document/youtube/{videoId}`**
    *   **Mô tả:** Truy xuất bản ghi cho ID video YouTube được chỉ định.
    *   **Biến Đường dẫn (Path Variable):** `videoId` (chuỗi) - ID của video YouTube.
    *   **Phản hồi:** `ResponseEntity<TranscriptContent>` - Trả về nội dung bản ghi.
    *   **Bộ nhớ đệm (Caching):** Phản hồi được lưu vào bộ nhớ đệm với tên "transcripts".
    *   **Ngoại lệ Tiềm năng:** `TranscriptRetrievalException` nếu không thể truy xuất bản ghi.

*   **`GET /document/scan-and-process`**
    *   **Mô tả:** Kích hoạt tác vụ theo lịch trình để quét và xử lý tài liệu từ các nguồn cấp dữ liệu RSS đã cấu hình.
    *   **Phản hồi:** `ResponseEntity<Map<String, String>>` - Trả về thông báo xác nhận.

*   **`GET /document/news`**
    *   **Mô tả:** Truy xuất danh sách phân trang các bài báo web được coi là phù hợp cho người học, được sắp xếp theo ngày xuất bản giảm dần.
    *   **Tham số Yêu cầu:** Hỗ trợ các tham số phân trang chuẩn (ví dụ: `page`, `size`, `sort`).
    *   **Phản hồi:** `ResponseEntity<Page<WebArticle>>` - Trả về một trang các đối tượng `WebArticle`.

### `DocumentService`

Lớp service này chứa logic nghiệp vụ để xử lý và truy xuất tài liệu.

**Các Phụ thuộc:**

*   `ArticleRepository`: Để tương tác với entity `WebArticle` trong cơ sở dữ liệu.
*   `ChatClient`: Để tương tác với mô hình AI (trong trường hợp này là Gemini) để kiểm duyệt và tóm tắt nội dung.
*   `WebContentTools`: Một tiện ích để trích xuất nội dung chính từ các URL.
*   `ObjectMapper`: Để xử lý JSON.

**Chức năng Chính:**

*   **`getNews(Pageable pageable)`:**
    *   Truy xuất danh sách phân trang các entity `WebArticle` từ cơ sở dữ liệu nơi `suitableForLearners` là true, được sắp xếp theo `publishedDate` giảm dần.

*   **`scanAndProcessDocumentsViaRss()`:**
    *   Phương thức này được lên lịch chạy định kỳ dựa trên thuộc tính `app.news.scan.cron` (mặc định: mỗi 4 giờ).
    *   Nó lặp qua một bản đồ được định nghĩa trước về các URL nguồn cấp dữ liệu RSS mục tiêu và tên nguồn của chúng (`targetRssFeeds`).
    *   Đối với mỗi nguồn cấp dữ liệu, nó tìm nạp các mục nhập mới nhất.
    *   Nó xử lý 3 mục nhập hàng đầu từ mỗi nguồn cấp dữ liệu.
    *   Đối với mỗi mục nhập, nó kiểm tra xem một bài báo có cùng URL đã tồn tại trong cơ sở dữ liệu hay chưa. Nếu chưa, nó tiếp tục xử lý mục nhập RSS đơn lẻ.

*   **`fetchArticlesFromRss(String feedUrlString)`:**
    *   Tìm nạp các bài báo từ URL nguồn cấp dữ liệu RSS đã cho bằng thư viện ROME.
    *   Trả về danh sách các đối tượng `SyndEntry`.

*   **`processSingleRssEntry(SyndEntry rssEntry, String siteName)`:**
    *   Xử lý một mục nhập RSS đơn lẻ.
    *   Trích xuất URL và tiêu đề bài báo.
    *   Sử dụng `WebContentTools` để trích xuất nội dung chính từ URL bài báo.
    *   Chuẩn bị nội dung cho phân tích AI, cắt bớt nếu vượt quá `maxContentLengthForAI`.
    *   Xây dựng một lời nhắc (prompt) cho mô hình AI để đánh giá tính phù hợp của nội dung cho người học tiếng Anh và tùy chọn tóm tắt nó.
    *   Gửi lời nhắc đến `ChatClient`.
    *   Phân tích phản hồi JSON của AI để xác định tính phù hợp và nhận đánh giá/tóm tắt.
    *   Tạo một entity `WebArticle`, điền các trường của nó bao gồm `suitableForLearners` và `moderationNotes` (chứa đánh giá hoặc tóm tắt của AI).
    *   Đặt `processedContent` dựa trên tính phù hợp và cờ `summarizeContent`.
    *   Trích xuất ngày xuất bản từ mục nhập RSS.
    *   Lưu entity `WebArticle` vào cơ sở dữ liệu bằng cách sử dụng `ArticleRepository`.
    *   Bao gồm xử lý lỗi và ghi log cho các giai đoạn khác nhau của quy trình.

## Mô hình Dữ liệu

### `WebArticle`

Đại diện cho một bài báo web được lưu trữ trong cơ sở dữ liệu.

*   `id` (Long): Khóa chính, tự động tạo.
*   `url` (String): URL của bài báo (duy nhất, không được null, độ dài tối đa 1024).
*   `title` (String): Tiêu đề của bài báo (không được null, độ dài tối đa 512).
*   `snippet` (String): Một đoạn trích hoặc mô tả ngắn gọn về bài báo (cột TEXT).
*   `processedContent` (String): Nội dung sau khi xử lý (hoặc tóm tắt của AI hoặc ghi chú về tính phù hợp) (cột TEXT).
*   `source` (String): Trang web nguồn của bài báo (độ dài tối đa 100).
*   `publishedDate` (LocalDateTime): Ngày xuất bản của bài báo.
*   `suitableForLearners` (boolean): Cho biết bài báo có phù hợp cho người học tiếng Anh hay không dựa trên đánh giá của AI (không được null).
*   `moderationNotes` (String): Chứa đánh giá của AI hoặc lý do không phù hợp (cột TEXT).

## Repository

### `ArticleRepository`

Một repository Spring Data JPA cho entity `WebArticle`.

*   Mở rộng `JpaRepository<WebArticle, Long>`.
*   Cung cấp các thao tác CRUD chuẩn.
*   **Các Phương thức Tùy chỉnh:**
    *   `boolean existsByUrl(String url)`: Kiểm tra xem một bài báo có URL đã cho có tồn tại hay không.
    *   `Optional<WebArticle> findByUrl(String url)`: Tìm một bài báo theo URL của nó.
    *   `Page<WebArticle> findBySuitableForLearnersOrderByPublishedDateDesc(boolean suitable, Pageable pageable)`: Tìm danh sách phân trang các bài báo dựa trên tính phù hợp của chúng cho người học, được sắp xếp theo ngày xuất bản giảm dần.
