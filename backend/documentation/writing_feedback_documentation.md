# Tài liệu Kỹ thuật: Module Phản hồi Bài viết

## Tổng quan

Module Phản hồi Bài viết cung cấp cho người dùng phân tích chi tiết và phản hồi mang tính xây dựng về các đoạn văn tiếng Anh của họ. Module này sử dụng mô hình AI để thực hiện đánh giá toàn diện dựa trên các tiêu chí tương tự như các bài kiểm tra viết học thuật như IELTS.

## Quy trình Tạo Phản hồi

Khi người dùng gửi văn bản để nhận phản hồi thông qua endpoint `POST /writing/analyze`, `WritingFeedbackService` sẽ xử lý yêu cầu. Nó xây dựng một prompt chi tiết bao gồm văn bản đầu vào của người dùng và ngữ cảnh tùy chọn. Prompt này sau đó được gửi đến mô hình AI đã cấu hình (`ChatClient`). AI phân tích văn bản theo hướng dẫn trong prompt và trả về phản hồi dưới dạng đối tượng JSON có cấu trúc.

## Cấu trúc Prompt cho AI

Prompt cho AI được xây dựng cẩn thận để thu được phân tích chi tiết và có cấu trúc. Nó định nghĩa vai trò của AI là một chuyên gia có kinh nghiệm cao trong phân tích ngôn ngữ tiếng Anh và đánh giá bài viết. Nhiệm vụ là phân tích văn bản được cung cấp, xác định điểm mạnh, điểm yếu và đề xuất cải tiến. Prompt chỉ định rõ ràng cấu trúc đầu ra JSON bắt buộc.

Prompt bao gồm:
- **ROLE:** Định nghĩa vai trò và chuyên môn của AI.
- **TASK:** Mô tả quy trình phân tích và tạo phản hồi.
- **INPUT TEXT:** Văn bản của người dùng cần phân tích.
- **(Optional) TOPIC CONTEXT:** Ngữ cảnh bổ sung do người dùng cung cấp.
- **OUTPUT REQUIREMENTS:** Đặc tả chi tiết về cấu trúc JSON bắt buộc.
- **CONSTRAINTS & GUIDELINES:** Các quy tắc cho phản hồi của AI, nhấn mạnh tính nghiêm ngặt của JSON, tính đầy đủ, chính xác, cụ thể, nhất quán và kỹ lưỡng.

## Cấu trúc Đầu ra của AI (JSON)

AI được yêu cầu trả về một đối tượng JSON duy nhất với cấu trúc sau:

```json
{
  "original_text": "...", // Toàn bộ văn bản gốc do người dùng cung cấp.
  "summary_topic": "...", // Tóm tắt ngắn gọn, súc tích về chủ đề chính (ví dụ: "Tác động của công nghệ đến giáo dục").
  "overall_assessment": { // Đánh giá tổng thể về bài viết.
    "task_achievement": { // Mức độ hoàn thành nhiệm vụ (ngụ ý).
      "score": null, // Có thể là null hoặc đánh giá mô tả (ví dụ: "Đã giải quyết đầy đủ").
      "comments": [ // 1-3 nhận xét tổng quan về việc hoàn thành nhiệm vụ.
        "Nhận xét 1...",
        "Nhận xét 2..."
      ]
    },
    "coherence_cohesion": { // Tổ chức, mạch lạc, liên kết ý tưởng.
      "score": null,
      "comments": [ // 1-3 nhận xét tổng quan về tính mạch lạc tổng thể, tổ chức đoạn văn, sử dụng từ nối.
        "Nhận xét 1...",
        "Nhận xét 2..."
      ]
    },
    "lexical_resource": { // Phạm vi từ vựng, độ chính xác, tính phù hợp.
      "score": null,
      "comments": [ // 1-3 nhận xét tổng quan về sự đa dạng, độ chính xác và tính phù hợp của từ vựng.
        "Nhận xét 1...",
        "Nhận xét 2..."
      ]
    },
    "grammatical_range_accuracy": { // Cấu trúc câu, độ chính xác ngữ pháp.
      "score": null,
      "comments": [ // 1-3 nhận xét tổng quan về sự đa dạng cấu trúc câu và độ chính xác ngữ pháp.
        "Nhận xét 1...",
        "Nhận xét 2..."
      ]
    },
    "overall_impression": {
      "score": null, // Có thể là null hoặc điểm ước tính tổng thể (ví dụ: "Ước tính Band 6.0").
      "summary": "Tóm tắt ngắn gọn (1-2 câu) về ấn tượng tổng thể của bài viết."
    }
  },
  "detailed_breakdown": [ // Mảng các đối tượng, mỗi đối tượng đại diện cho một đoạn văn.
    {
      "paragraph_index": 0, // Chỉ số của đoạn văn (bắt đầu từ 0).
      "original_paragraph": "...", // Văn bản gốc của đoạn văn này.
      "paragraph_level_analysis": { // Phân tích cụ thể ở cấp độ đoạn văn.
        "topic_sentence_clarity": "Đánh giá về câu chủ đề.",
        "idea_development": "Đánh giá về sự phát triển ý tưởng trong đoạn văn.",
        "cohesion_within_paragraph": "Đánh giá về sự liên kết giữa các câu trong đoạn văn.",
        "transition_to_next": "Đánh giá về sự chuyển tiếp sang đoạn văn tiếp theo (N/A cho đoạn văn cuối cùng)."
      },
      "sentences": [ // Mảng các đối tượng, mỗi đối tượng đại diện cho một câu trong đoạn văn.
        {
          "sentence_index": 0, // Chỉ số của câu trong đoạn văn (bắt đầu từ 0).
          "original_sentence": "...", // Câu gốc chính xác.
          "upgraded_sentence": "...", // Phiên bản câu đã sửa lỗi và nâng cao.
          "analysis_points": [ // Mảng các điểm phân tích chi tiết cho câu này.
            {
              "criterion": "...", // ví dụ: "Phạm vi và Độ chính xác Ngữ pháp".
              "type": "...", // ví dụ: "lỗi_ngữ_pháp", "vấn_đề_từ_vựng".
              "issue": "...", // Mô tả ngắn gọn về vấn đề.
              "explanation": "...", // Giải thích chi tiết.
              "suggestion": "..." // Đề xuất cụ thể để sửa lỗi.
            }
          ],
          "related_vocabulary": [ // Mảng các từ vựng nâng cao/liên quan đến câu.
            {
              "word_or_phrase": "...",
              "definition_or_usage": "..."
            }
          ],
          "identified_errors_summary": [ // Mảng tóm tắt nhanh các loại lỗi chính trong câu này.
             {
               "error_type": "...", // Loại lỗi: "ngữ_pháp", "từ_vựng", "liên_kết", "chính_tả", "dấu_câu", "phong_cách", "rõ_ràng", "ngữ_cảnh"
               "description": "..." // Mô tả ngắn gọn về loại lỗi.
             }
          ]
        }
      ]
    }
  ]
}
```

## Lưu trữ Dữ liệu

Sau khi nhận và phân tích phản hồi JSON từ AI, `WritingFeedbackService` lưu văn bản đầu vào gốc, ngữ cảnh, số lượng từ, dấu thời gian và toàn bộ phản hồi JSON do AI tạo ra vào một collection MongoDB có tên `writing_feedbacks`. Mỗi mục phản hồi được liên kết với người dùng đã yêu cầu.

## Truy xuất Phản hồi

Người dùng có thể truy xuất kết quả phản hồi bài viết đã lưu trước đó bằng cách sử dụng endpoint `GET /writing/result/my`. Endpoint này gọi phương thức `getWritingFeedbacksCurrentUser` trong service, phương thức này truy vấn collection MongoDB `writing_feedbacks` để lấy tất cả các mục phản hồi cho người dùng đã xác thực, được sắp xếp theo ngày.
