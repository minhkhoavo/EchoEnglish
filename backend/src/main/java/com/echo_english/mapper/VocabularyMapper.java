package com.echo_english.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring") // Có thể mở trong target (sau khi run) để xem nó tạo @Mapper map dữ liệu thế nào
public interface VocabularyMapper {
//    Vocabulary toVocabulary(VocabularyCreationRequest request);

//    // @Mapping(source = "lastName", target = "firstName")
//    // @Mapping(target = "lastName",ignore = true)
//    UserResponse toUserResponse(User user); // Chuyển đổi từ User (Entity) sang UserResponse (DTO).
//
//    void updateUser(@MappingTarget User user, UserUpdateRequest request); // @MappingTarget giúp cập nhật trực tiếp UserUpdateRequest vào User thay vì tạo mới một object khác.
}