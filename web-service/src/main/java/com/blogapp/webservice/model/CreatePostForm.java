package com.blogapp.webservice.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CreatePostForm {
    @NotBlank(message = "Заголовок обязателен")
    @Length(max = 200, message = "Заголовок не должен превышать 200 символов")
    private String title;

    @NotBlank(message = "Содержание обязательно")
    @Length(max = 5000, message = "Содержание не должно превышать 5000 символов")
    private String content;
}