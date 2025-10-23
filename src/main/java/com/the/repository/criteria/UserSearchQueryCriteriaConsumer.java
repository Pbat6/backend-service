package com.the.repository.criteria;

import com.the.dto.request.SearchCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchQueryCriteriaConsumer implements Consumer<SearchCriteria> {

    private Predicate predicate;
    private CriteriaBuilder builder;
    private Root r;

    @Override
    public void accept(SearchCriteria param) {
        String key = param.getKey();
        Object value = param.getValue();

        // XỬ LÝ ĐẶC BIỆT CHO TRƯỜNG "ROLES"
        if ("roles".equalsIgnoreCase(key)) {

            // Join từ User đến bảng trung gian UserHasRole
            Join<Object, Object> userHasRoleJoin = r.join("roles");

            // Join từ UserHasRole đến bảng Role
            Join<Object, Object> roleJoin = userHasRoleJoin.join("role");

            // Áp dụng điều kiện WHERE lên trường "name" của Role
            Predicate rolePredicate = builder.equal(roleJoin.get("name"), value.toString().toUpperCase());

            // Thêm điều kiện này vào predicate chính
            this.predicate = builder.and(this.predicate, rolePredicate);

            // return để không chạy vào logic xử lý chung bên dưới
            return;
        }

        // Lấy kiểu dữ liệu của thuộc tính trong entity
        Class<?> attributeType = r.get(param.getKey()).getJavaType();

        // Chuyển đổi giá trị đầu vào (luôn là String) sang kiểu dữ liệu chính xác
        Object parsedValue = parseValueToCorrectType(param.getValue().toString(), attributeType);

        switch (param.getOperation().toLowerCase()) {
            case ">":
                if (parsedValue instanceof Comparable) {
                    // Ép kiểu an toàn vì đã qua bước parse
                    predicate = builder.and(predicate, builder.greaterThanOrEqualTo(r.get(param.getKey()), (Comparable) parsedValue));
                }
                break;

            case "<":
                if (parsedValue instanceof Comparable) {
                    predicate = builder.and(predicate, builder.lessThanOrEqualTo(r.get(param.getKey()), (Comparable) parsedValue));
                }
                break;

            case ":":
                if (attributeType == String.class) {
                    predicate = builder.and(predicate, builder.like(
                            builder.lower(r.get(param.getKey())),
                            "%" + parsedValue.toString().toLowerCase() + "%"
                    ));
                } else {
                    predicate = builder.and(predicate, builder.equal(r.get(param.getKey()), parsedValue));
                }
                break;

            default:
                // Không làm gì hoặc throw exception nếu toán tử không hợp lệ
                break;
        }
    }

    private Object parseValueToCorrectType(String value, Class<?> targetType) {
        try {
            if (targetType == String.class) {
                return value;
            }
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(value);
            }
            if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(value);
            }
            if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(value);
            }
            if (targetType == LocalDate.class) {
                return LocalDate.parse(value); // Giả định định dạng ISO, ví dụ: "2025-10-13"
            }
            if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.parseBoolean(value);
            }

        } catch (NumberFormatException | DateTimeParseException e) {
            System.err.println("Không thể parse giá trị '" + value + "' sang kiểu " + targetType.getName());
            return null;
        }
        // Mặc định trả về chuỗi nếu không có kiểu nào khớp
        return value;
    }
}
